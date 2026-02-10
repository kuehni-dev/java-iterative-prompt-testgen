package dev.kuehni.llmtestgen.models;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table
public class FixerStage extends BaseModel {

    @ManyToOne
    @JoinColumn(nullable = false)
    private Iteration iteration;

    @Column(nullable = false)
    private int number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private boolean success;

    @OneToOne
    @JoinColumn
    @Nullable
    private Prompt followUpPrompt;

    @Column(columnDefinition = "text")
    @Nullable
    private String errorMessage;

    @Column(updatable = false, nullable = false)
    private Timestamp createdAt;

    @Nullable
    private Timestamp completedAt;


    protected FixerStage() {}

    public FixerStage(@Nonnull Iteration iteration, @Nonnull Type type) {
        this.iteration = Objects.requireNonNull(iteration, "iteration");
        this.type = Objects.requireNonNull(type, "type");
        createdAt = new Timestamp(System.currentTimeMillis());
    }

    @Nonnull
    public Iteration getIteration() {
        return Objects.requireNonNull(iteration, "iteration");
    }

    public void setIteration(@Nonnull Iteration iteration) {
        this.iteration = Objects.requireNonNull(iteration, "iteration");
    }

    public int getNumber() {
        return number;
    }

    @Nonnull
    public Type getStage() {
        return Objects.requireNonNull(type, "type");
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Nonnull
    public Type getType() {
        return Objects.requireNonNull(type, "type");
    }

    @Nullable
    public Prompt getFollowUpPrompt() {
        return followUpPrompt;
    }

    public void setFollowUpPrompt(@Nullable Prompt prompt) {
        this.followUpPrompt = prompt;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;
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
        PARSE,
        COMPILE,
        TEST,
    }

}
