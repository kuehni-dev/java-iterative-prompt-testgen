package dev.kuehni.llmtestgen.models;

import dev.kuehni.llmtestgen.d4j.TestEvaluator;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table
public class Evaluation extends BaseModel {

    private long mutants;

    private long coveredMutants;

    private long killedMutants;

    private double lineCoverage;

    private double branchCoverage;

    @Enumerated(EnumType.STRING)
    @Nullable
    private RealBugResult realBugResult;


    protected Evaluation() {}

    public Evaluation(
            long mutants,
            long coveredMutants,
            long killedMutants,
            double lineCoverage,
            double branchCoverage,
            @Nullable RealBugResult realBugResult
    ) {
        this.mutants = mutants;
        this.coveredMutants = coveredMutants;
        this.killedMutants = killedMutants;
        this.lineCoverage = lineCoverage;
        this.branchCoverage = branchCoverage;
        this.realBugResult = realBugResult;
    }

    @Nonnull
    public static Evaluation from(@Nonnull TestEvaluator.Result evaluationResult) {
        final var mutationTestResult = evaluationResult.mutationTestResult();
        final var coverageResult = evaluationResult.coverageResult();

        return new Evaluation(
                mutationTestResult.mutantCount(),
                mutationTestResult.coveredCount(),
                mutationTestResult.killedCount(),
                coverageResult.lineRate(),
                coverageResult.branchRate(),
                evaluationResult.realBugResult()
        );
    }


    public long getMutants() {
        return mutants;
    }

    public long getCoveredMutants() {
        return coveredMutants;
    }

    public long getKilledMutants() {
        return killedMutants;
    }

    public double getLineCoverage() {
        return lineCoverage;
    }

    public double getBranchCoverage() {
        return branchCoverage;
    }

    @Nullable
    public RealBugResult getRealBugResult() {
        return realBugResult;
    }

    public enum RealBugResult {
        NOT_APPLICABLE,
        DETECTED,
        UNDETECTED
    }

}
