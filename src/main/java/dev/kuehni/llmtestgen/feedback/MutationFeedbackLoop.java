package dev.kuehni.llmtestgen.feedback;

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
import dev.kuehni.llmtestgen.major.MutantResult;
import dev.kuehni.llmtestgen.major.MutationTestResult;
import dev.kuehni.llmtestgen.models.*;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static dev.kuehni.llmtestgen.util.text.TextUtils.formatPercentagePoints;

public class MutationFeedbackLoop {

    private static final Logger logger = LoggerFactory.getLogger(MutationFeedbackLoop.class);


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


    public MutationFeedbackLoop(
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
    public List<MutationTestResult> run(
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

        final var results = new ArrayList<MutationTestResult>();

        var currentTest = validTest;
        var currentMutationTestResult = testEvaluator.mutationTesting(classUnderTest, currentTest, fixedWorkspace);
        results.add(currentMutationTestResult);

        for (var iterationNumber = 0; iterationNumber < iterations; iterationNumber++) {
            logger.info(
                    "Running iteration {}/{} of mutation testing feedback loop...",
                    iterationNumber + 1,
                    iterations
            );

            final var prompt = new Prompt(Prompt.Motive.FEEDBACK);
            prompt.save();
            final var iteration = new Iteration(feedbackLoop.getTarget(), iterationNumber + 1);
            iteration.setGenerationPrompt(prompt);
            iteration.save();
            new FeedbackLoopIteration(feedbackLoop, iteration).save();

            final var survivingMutants = currentMutationTestResult.surviving()
                    .filter(it -> classUnderTest.fullyQualifiedName.equals(it.mutant().className()))
                    .toList();

            logger.info("Requesting new test based on mutation test result...");
            final var generatedTest = requestNewTest(classUnderTest, currentTest, survivingMutants, prompt);
            currentTest = testFixPipeline.execute(classUnderTest, generatedTest, fixedWorkspace, iteration).validTest();

            final var evalResult = testEvaluator.evaluate(classUnderTest, currentTest, fixedWorkspace, buggyWorkspace);
            currentMutationTestResult = evalResult.mutationTestResult();

            final var evaluation = Evaluation.from(evalResult);
            evaluation.save();
            iteration.setEvaluation(evaluation);
            iteration.setCompleted();
            iteration.save();

            final var lastScore = results.getLast().rawMutationScore();
            final var newScore = currentMutationTestResult.rawMutationScore();

            logger.info("Raw mutation score difference to last run: {}", formatPercentagePoints(newScore - lastScore));

            results.add(currentMutationTestResult);
        }

        logger.info("Completed {} iterations of mutation feedback", iterations);
        return results;
    }

    @Nonnull
    private GeneratedTestClass requestNewTest(
            @Nonnull ClassUnderTest classUnderTest,
            @Nonnull ValidTestClass validTest,
            @Nonnull List<MutantResult> survivingMutants,
            @Nonnull Prompt prompt
    ) {
        final var metadata = this.metadata.withNewPromptId();

        final var mutationReport = mutationReportForPrompt(survivingMutants);

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
                        The associated test class has been evaluated using mutation testing.
                        These are the surviving mutants:
                        
                        %s""".formatted(mutationReport),
                """
                        Update the following test class to detect these mutations.
                        
                        ```
                        %s
                        ```""".formatted(validTest.code)
        );
        return validTest.info.generated(output.extractCode());
    }

    @Nonnull
    private static StringBuilder mutationReportForPrompt(@Nonnull List<MutantResult> survivingMutants) {
        final var mutationReport = new StringBuilder();
        final Consumer<MutantResult> addToMutationReport = mutantResult -> {
            final var mutant = mutantResult.mutant();
            final var mutantState = mutantResult.state();

            mutationReport.append("- ");

            final var methodSignature = mutant.methodSignature();
            if (methodSignature != null) {
                mutationReport.append("In `").append(methodSignature).append("`, on line ");
            } else {
                mutationReport.append("On line ");
            }

            mutationReport.append(mutant.lineNumber()).append(" (");

            if (mutantState.isUncovered()) {
                mutationReport.append("uncovered");
            } else {
                mutationReport.append("covered but undetected");
            }

            mutationReport.append("): `")
                    .append(mutant.originalExpression())
                    .append("` |==> `")
                    .append(mutant.mutatedExpression())
                    .append("`\n");
        };

        survivingMutants.stream().filter(res -> res.state().isUncovered()).forEach(addToMutationReport);
        survivingMutants.stream().filter(res -> res.state().isCovered()).forEach(addToMutationReport);

        return mutationReport;
    }
}
