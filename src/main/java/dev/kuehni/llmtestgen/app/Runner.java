package dev.kuehni.llmtestgen.app;

import com.github.javaparser.JavaParser;
import dev.kuehni.llmtestgen.cobertura.CoverageResult;
import dev.kuehni.llmtestgen.d4j.Defects4J;
import dev.kuehni.llmtestgen.d4j.TestEvaluator;
import dev.kuehni.llmtestgen.dto.ClassUnderTest;
import dev.kuehni.llmtestgen.dto.JavaOptions;
import dev.kuehni.llmtestgen.dto.TestClassInfo;
import dev.kuehni.llmtestgen.feedback.CoverageFeedbackLoop;
import dev.kuehni.llmtestgen.feedback.MutationFeedbackLoop;
import dev.kuehni.llmtestgen.fixer.TestFixPipeline;
import dev.kuehni.llmtestgen.fixer.TestFixer;
import dev.kuehni.llmtestgen.fixer.TestGenerator;
import dev.kuehni.llmtestgen.llm.LlmClient;
import dev.kuehni.llmtestgen.llm.Metadata;
import dev.kuehni.llmtestgen.major.MutationTestResult;
import dev.kuehni.llmtestgen.models.*;
import dev.kuehni.llmtestgen.util.functions.ThrowingFunction;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static dev.kuehni.llmtestgen.util.text.TextUtils.formatStackTrace;

public class Runner {

    private static final Logger logger = LoggerFactory.getLogger(Runner.class);

    @Nonnull
    private final Metadata metadata;

    @Nonnull
    private final Defects4J defects4j;

    private final int maxLlmRepromptsForFixingTests;

    @Nonnull
    private final TestGenerator testGenerator;

    @Nonnull
    private final TestFixPipeline testFixPipeline;

    @Nonnull
    private final TestEvaluator testEvaluator;

    private final int bugId;

    @Nonnull
    private final CoverageFeedbackLoop coverageFeedbackLoop;

    @Nonnull
    private final MutationFeedbackLoop mutationFeedbackLoop;

    @Nonnull
    private final Defects4J.Project project;

    @Nonnull
    private final Run run;

    private boolean hasBeenRun = false;


    public Runner(
            @Nonnull Run run,
            @Nonnull LlmClient llmClient,
            @Nonnull JavaOptions javaOptions,
            @Nonnull JavaParser javaParser,
            @Nonnull Defects4J defects4j,
            @Nonnull Defects4J.Project project,
            int bugId,
            int maxLlmRepromptsForFixingTests,
            int feedbackIterations
    ) {
        this.bugId = bugId;
        Objects.requireNonNull(javaParser, "javaParser");
        Objects.requireNonNull(llmClient, "llmClient");
        Objects.requireNonNull(javaOptions, "javaOptions");

        this.run = Objects.requireNonNull(run, "run");
        metadata = new Metadata(run.getUuid());
        this.defects4j = Objects.requireNonNull(defects4j, "defects4j");
        this.project = Objects.requireNonNull(project, "project");
        this.maxLlmRepromptsForFixingTests = maxLlmRepromptsForFixingTests;

        final var testFixer = new TestFixer(llmClient, metadata, javaOptions);

        this.testGenerator = new TestGenerator(llmClient, metadata, javaOptions);
        this.testFixPipeline = new TestFixPipeline(javaParser, testFixer, defects4j, maxLlmRepromptsForFixingTests);
        this.testEvaluator = new TestEvaluator(defects4j);
        this.coverageFeedbackLoop = new CoverageFeedbackLoop(
                testFixPipeline,
                defects4j,
                javaOptions,
                llmClient,
                metadata,
                feedbackIterations
        );
        this.mutationFeedbackLoop = new MutationFeedbackLoop(
                testFixPipeline,
                defects4j,
                javaOptions,
                llmClient,
                metadata,
                feedbackIterations
        );
    }

