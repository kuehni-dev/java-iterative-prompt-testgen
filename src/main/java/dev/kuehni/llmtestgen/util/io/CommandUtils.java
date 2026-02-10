package dev.kuehni.llmtestgen.util.io;

import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandUtils {

    private static final Logger logger = LoggerFactory.getLogger(CommandUtils.class);

    public static int runCommandSync(@Nonnull Path directory, @Nonnull String... command) {
        if (command.length == 0) {
            throw new IllegalArgumentException("command must not be empty");
        }

        final var processBuilder = new ProcessBuilder(command)
                .directory(directory.toFile())
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT);

        logger.atTrace().setMessage("Executing command: {}").addArgument(() -> CommandLogger.format(command)).log();
        final Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        logger.trace("{} exited with code {}", command[0], exitCode);

        return exitCode;
    }

    @Nonnull
    public static ExitCodeAndStdout runCommandSyncGetStdout(@Nonnull Path directory, @Nonnull String... command) {
        if (command.length == 0) {
            throw new IllegalArgumentException("command must not be empty");
        }

        final var processBuilder = new ProcessBuilder(command)
                .directory(directory.toFile())
                .redirectError(ProcessBuilder.Redirect.INHERIT);

        logger.atTrace().setMessage("Executing command: {}").addArgument(() -> CommandLogger.format(command)).log();
        final Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final ExitCodeAndStdout exitCodeAndStdout;
        try (final var stdoutReader = process.inputReader()) {
            final int exitCode;
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            final var stdout = stdoutReader.lines().collect(Collectors.joining(System.lineSeparator()));
            exitCodeAndStdout = new ExitCodeAndStdout(exitCode, stdout);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logger.trace("{} exited with code {}", command[0], exitCodeAndStdout.exitCode);

        return exitCodeAndStdout;
    }

    public static final class ExitCodeAndStdout {

        public final int exitCode;

        @Nonnull
        public final String stdout;

        private ExitCodeAndStdout(int exitCode, @Nonnull String stdout) {
            this.exitCode = exitCode;
            this.stdout = Objects.requireNonNull(stdout, "stdout");
        }

    }
}
