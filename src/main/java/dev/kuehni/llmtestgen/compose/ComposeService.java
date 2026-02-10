package dev.kuehni.llmtestgen.compose;

import dev.kuehni.llmtestgen.util.env.Env;
import dev.kuehni.llmtestgen.util.io.CommandUtils;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static dev.kuehni.llmtestgen.util.io.CommandUtils.runCommandSync;
import static dev.kuehni.llmtestgen.util.io.CommandUtils.runCommandSyncGetStdout;

public class ComposeService implements AutoCloseable {
    private static final String COMPOSE_PROGRAM = Env.composeProgram();
    private static final Logger logger = LoggerFactory.getLogger(ComposeService.class);

    @Nonnull
    public final Path directory;

    @Nonnull
    public final String name;

    private boolean closed;

    private ComposeService(@Nonnull Path directory, @Nonnull String name) {
        this.directory = Objects.requireNonNull(directory, "directory");
        this.name = Objects.requireNonNull(name, "name");
    }

    @Nonnull
    public static ComposeService up(@Nonnull Path directory, @Nonnull String name) {
        Objects.requireNonNull(directory, "directory");
        Objects.requireNonNull(name, "name");

        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("%s is not a directory".formatted(directory));
        }

        logger.info("Starting compose stack...");
        final var exitCode = runCommandSync(directory, COMPOSE_PROGRAM, "up", "-d", "--build");
        if (exitCode != 0) {
            throw new IllegalStateException("%s up exited with code: %d".formatted(COMPOSE_PROGRAM, exitCode));
        }
        return new ComposeService(directory, name);
    }

    private void verifyNotClosed() {
        if (closed) {
            throw new IllegalStateException("service is down");
        }
    }

    public int execInheritIo(@Nonnull String... command) {
        Objects.requireNonNull(command, "command");
        verifyNotClosed();

        return runCommandSync(directory, composeCommand("exec", command));
    }

    public CommandUtils.ExitCodeAndStdout execCaptureStdout(@Nonnull String... command) {
        Objects.requireNonNull(command, "command");
        verifyNotClosed();

        return runCommandSyncGetStdout(directory, composeCommand("exec", command));
    }

    @Nonnull
    private String[] composeCommand(@Nonnull String subCommand, @Nonnull String... arguments) {
        Objects.requireNonNull(subCommand, "subCommand");
        Objects.requireNonNull(arguments, "arguments");

        final String[] composeCommand = new String[arguments.length + 3];
        composeCommand[0] = COMPOSE_PROGRAM;
        composeCommand[1] = subCommand;
        composeCommand[2] = name;

        System.arraycopy(arguments, 0, composeCommand, 3, arguments.length);

        return composeCommand;
    }

    @Override
    public void close() {
        closed = true;

        logger.info("Tearing down compose stack...");
        final var result = runCommandSyncGetStdout(directory, COMPOSE_PROGRAM, "down");
        if (result.exitCode != 0) {
            System.out.println(result.stdout);
            throw new IllegalStateException("compose down exited with code: " + result.exitCode);
        }
    }
}