    public boolean run() throws IOException {
        synchronized (this) {
            if (hasBeenRun) {
                throw new IllegalStateException("Already run");
            }
            hasBeenRun = true;
        }

        logger.trace("Running with metadata: {}", metadata);

        final var buggyWorkspace = defects4j.checkout(project, new Defects4J.VersionId.Buggy(bugId));
        final var fixedWorkspace = defects4j.checkout(project, new Defects4J.VersionId.Fixed(bugId));
        final var cleanCompileResult = defects4j.compile(fixedWorkspace);
        if (cleanCompileResult.exitCode != 0) {
            logger.error("Compile failed:\n{}", cleanCompileResult.stdout);
            throw new IllegalStateException("Freshly checked out version %s-%s failed to compile".formatted(
                    project,
                    fixedWorkspace.versionId
            ));
        }

        final var modifiedClasses = defects4j.getModifiedClasses(project, bugId);
        logger.trace("Modified classes (targets):");
        modifiedClasses.forEach(it -> logger.trace("- {}", it));

        final var relativeSourcesRootDir = defects4j.getSourcesRootDir(fixedWorkspace);
        final var sourcesRoot = fixedWorkspace.getHostPath().resolve(relativeSourcesRootDir);
        final var relativeTestSourcesRootDir = defects4j.getTestSourcesRootDir(fixedWorkspace);
        final var testSourcesRoot = fixedWorkspace.getHostPath().resolve(relativeTestSourcesRootDir);

        logger.trace("Sources root: {}", relativeSourcesRootDir);
        logger.trace("Test sources root: {}", relativeTestSourcesRootDir);

        for (final var fullyQualifiedSourceClassName : modifiedClasses) {
            final var target = new Target(run, fullyQualifiedSourceClassName);
            target.save();
            final var initIteration = new Iteration(target);
            initIteration.save();

            final var classUnderTest = ClassUnderTest.read(fullyQualifiedSourceClassName, sourcesRoot);
            final var testClassInfo = TestClassInfo.from(classUnderTest, testSourcesRoot, "GenTest");

            logger.info(
                    "Generating initial test class for {}...",
                    classUnderTest.fullyQualifiedName
            );

            final var generatedTestCode = testGenerator.generateInitialTestClass(classUnderTest.code, initIteration);

            final var pipelineResult = testFixPipeline.execute(
                    classUnderTest,
                    testClassInfo.generated(generatedTestCode),
                    fixedWorkspace,
                    initIteration
            );

            final var validTest = pipelineResult.validTest();
            logger.trace(
                    "Generated valid test class {} using {}/{} reprompts",
                    validTest.info.fullyQualifiedName,
                    pipelineResult.repromptCount(),
                    maxLlmRepromptsForFixingTests
            );

            final var evalResult = testEvaluator.evaluate(classUnderTest, validTest, fixedWorkspace, buggyWorkspace);

            final var evaluation = Evaluation.from(evalResult);
            evaluation.save();
            initIteration.setEvaluation(evaluation);
            initIteration.setCompleted();
            initIteration.save();

            final Optional<List<CoverageResult>> covResults = runFeedbackLoop(
                    target,
                    initIteration,
                    FeedbackLoop.Type.COVERAGE,
                    feedbackLoop -> this.coverageFeedbackLoop.run(
                            feedbackLoop,
                            classUnderTest,
                            validTest,
                            fixedWorkspace,
                            buggyWorkspace
                    )
            );

            // Reuse the initially generated and fixed (valid) test
            validTest.write();

            final Optional<List<MutationTestResult>> mutResults = runFeedbackLoop(
                    target,
                    initIteration,
                    FeedbackLoop.Type.MUTATION,
                    feedbackLoop -> this.mutationFeedbackLoop.run(
                            feedbackLoop,
                            classUnderTest,
                            validTest,
                            fixedWorkspace,
                            buggyWorkspace
                    )
            );

            if (covResults.isEmpty() || mutResults.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Nonnull
    private <R> Optional<R> runFeedbackLoop(
            @Nonnull Target target,
            @Nonnull Iteration initialIteration,
            @Nonnull FeedbackLoop.Type type,
            @Nonnull ThrowingFunction<FeedbackLoop, R, ? extends Exception> callable
    ) {
        final var feedbackLoop = new FeedbackLoop(target, type);
        feedbackLoop.save();
        new FeedbackLoopIteration(feedbackLoop, initialIteration).save();
        try {
            final var result = callable.apply(feedbackLoop);
            feedbackLoop.setSuccess(true);
            return Optional.of(result);
        } catch (Exception ex) {
            logger.error("Feedback loop ({}) failed", feedbackLoop.getType().name(), ex);
            feedbackLoop.setErrorStackTrace(formatStackTrace(ex));
            return Optional.empty();
        } finally {
            feedbackLoop.setCompleted();
            feedbackLoop.save();
        }
    }
}
