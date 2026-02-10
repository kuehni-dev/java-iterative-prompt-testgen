package dev.kuehni.llmtestgen.models;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table
public class Prompt extends BaseModel {

    @Nullable
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Motive motive;

    @Nullable
    private Long inputTokens;

    @Nullable
    private Long outputTokens;

    @Nullable
    private Long reasoningTokens;

    @Column(updatable = false, nullable = false)
    private Timestamp createdAt;

    @Nullable
    private Timestamp completedAt;

    protected Prompt() {}

    public Prompt(@Nonnull Motive motive) {
        this.motive = Objects.requireNonNull(motive, "motive");
        createdAt = new Timestamp(System.currentTimeMillis());
    }

    @Nullable
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(@Nullable UUID uuid) {
        this.uuid = uuid;
    }

    @Nonnull
    public Motive getMotive() {
        return Objects.requireNonNull(motive, "motive");
    }

    @Nullable
    public Long getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(@Nullable Long inputTokens) {
        this.inputTokens = inputTokens;
    }

    @Nullable
    public Long getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(@Nullable Long outputTokens) {
        this.outputTokens = outputTokens;
    }

    @Nullable
    public Long getReasoningTokens() {
        return reasoningTokens;
    }

    public void setReasoningTokens(@Nullable Long reasoningTokens) {
        this.reasoningTokens = reasoningTokens;
    }

    @Nonnull
    public Timestamp getCreatedAt() {
        return Objects.requireNonNull(createdAt, "createdAt");
    }

    @Nullable
    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(@Nullable Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    public void setCompleted() {
        setCompletedAt(new Timestamp(System.currentTimeMillis()));
    }

    public enum Motive {
        INITIAL,
        FIX,
        FEEDBACK,
    }

}
