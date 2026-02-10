package dev.kuehni.llmtestgen.llm.openai;

import com.openai.core.JsonString;
import com.openai.models.Reasoning;
import com.openai.models.ReasoningEffort;
import com.openai.models.ResponsesModel;
import com.openai.models.responses.ResponseCreateParams;
import dev.kuehni.llmtestgen.llm.Metadata;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

public record OpenAIModelOptions(
        @Nonnull ResponsesModel model,
        @Nullable ReasoningEffort reasoningEffort,
        @Nullable Integer maxOutputTokens
) {
    public OpenAIModelOptions {
        Objects.requireNonNull(model, "model");
    }

    public OpenAIModelOptions(@Nonnull ResponsesModel model, @Nullable ReasoningEffort reasoningEffort) {
        this(model, reasoningEffort, null);
    }

    public OpenAIModelOptions(@Nonnull ResponsesModel model) {
        this(model, null);
    }

    @Nonnull
    public OpenAIModelOptions withMaxOutputTokens(int maxOutputTokens) {
        return new OpenAIModelOptions(model, reasoningEffort, maxOutputTokens);
    }

    @Nullable
    public Reasoning reasoning() {
        if (reasoningEffort == null) {
            return null;
        }
        return Reasoning.builder().effort(reasoningEffort).build();
    }

    @Nonnull
    public ResponseCreateParams.Builder responseCreateParamsBuilder(@Nonnull Metadata metadata) {
        Objects.requireNonNull(metadata, "metadata");

        final var builder = ResponseCreateParams.builder()
                .model(model)
                .reasoning(reasoning())
                .metadata(buildMetadata(metadata));

        if (maxOutputTokens != null) {
            builder.maxOutputTokens(maxOutputTokens);
        }

        return builder;
    }

    @Nonnull
    private static ResponseCreateParams.Metadata buildMetadata(@Nonnull Metadata metadata) {
        final var metadataBuilder = ResponseCreateParams.Metadata.builder();

        metadataBuilder.putAdditionalProperty(
                "run_id",
                JsonString.of(metadata.runId().toString())
        );

        final var promptId = metadata.promptId();
        if (promptId != null) {
            metadataBuilder.putAdditionalProperty(
                    "prompt_id",
                    JsonString.of(promptId.toString())
            );
        }

        return metadataBuilder.build();
    }
}
