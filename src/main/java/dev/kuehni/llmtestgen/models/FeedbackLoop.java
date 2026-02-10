package dev.kuehni.llmtestgen.models;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table
public class FeedbackLoop extends BaseModel {

    @ManyToOne
    @JoinColumn(nullable = false)
    private Target target;

    // type == null means that this is still in the initial generation phase
    @Enumerated(EnumType.STRING)
    @Nullable
    private Type type;

    @Column(columnDefinition = "text")
    @Nullable
    private String errorStackTrace;

    @Column(nullable = false)
    private boolean success;

    @Column(updatable = false, nullable = false)
    private Timestamp createdAt;

    @Nullable
    private Timestamp completedAt;

    protected FeedbackLoop() {}

    public FeedbackLoop(@Nonnull Target target, @Nonnull Type type) {
        this.target = Objects.requireNonNull(target, "run");
        this.type = Objects.requireNonNull(type, "type");
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    @Nonnull
    public Target getTarget() {
        return Objects.requireNonNull(target, "target");
    }

    @Nonnull
    public Type getType() {
        return Objects.requireNonNull(type, "type");
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

    public enum Type {
        COVERAGE,
        MUTATION
    }

}
