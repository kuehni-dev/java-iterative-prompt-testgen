package dev.kuehni.llmtestgen.llm.openai;

import com.openai.models.responses.*;
import jakarta.annotation.Nonnull;

import java.util.Objects;
import java.util.stream.Collectors;

public class OpenAIUtils {

    private OpenAIUtils() {}

    @Nonnull
    public static String aggregateOutputReasoningSummary(@Nonnull Response response) {
        Objects.requireNonNull(response, "response");

        return response.output().stream()
                .flatMap(item -> item.reasoning().stream())
                .flatMap(item -> item.summary().stream())
                .map(ResponseReasoningItem.Summary::text)
                .collect(Collectors.joining("\n\n"));
    }

    @Nonnull
    public static String aggregateOutputText(@Nonnull Response response) {
        Objects.requireNonNull(response, "response");

        return response.output()
                .stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .map(ResponseOutputMessage.Content::asOutputText)
                .map(ResponseOutputText::text)
                .collect(Collectors.joining("\n\n"));
    }

    @Nonnull
    static ResponseInputItem responseInputUserMessage(@Nonnull String content) {
        return ResponseInputItem.ofMessage(ResponseInputItem.Message.builder()
                                                   .role(ResponseInputItem.Message.Role.USER)
                                                   .addInputTextContent(content)
                                                   .build());
    }
}
