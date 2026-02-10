package dev.kuehni.llmtestgen.models;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table
public class Run extends BaseModel {

    @Column(nullable = false)
    private UUID uuid;

    @Column(nullable = false)
    private String d4jProject;

    @Column(nullable = false)
    private int d4jBugId;

    @Column(nullable = false)
    private String model;

    @Nullable
    private String reasoningLevel;

    @Column(nullable = false)
    private int maxFixReprompts;

    @Column(nullable = false)
    private int feedbackIterations;

    @Column(columnDefinition = "text")
    @Nullable
    private String errorStackTrace;

    @Column(nullable = false)
    protected boolean success;

    @Column(updatable = false, nullable = false)
    private Timestamp createdAt;

    @Nullable
    private Timestamp completedAt;

    protected Run() {}

    public Run(
            @Nonnull UUID uuid,
            @Nonnull String d4jProject,
            int d4jBugId,
            @Nonnull String model,
            @Nullable String reasoningLevel,
            int maxFixReprompts,
            int feedbackIterations
    ) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.d4jProject = Objects.requireNonNull(d4jProject, "d4jProject");
        this.d4jBugId = d4jBugId;
        this.model = Objects.requireNonNull(model, "model");
        this.reasoningLevel = reasoningLevel;
        this.maxFixReprompts = maxFixReprompts;
        this.feedbackIterations = feedbackIterations;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    @Nonnull
    public UUID getUuid() {
        return Objects.requireNonNull(uuid, "uuid");
    }

    @Nonnull
    public String getD4jProject() {
        return Objects.requireNonNull(d4jProject, "d4jProject");
    }

    public int getD4jBugId() {
        return d4jBugId;
    }

    @Nonnull
    public String getModel() {
        return Objects.requireNonNull(model, "model");
    }

    @Nullable
    public String getReasoningLevel() {
        return reasoningLevel;
    }

    public int getMaxFixReprompts() {
        return maxFixReprompts;
    }

    public int getFeedbackIterations() {
        return feedbackIterations;
    }

    @Nullable
    public String getErrorStackTrace() {
        return errorStackTrace;
    }

    public void setErrorStackTrace(@Nullable String errorStackTrace) {
        this.errorStackTrace = errorStackTrace;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

}
