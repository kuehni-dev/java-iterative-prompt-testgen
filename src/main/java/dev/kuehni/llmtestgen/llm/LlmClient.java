package dev.kuehni.llmtestgen.llm;

import dev.kuehni.llmtestgen.models.Prompt;
import dev.kuehni.llmtestgen.models.PromptMessage;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class LlmClient {
    /**
     * @param prompt An empty prompt. This method will add instructions and user/assistant messages to it.
     */
    @Nonnull
    public final Answer ask(
            @Nonnull Metadata metadata,
            @Nonnull Prompt prompt,
            @Nonnull String systemInstructions,
            @Nonnull String firstUserMessage,
            @Nonnull String... moreUserMessages
    ) {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(prompt, "prompt");
        Objects.requireNonNull(systemInstructions, "systemInstructions");
        Objects.requireNonNull(firstUserMessage, "firstUserMessage");
        Objects.requireNonNull(moreUserMessages, "moreUserMessages");

        prompt.setUuid(metadata.promptId());
        prompt.save();

        final var userMessages = Stream.concat(Stream.of(firstUserMessage), Arrays.stream(moreUserMessages))
                .filter(it -> it != null && !it.isEmpty())
                .toList();

        new PromptMessage(prompt, systemInstructions, PromptMessage.Role.SYSTEM).save();
        userMessages.forEach(message -> new PromptMessage(prompt, message, PromptMessage.Role.USER).save());

        final var answer = ask(metadata, systemInstructions, userMessages);

        new PromptMessage(prompt, answer.content, PromptMessage.Role.ASSISTANT).save();

        prompt.setCompleted();
        prompt.setInputTokens(answer.inputTokens());
        prompt.setOutputTokens(answer.outputTokens());
        prompt.setReasoningTokens(answer.reasoningTokens());
        prompt.save();

        return answer;
    }

    protected abstract Answer ask(
            @Nonnull Metadata metadata,
            @Nonnull String systemInstructions,
            @Nonnull List<String> userMessages
    );

    @Nonnull
    public abstract String getModelName();

    @Nullable
    public abstract String getReasoningEffort();


    public record Answer(
            @Nonnull String content,
            @Nullable Long inputTokens,
            @Nullable Long outputTokens,
            @Nullable Long reasoningTokens
    ) {

        private static final Pattern CODE_PATTERN = Pattern.compile("(?s)```\\s*([\\w#+.-]+)?\\s*\\R(.*?)\\R```");

        public Answer {
            Objects.requireNonNull(content, "content");
        }

        public Answer(@Nonnull String content) {
            this(content, null, null, null);
        }

        @Nonnull
        public Answer withUsage(long inputTokens, long outputTokens, long reasoningTokens) {
            return new Answer(content, inputTokens, outputTokens, reasoningTokens);
        }

        @Nonnull
        public Answer withUsage(long inputTokens, long outputTokens) {
            return new Answer(content, inputTokens, outputTokens, null);
        }

        @Nonnull
        public String extractCode() {
            final var matcher = CODE_PATTERN.matcher(content);
            if (!matcher.find()) {
                return content;
            }

            return matcher.group(2);
        }
    }
}
