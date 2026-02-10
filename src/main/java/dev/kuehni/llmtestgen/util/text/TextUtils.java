package dev.kuehni.llmtestgen.util.text;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Objects;

public class TextUtils {
    private TextUtils() {}

    @Nonnull
    public static String truncateToFirstLine(@Nonnull String text, @Nullable String suffix) {
        Objects.requireNonNull(text, "text");

        final var truncated = text.lines().findFirst().orElse("");
        if (suffix != null && text.lines().count() > 1) {
            return truncated + suffix;
        }
        return truncated;
    }

    @Nonnull
    public static String nonBlankOr(@Nullable String text, @Nonnull String defaultText) {
        Objects.requireNonNull(defaultText, "defaultText");

        return text == null || text.isBlank() ? defaultText : text;
    }

    @Nonnull
    public static String formatDuration(@Nonnull Duration duration) {
        long ms = duration.toMillis();
        if (ms < 1000) {
            return ms + " ms";
        }
        return (ms / 1000) + " s " + (ms % 1000) + " ms";
    }

    @Nonnull
    public static String formatPercentage(double value) {
        return new DecimalFormat("0.0 %").format(value);
    }

    @Nonnull
    public static String formatPercentagePoints(double value) {
        final var format = new DecimalFormat("0.0 pp");
        format.setPositivePrefix("+");

        return format.format(value * 100);
    }

    @Nonnull
    public static String formatStackTrace(@Nonnull Throwable throwable) {
        final var stringWriter = new StringWriter();
        final var writer = new PrintWriter(stringWriter);
        throwable.printStackTrace(writer);
        return stringWriter.toString();
    }
}
