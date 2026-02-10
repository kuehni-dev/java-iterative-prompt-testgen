package dev.kuehni.llmtestgen.cobertura;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Objects;

public record CoverageResult(
        @Nonnull List<Method> methods,
        double lineRate,
        double branchRate
) {
    public CoverageResult {
        Objects.requireNonNull(methods, "methods");
    }

    public record Method(
            @Nonnull String name,
            @Nonnull String signature,
            boolean constructor,
            @Nonnull List<Line> lines,
            double lineRate,
            double branchRate
    ) {
        public Method {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(signature, "signature");
            Objects.requireNonNull(lines, "lines");
        }

        public boolean isNotFullyCovered() {
            return lineRate < 1 || branchRate < 1;
        }
    }

    public record Line(
            int number,
            int hits,
            @Nullable String conditionCoverage // "<rate>% (<covered_branches>/<branches>)"
    ) {
        public boolean isMissed() {
            return hits <= 0;
        }
    }
}
