package dev.kuehni.llmtestgen.feedback;

import dev.kuehni.llmtestgen.cobertura.CoverageResult;
import dev.kuehni.llmtestgen.d4j.Defects4J;
import dev.kuehni.llmtestgen.d4j.TestEvaluator;
import dev.kuehni.llmtestgen.dto.ClassUnderTest;
import dev.kuehni.llmtestgen.dto.GeneratedTestClass;
import dev.kuehni.llmtestgen.dto.JavaOptions;
import dev.kuehni.llmtestgen.fixer.TestFixPipeline;
import dev.kuehni.llmtestgen.fixer.ValidTestClass;
import dev.kuehni.llmtestgen.llm.LlmClient;
import dev.kuehni.llmtestgen.llm.Metadata;
import dev.kuehni.llmtestgen.llm.PromptUtils;
import dev.kuehni.llmtestgen.models.*;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import static dev.kuehni.llmtestgen.util.text.TextUtils.formatPercentagePoints;

public class CoverageFeedbackLoop {

    private static final Logger logger = LoggerFactory.getLogger(CoverageFeedbackLoop.class);


    @Nonnull
    private final TestFixPipeline testFixPipeline;

    @Nonnull
    private final JavaOptions javaOptions;

    @Nonnull
    private final LlmClient llmClient;

    @Nonnull
    private final TestEvaluator testEvaluator;

    @Nonnull
    private final Metadata metadata;

    private final int iterations;


    public CoverageFeedbackLoop(
            @Nonnull TestFixPipeline testFixPipeline,
            @Nonnull Defects4J defects4j,
            @Nonnull JavaOptions javaOptions,
            @Nonnull LlmClient llmClient,
            @Nonnull Metadata metadata,
            int iterations
    ) {
        Objects.requireNonNull(defects4j, "defects4j");

        this.testFixPipeline = Objects.requireNonNull(testFixPipeline, "testFixPipeline");
        this.javaOptions = Objects.requireNonNull(javaOptions, "javaOptions");
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient");
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.iterations = iterations;

        testEvaluator = new TestEvaluator(defects4j);
    }

    @Nonnull
    public List<CoverageResult> run(
            @Nonnull FeedbackLoop feedbackLoop,
            @Nonnull ClassUnderTest classUnderTest,
            @Nonnull ValidTestClass validTest,
            @Nonnull Defects4J.Workspace fixedWorkspace,
            @Nonnull Defects4J.Workspace buggyWorkspace
    ) throws IOException {
        Objects.requireNonNull(feedbackLoop, "feedbackLoop");
        Objects.requireNonNull(classUnderTest, "classUnderTest");
        Objects.requireNonNull(validTest, "validTest");
        Objects.requireNonNull(fixedWorkspace, "fixedWorkspace");
        Objects.requireNonNull(buggyWorkspace, "buggyWorkspace");

        final var results = new ArrayList<CoverageResult>();

        var currentTest = validTest;
        var currentCoverageResult = testEvaluator.coverage(classUnderTest, currentTest, fixedWorkspace);
        results.add(currentCoverageResult);

        for (var iterationNumber = 0; iterationNumber < iterations; iterationNumber++) {
            logger.info(
                    "Running iteration {}/{} of coverage feedback loop...",
                    iterationNumber + 1,
                    iterations
            );

            final var prompt = new Prompt(Prompt.Motive.FEEDBACK);
            prompt.save();
            final var iteration = new Iteration(feedbackLoop.getTarget(), iterationNumber + 1);
            iteration.setGenerationPrompt(prompt);
            iteration.save();
            new FeedbackLoopIteration(feedbackLoop, iteration).save();

            logger.info("Requesting new test based on coverage result...");
            final var generatedTest = requestNewTest(classUnderTest, currentTest, currentCoverageResult, prompt);
            currentTest = testFixPipeline.execute(classUnderTest, generatedTest, fixedWorkspace, iteration).validTest();

            final var evalResult = testEvaluator.evaluate(classUnderTest, currentTest, fixedWorkspace, buggyWorkspace);
            currentCoverageResult = evalResult.coverageResult();

            final var evaluation = Evaluation.from(evalResult);
            evaluation.save();
            iteration.setEvaluation(evaluation);
            iteration.setCompleted();
            iteration.save();

            final var lastLineCoverage = results.getLast().lineRate();
            final var newLineCoverage = currentCoverageResult.lineRate();

            logger.info(
                    "Line coverage difference to last run: {}",
                    formatPercentagePoints(newLineCoverage - lastLineCoverage)
            );

            final var lastBranchCoverage = results.getLast().branchRate();
            final var newBranchCoverage = currentCoverageResult.branchRate();

            logger.info(
                    "Branch coverage difference to last run: {}",
                    formatPercentagePoints(newBranchCoverage - lastBranchCoverage)
            );

            results.add(currentCoverageResult);
        }

        logger.info("Completed {} iterations of coverage feedback", iterations);
        return results;
    }

    @Nonnull
    private GeneratedTestClass requestNewTest(
            @Nonnull ClassUnderTest classUnderTest,
            @Nonnull ValidTestClass validTest,
            @Nonnull CoverageResult coverageResult,
            @Nonnull Prompt prompt
    ) {
        final var metadata = this.metadata.withNewPromptId();

        final var coverageReport = coverageReportForPrompt(classUnderTest.simpleName, coverageResult);

        final var output = llmClient.ask(
                metadata,
                prompt,
                PromptUtils.instructionsForJavaOnlyResponse(javaOptions),
                """
                        This is a java class that should be tested:
                        
                        ```
                        %s
                        ```""".formatted(classUnderTest.code),
                """
                        Code coverage has been measured for the associated test class.
                        These are the uncovered parts:
                        
                        %s""".formatted(coverageReport),
                """
                        Update the following test class to cover the uncovered parts.
                        
                        ```
                        %s
                        ```""".formatted(validTest.code)
        );

        return validTest.info.generated(output.extractCode());
    }

    @Nonnull
    private static StringBuilder coverageReportForPrompt(
            @Nonnull String simpleClassName,
            @Nonnull CoverageResult coverageResult
    ) {
        final var coverageReport = new StringBuilder();
        coverageReport.append("Total line coverage: ").append(coverageResult.lineRate()).append("\n");
        coverageReport.append("Total branch coverage: ").append(coverageResult.branchRate()).append("\n");
        coverageReport.append("\n");

        coverageResult.methods().stream().filter(CoverageResult.Method::isNotFullyCovered).forEach(method -> {
            if (method.constructor()) {
                coverageReport.append("Constructor: `").append(simpleClassName);
            } else {
                coverageReport.append("Method: `").append(method.name());
            }
            coverageReport.append(method.signature()).append("`:\n");
            coverageReport.append("- Missed lines: ");

            final var lineJoiner = new StringJoiner(", ");
            method.lines()
                    .stream()
                    .filter(CoverageResult.Line::isMissed)
                    .map(CoverageResult.Line::number)
                    .map(String::valueOf)
                    .forEach(lineJoiner::add);
            coverageReport.append(lineJoiner);

            coverageReport.append("\n- Partial branches:\n");

            method.lines().forEach(line -> {
                final var conditionCoverage = line.conditionCoverage();
                if (conditionCoverage == null) {
                    return; // not a branch
                }
                if (conditionCoverage.startsWith("100%")) {
                    return; // covered
                }
                coverageReport.append("  - Line ")
                        .append(line.number())
                        .append(": ")
                        .append(conditionCoverage)
                        .append("\n");
            });
            coverageReport.append("\n");
        });

        return coverageReport;
    }
}
