package dev.kuehni.llmtestgen.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.Objects;

@Entity
public class FeedbackLoopIteration extends BaseModel {

    @ManyToOne
    @JoinColumn(updatable = false, nullable = false)
    private FeedbackLoop feedbackLoop;

    @ManyToOne
    @JoinColumn(updatable = false, nullable = false)
    private Iteration iteration;

    protected FeedbackLoopIteration() {}

    public FeedbackLoopIteration(
            @Nonnull FeedbackLoop feedbackLoop,
            @Nonnull Iteration iteration
    ) {
        this.feedbackLoop = Objects.requireNonNull(feedbackLoop, "feedbackLoop");
        this.iteration = Objects.requireNonNull(iteration, "iteration");
    }

    @Nonnull
    public FeedbackLoop getFeedbackLoop() {
        return Objects.requireNonNull(feedbackLoop, "feedbackLoop");
    }

    @Nonnull
    public Iteration getIteration() {
        return Objects.requireNonNull(iteration, "iteration");
    }

}
