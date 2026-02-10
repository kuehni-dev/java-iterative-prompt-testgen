package dev.kuehni.llmtestgen.major;

import jakarta.annotation.Nonnull;

import java.util.Objects;

public record MutantResult(
        @Nonnull Mutant mutant,
        @Nonnull MutantState state
) {
    public MutantResult {
        Objects.requireNonNull(mutant);
        Objects.requireNonNull(state);
    }
}
