package dev.kuehni.llmtestgen.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table
public class PromptMessage extends BaseModel {

    @ManyToOne
    @JoinColumn(nullable = false)
    private Prompt prompt;

    @Column(columnDefinition = "text", nullable = false)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected PromptMessage() {}

    public PromptMessage(@Nonnull Prompt prompt, @Nonnull String text, @Nonnull Role role) {
        this.prompt = Objects.requireNonNull(prompt, "prompt");
        this.text = Objects.requireNonNull(text, "text");
        this.role = Objects.requireNonNull(role, "role");
    }

    @Nonnull
    public Prompt getPrompt() {
        return Objects.requireNonNull(prompt, "prompt");
    }

    @Nonnull
    public String getText() {
        return Objects.requireNonNull(text, "text");
    }

    @Nonnull
    public Role getRole() {
        return Objects.requireNonNull(role, "role");
    }

    public enum Role {
        SYSTEM,
        USER,
        ASSISTANT
    }

}
