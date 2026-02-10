package dev.kuehni.llmtestgen.fixer;

import com.github.javaparser.JavaParser;
import dev.kuehni.llmtestgen.d4j.Defects4J;
import dev.kuehni.llmtestgen.d4j.Defects4JUtils;
import dev.kuehni.llmtestgen.dto.ClassUnderTest;
import dev.kuehni.llmtestgen.dto.GeneratedTestClass;
import dev.kuehni.llmtestgen.models.FixerStage;
import dev.kuehni.llmtestgen.models.Iteration;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

import static dev.kuehni.llmtestgen.util.text.TextUtils.formatDuration;

public class TestFixPipeline {

    private static final Logger logger = LoggerFactory.getLogger(TestFixPipeline.class);

    @Nonnull
    private final JavaParser javaParser;

    @Nonnull
    private final TestFixer testFixer;

    @Nonnull
    private final Defects4J defects4j;

    private final int maxReprompts;

    public TestFixPipeline(
            @Nonnull JavaParser javaParser,
            @Nonnull TestFixer testFixer,
            @Nonnull Defects4J defects4j,
            int maxReprompts
    ) {
        this.javaParser = Objects.requireNonNull(javaParser, "javaParser");
        this.testFixer = Objects.requireNonNull(testFixer, "testFixer");
        this.defects4j = Objects.requireNonNull(defects4j, "defects4j");
        this.maxReprompts = maxReprompts;
    }

    @Nonnull
    public Result execute(
            @Nonnull ClassUnderTest classUnderTest,
            @Nonnull GeneratedTestClass generatedTest,
            @Nonnull Defects4J.Workspace workspace,
            @Nonnull Iteration iteration
    ) throws IOException {
        Objects.requireNonNull(classUnderTest, "classUnderTest");
        Objects.requireNonNull(generatedTest, "generatedTest");
        Objects.requireNonNull(workspace, "workspace");
        Objects.requireNonNull(iteration, "iteration");

        logger.info("Running test fix pipeline...");
        final var pipelineStart = Instant.now();

        var currentState = generatedTest;
        var repromptCount = 0;
        while (true) {
            final var stageResult = executeStage(classUnderTest, currentState, workspace, iteration);

            switch (stageResult) {
                case StageResult.Success(var validTest) -> {
                    logger.debug(
                            "Test fix pipeline took {}",
                            formatDuration(Duration.between(pipelineStart, Instant.now()))
                    );
                    return new Result(validTest, repromptCount);
                }
                case StageResult.RetryWith(var nextGenerator) -> {
                    repromptCount++;
                    logger.warn("Reprompt {}/{}", repromptCount, maxReprompts);

                    if (repromptCount > maxReprompts) {
                        throw new IllegalStateException("Ran out of reprompts");
                    }

                    currentState = nextGenerator.get();
                }
                default -> throw new IllegalStateException("Unsupported stage result: " + stageResult);
            }
        }
    }

    public record Result(@Nonnull ValidTestClass validTest, int repromptCount) {
        public Result {
            Objects.requireNonNull(validTest, "validTest");
            if (repromptCount < 0) {
                throw new IllegalStateException("Reprompt count must not be negative");
            }
        }
    }

    @Nonnull
    private StageResult executeStage(
            @Nonnull ClassUnderTest classUnderTest,
            @Nonnull GeneratedTestClass generatedTest,
            @Nonnull Defects4J.Workspace workspace,
            @Nonnull Iteration iteration
    ) throws IOException {
        logger.trace("Parsing test code...");
        final var parsingStage = new FixerStage(iteration, FixerStage.Type.PARSE);
        parsingStage.save();

        final var parseResult = javaParser.parse(generatedTest.code);
        if (!parseResult.isSuccessful()) {
            parsingStage.setCompleted();
            parsingStage.setErrorMessage(parseResult.toString());
            parsingStage.save();

            logger.warn("Parsing failed. Requesting a fix...");
            return new StageResult.RetryWith(() -> generatedTest.info.generated(testFixer.fixSyntax(
                    generatedTest,
                    parseResult.getProblems(),
                    parsingStage
            )));
        }

        parsingStage.setCompleted();
        parsingStage.setSuccess(true);
        parsingStage.save();

        final var parsedTest = generatedTest.info.parsed(parseResult.getResult().orElseThrow());

//        System.out.println("```");
//        System.out.println(validTest.code);
//        System.out.println("```");

//        final var primaryClass = JavaUtils.getPrimaryClass(compilationUnit);

        logger.trace("Writing parsed test code to workspace...");
        parsedTest.write();

        logger.trace("Compiling test...");
        final var compileStage = new FixerStage(iteration, FixerStage.Type.COMPILE);
        compileStage.save();

        final var compileResult = defects4j.compile(workspace);
        if (compileResult.exitCode != 0) {
            final String javacOutput = Defects4JUtils.extractCompileTestJavac(compileResult.stdout);
            compileStage.setCompleted();
            if (javacOutput.isBlank()) {
                compileStage.setErrorMessage(compileResult.stdout);
                compileStage.save();
                System.out.println(compileResult.stdout);
                throw new IllegalStateException(
                        "Cannot find test compilation javac output. Compilation failed with exit code " +
                        compileResult.exitCode
                );
            }
            compileStage.setErrorMessage(javacOutput);
            compileStage.save();
            logger.warn("Compiling test failed. Requesting a fix...");
            return new StageResult.RetryWith(() -> generatedTest.info.generated(testFixer.fixCompilationErrors(
                    parsedTest,
                    javacOutput,
                    compileStage
            )));
        }
        compileStage.setCompleted();
        compileStage.setSuccess(true);
        compileStage.save();

        final var testMethodNames = parsedTest.testMethodNames();
        logger.trace("Running {} test(s) from {}...", testMethodNames.length, parsedTest.info.fullyQualifiedName);
        final var testRunStage = new FixerStage(iteration, FixerStage.Type.TEST);
        testRunStage.save();

        final var testResult = defects4j.test(workspace, parsedTest.info.fullyQualifiedName, testMethodNames);

        logger.trace("Succeeding tests: {}", testMethodNames.length - testResult.failingTests);
        if (!testResult.isSuccess()) {
            logger.debug("Failing tests: {}", testResult.failingTests);
            testRunStage.setCompleted();
            testRunStage.setErrorMessage(testResult.getFailingTestsOutput());
            testRunStage.save();

            final var failingTestsOutput = testResult.getFailingTestsOutput();
            logger.trace("Test output:\n{}", failingTestsOutput);

            logger.warn("Test suite is not green. Requesting a fix...");
            return new StageResult.RetryWith(() -> generatedTest.info.generated(testFixer.fixFailingTests(
                    parsedTest,
                    failingTestsOutput,
                    classUnderTest,
                    testRunStage
            )));
        }
        testRunStage.setCompleted();
        testRunStage.setSuccess(true);
        testRunStage.save();

        return new StageResult.Success(parsedTest.toValid());
    }


    private sealed interface StageResult {
        record Success(@Nonnull ValidTestClass validTest) implements StageResult {
            public Success {
                Objects.requireNonNull(validTest, "validTest");
            }
        }

        record RetryWith(@Nonnull Supplier<GeneratedTestClass> generator) implements StageResult {
            public RetryWith {
                Objects.requireNonNull(generator, "generator");
            }
        }
    }
}
