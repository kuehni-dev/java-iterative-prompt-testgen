package dev.kuehni.llmtestgen.d4j;

import dev.kuehni.llmtestgen.cobertura.CoverageResult;
import dev.kuehni.llmtestgen.dto.ClassUnderTest;
import dev.kuehni.llmtestgen.fixer.ValidTestClass;
import dev.kuehni.llmtestgen.major.MutationTestResult;
import dev.kuehni.llmtestgen.models.Evaluation;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static dev.kuehni.llmtestgen.util.text.TextUtils.formatPercentage;

public class TestEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(TestEvaluator.class);

    @Nonnull
    private final Defects4J defects4j;

    public TestEvaluator(@Nonnull Defects4J defects4j) {
        this.defects4j = Objects.requireNonNull(defects4j, "defects4j");
    }

    @Nonnull
    public Result evaluate(
            @Nonnull ClassUnderTest classUnderTest,
            @Nonnull ValidTestClass validTest,
            @Nonnull Defects4J.Workspace fixedWorkspace,
            @Nonnull Defects4J.Workspace buggyWorkspace
    ) throws IOException {
        Objects.requireNonNull(validTest, "validTest");
        Objects.requireNonNull(fixedWorkspace, "fixedWorkspace");
        Objects.requireNonNull(buggyWorkspace, "buggyWorkspace");

        final var realBugResult = testAgainstRealBug(classUnderTest, validTest, fixedWorkspace, buggyWorkspace);
        final var mutationTestResult = mutationTesting(classUnderTest, validTest, fixedWorkspace);
        final var coverageResult = coverage(classUnderTest, validTest, fixedWorkspace);

        return new Result(realBugResult, mutationTestResult, coverageResult);
    }

    @Nonnull
    public Evaluation.RealBugResult testAgainstRealBug(
            @Nonnull ClassUnderTest classUnderTest,
            @Nonnull ValidTestClass validTest,
            @Nonnull Defects4J.Workspace fixedWorkspace,
            @Nonnull Defects4J.Workspace buggyWorkspace
    ) throws IOException {
        Objects.requireNonNull(classUnderTest, "classUnderTest");
        Objects.requireNonNull(validTest, "validTest");
        Objects.requireNonNull(fixedWorkspace, "fixedWorkspace");
        Objects.requireNonNull(buggyWorkspace, "buggyWorkspace");

        logger.trace("Injecting real bug...");

        final var buggyPath = buggyWorkspace.getHostPath()
                .resolve(fixedWorkspace.getHostPath().relativize(classUnderTest.sourcesRoot))
                .resolve(classUnderTest.pathRelativeToSourcesRoot);
        final var fixedPath = fixedWorkspace.getHostPath()
                .resolve(classUnderTest.sourcesRoot)
                .resolve(classUnderTest.pathRelativeToSourcesRoot);

        final var buggyCode = Files.readString(buggyPath);
        final var fixedCode = Files.readString(fixedPath);

        // Inject buggy code
        Files.writeString(fixedPath, buggyCode);

        Evaluation.RealBugResult result;
        try {
            final var testResult =
                    defects4j.test(fixedWorkspace, validTest.info.fullyQualifiedName, validTest.testMethodNames());

            if (testResult.isSuccess()) {
                logger.trace("Real bug remains UNDETECTED");
                result = Evaluation.RealBugResult.UNDETECTED;
            } else {
                logger.trace("Real bug has been detected");
                result = Evaluation.RealBugResult.DETECTED;
            }
        } catch (Exception ex) {
            logger.warn("Failed to test against real bug", ex);
            result = Evaluation.RealBugResult.NOT_APPLICABLE;
        }

        // Reset to fixed code
        Files.writeString(fixedPath, fixedCode);

        return result;
    }

    @Nonnull
    public MutationTestResult mutationTesting(
            @Nonnull ClassUnderTest classUnderTest,
            @Nonnull ValidTestClass validTest,
            @Nonnull Defects4J.Workspace workspace
    ) {
        Objects.requireNonNull(classUnderTest, "classUnderTest");
        Objects.requireNonNull(validTest, "validTest");
        Objects.requireNonNull(workspace, "workspace");

        logger.trace("Running mutation test...");
        final var mutationTestResult = defects4j.mutationTest(
                workspace,
                classUnderTest.fullyQualifiedName,
                validTest.info.fullyQualifiedName,
                validTest.testMethodNames()
        );
        logger.trace(
                "Mutation score: {} ({})",
                formatPercentage(mutationTestResult.mutationScore()),
                formatPercentage(mutationTestResult.rawMutationScore())
        );

        return mutationTestResult;
    }

    @Nonnull
    public CoverageResult coverage(
            @Nonnull ClassUnderTest classUnderTest,
            @Nonnull ValidTestClass validTest,
            @Nonnull Defects4J.Workspace workspace
    ) {
        Objects.requireNonNull(classUnderTest, "classUnderTest");
        Objects.requireNonNull(validTest, "validTest");
        Objects.requireNonNull(workspace, "workspace");

        logger.trace("Running coverage...");
        final var coverageResult = defects4j.coverage(
                workspace,
                classUnderTest.fullyQualifiedName,
                validTest.info.fullyQualifiedName,
                validTest.testMethodNames()
        );
        logger.trace(
                "Line coverage: {}",
                formatPercentage(coverageResult.lineRate())
        );
        logger.trace(
                "Branch coverage: {}",
                formatPercentage(coverageResult.branchRate())
        );

        return coverageResult;
    }

    public record Result(
            @Nonnull Evaluation.RealBugResult realBugResult, @Nonnull MutationTestResult mutationTestResult,
            @Nonnull CoverageResult coverageResult
    ) {
        public Result {
            Objects.requireNonNull(realBugResult, "realBugResult");
            Objects.requireNonNull(mutationTestResult, "mutationTestResult");
            Objects.requireNonNull(coverageResult, "coverageResult");
        }
    }
}
