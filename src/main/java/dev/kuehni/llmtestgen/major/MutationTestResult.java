package dev.kuehni.llmtestgen.major;

import jakarta.annotation.Nonnull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public record MutationTestResult(
        @Nonnull List<MutantResult> mutantResults,
        long mutantCount,
        long coveredCount,
        long killedCount
) {

    public MutationTestResult(
            @Nonnull List<MutantResult> mutantResults,
            long mutantCount,
            long coveredCount,
            long killedCount
    ) {
        this.mutantResults = Collections.unmodifiableList(Objects.requireNonNull(mutantResults, "mutantResults"));
        this.mutantCount = mutantCount;
        this.coveredCount = coveredCount;
        this.killedCount = killedCount;
    }

    @Nonnull
    public static MutationTestResult from(@Nonnull List<MutantResult> mutantResults) {
        final var mutantCount = mutantResults.size();
        final var coveredCount = mutantResults.stream().map(MutantResult::state).filter(MutantState::isCovered).count();
        final var killedCount = mutantResults.stream().map(MutantResult::state).filter(MutantState::isKilled).count();

        return new MutationTestResult(
                mutantResults,
                mutantCount,
                coveredCount,
                killedCount
        );
    }

    @Nonnull
    public Stream<MutantResult> surviving() {
        return mutantResults.stream().filter(it -> it.state().isSurviving());
    }

    public double mutationScore() {
        return killedCount * 1f / coveredCount;
    }

    public double rawMutationScore() {
        return killedCount * 1f / mutantCount;
    }
}
