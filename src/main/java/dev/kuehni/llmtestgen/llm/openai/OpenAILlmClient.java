package dev.kuehni.llmtestgen.llm.openai;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.ResponsesModel;
import com.openai.models.responses.ResponseCreateParams;
import dev.kuehni.llmtestgen.llm.LlmClient;
import dev.kuehni.llmtestgen.llm.Metadata;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static dev.kuehni.llmtestgen.llm.openai.OpenAILogger.logInput;
import static dev.kuehni.llmtestgen.llm.openai.OpenAILogger.logUsage;
import static dev.kuehni.llmtestgen.llm.openai.OpenAIUtils.aggregateOutputText;

public class OpenAILlmClient extends LlmClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenAILlmClient.class);


    @Nonnull
    private final OpenAIClient client;

    @Nonnull
    private final OpenAIModelOptions modelOptions;

    public OpenAILlmClient(
            @Nonnull OpenAIClient client,
            @Nonnull OpenAIModelOptions modelOptions
    ) {
        this.client = Objects.requireNonNull(client);
        this.modelOptions = Objects.requireNonNull(modelOptions);
    }


    @Nonnull
    @Override
    public Answer ask(
            @Nonnull Metadata metadata,
            @Nonnull String systemInstructions,
            @Nonnull List<String> userMessages
    ) {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(systemInstructions, "systemInstructions");
        Objects.requireNonNull(userMessages, "userPrompts");

        final var userInputItems = userMessages.stream().map(OpenAIUtils::responseInputUserMessage).toList();
        final var params = modelOptions.responseCreateParamsBuilder(metadata)
                .instructions(systemInstructions)
                .input(ResponseCreateParams.Input.ofResponse(userInputItems))
                .build();

        logInput(params, logger);
        final var response = client.responses().create(params);
        logUsage(response, logger);

        final var answer = new Answer(aggregateOutputText(response));

        return response.usage()
                .map(usage -> answer.withUsage(
                        usage.inputTokens(),
                        usage.outputTokens(),
                        usage.outputTokensDetails().reasoningTokens()
                ))
                .orElse(answer);
    }

    @Nonnull
    @Override
    public String getModelName() {
        final var model = modelOptions.model();

        return Stream.of(
                        model.string(),
                        model.chat().map(ChatModel::toString),
                        model.only().map(ResponsesModel.ResponsesOnlyModel::toString)
                ).flatMap(Optional::stream)
                .findFirst()
                .orElseGet(model::toString);
    }

    @Nullable
    @Override
    public String getReasoningEffort() {
        return modelOptions.reasoningEffort() != null ? modelOptions.reasoningEffort().toString() : null;
    }
}
