package dev.kuehni.llmtestgen.models;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table
public class Iteration extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "target_id", nullable = false)
    private Target target;

    @JoinColumn(nullable = false)
    private int number;

    @ManyToOne
    @JoinColumn(name = "generation_prompt_id")
    @Nullable
    private Prompt generationPrompt;

    @ManyToOne
    @JoinColumn(name = "evaluation_id")
    @Nullable
    private Evaluation evaluation;

    @Column(updatable = false, nullable = false)
    private Timestamp createdAt;

    @Nullable
    private Timestamp completedAt;


    public Iteration(@Nonnull Target target, int number) {
        this.target = Objects.requireNonNull(target, "target");
        this.number = number;
        createdAt = new Timestamp(System.currentTimeMillis());
    }

    public Iteration(@Nonnull Target target) {
        this(target, 0);
    }

    protected Iteration() {}


    @Nonnull
    public Target getTarget() {
        return Objects.requireNonNull(target, "target");
    }

    public int getNumber() {
        return number;
    }

    @Nullable
    public Prompt getGenerationPrompt() {
        return generationPrompt;
    }

    public void setGenerationPrompt(@Nullable Prompt prompt) {
        this.generationPrompt = prompt;
    }

    @Nullable
    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(@Nullable Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    @Nonnull
    public Timestamp getCreatedAt() {
        return Objects.requireNonNull(createdAt, "createdAt");
    }

    public void setCompletedAt(@Nullable Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    public void setCompleted() {
        setCompletedAt(new Timestamp(System.currentTimeMillis()));
    }
}
