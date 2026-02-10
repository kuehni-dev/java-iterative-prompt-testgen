package dev.kuehni.llmtestgen.llm;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

public record Metadata(@Nonnull UUID runId, @Nullable UUID promptId) {

    public Metadata {
        Objects.requireNonNull(runId, "runId");
    }

    public Metadata(@Nonnull UUID runId) {
        this(runId, null);
    }

    @Nonnull
    public Metadata withPromptId(@Nonnull UUID promptId) {
        return new Metadata(runId, Objects.requireNonNull(promptId, "promptId"));
    }

    @Nonnull
    public Metadata withNewPromptId() {
        return withPromptId(UuidCreator.getTimeOrderedEpoch());
    }


    @Nonnull
    public Path getOrCreateOutputDirectory() throws IOException {
        final var runDir = Path.of("data", "prompts", "run-" + runId);
        final var dir = promptId != null ? runDir.resolve("prompt-" + promptId) : runDir;

        Files.createDirectories(dir);

        return dir;
    }
}
