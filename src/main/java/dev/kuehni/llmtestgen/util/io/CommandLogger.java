package dev.kuehni.llmtestgen.util.io;

import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

class CommandLogger {

    private CommandLogger() {}

    /**
     * Quotes command argument if necessary. Uses POSIX-style.
     */
    @Nonnull
    static String quoteArgIfNeeded(@Nonnull String arg) {
        Objects.requireNonNull(arg, "arg");

        if (arg.isEmpty()) {
            return "''";
        }

        if (arg.matches("[a-zA-Z0-9._/\\-\\\\]+")) {
            return arg;
        }

        return "'" + arg.replace("'", "'\"'\"'") + "'";
    }

    @Nonnull
    static String format(@Nonnull String[] command) {
        return Arrays.stream(command).map(CommandLogger::quoteArgIfNeeded).collect(Collectors.joining(" "));
    }
}
