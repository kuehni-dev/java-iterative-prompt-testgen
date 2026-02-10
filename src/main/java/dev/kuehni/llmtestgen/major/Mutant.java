package dev.kuehni.llmtestgen.major;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

public record Mutant(
        @Nonnull String id,
        @Nonnull String operator,
        @Nonnull String originalSignature,
        @Nonnull String mutationStrategy,
        @Nonnull String className,
        @Nullable String methodSignature,
        @Nonnull String lineNumber,
        @Nonnull String fileOffset,
        @Nonnull String originalExpression,
        @Nonnull String mutatedExpression
) {

    public Mutant {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(originalSignature, "originalSignature");
        Objects.requireNonNull(mutationStrategy, "mutationStrategy");
        Objects.requireNonNull(className, "className");
        Objects.requireNonNull(lineNumber, "lineNumber");
        Objects.requireNonNull(fileOffset, "fileOffset");
        Objects.requireNonNull(originalExpression, "originalExpression");
        Objects.requireNonNull(mutatedExpression, "mutatedExpression");
    }


    @Nonnull
    public static Mutant parse(@Nonnull String line) {
        final var parts = line.split(":", 8);
        if (parts.length != 8) {
            throw new IllegalArgumentException("Invalid mutant format. Expected seven colons but got %d: %s".formatted(
                    parts.length,
                    line
            ));
        }

        final var id = parts[0];
        final var operator = parts[1];
        final var originalSignature = parts[2];
        final var mutationStrategy = parts[3];


        final var atIndex = parts[4].indexOf('@');
        final String className;
        final String methodSignature;
        if (atIndex == -1) {
            className = parts[4];
            methodSignature = null;
        } else {
            className = parts[4].substring(0, atIndex);
            methodSignature = parts[4].substring(atIndex + 1);
        }

        final var lineNumber = parts[5];
        final var fileOffset = parts[6];

        final var mutArrowIndex = parts[7].indexOf("|==>");
        final var originalExpression = parts[7].substring(0, mutArrowIndex).trim();
        final var mutatedExpression = parts[7].substring(mutArrowIndex + 4).trim();

        return new Mutant(
                id,
                operator,
                originalSignature,
                mutationStrategy,
                className,
                methodSignature,
                lineNumber,
                fileOffset,
                originalExpression,
                mutatedExpression
        );
    }
}
