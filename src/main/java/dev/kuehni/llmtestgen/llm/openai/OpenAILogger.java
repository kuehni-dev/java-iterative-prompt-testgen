package dev.kuehni.llmtestgen.llm.openai;

import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventBuilder;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.kuehni.llmtestgen.util.text.TextUtils.truncateToFirstLine;

class OpenAILogger {
    OpenAILogger() {}

    public static void logInput(@Nonnull ResponseCreateParams params, @Nonnull Logger logger) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(logger, "logger");

        params.instructions().ifPresent(instructions -> logger.trace("Instructions: {}", instructions));
        params.input().ifPresent(input -> logger.atTrace()
                .setMessage("Input: {}")
                .addArgument(() -> formatInput(input))
                .log());
    }

    public static void logUsage(@Nonnull Response response, @Nonnull Logger logger) {
        Objects.requireNonNull(response, "response");
        Objects.requireNonNull(logger, "logger");

        response.usage().ifPresent(usage -> {
            logger.trace(
                    "Input Tokens:  {} ({} cached)",
                    usage.inputTokens(),
                    usage.inputTokensDetails().cachedTokens()
            );


            final var maxOutputTokens = response.maxOutputTokens();

            final var outputTokens = usage.outputTokens();
            final var reasoningTokens = usage.outputTokensDetails().reasoningTokens();
            if (maxOutputTokens.isPresent()) {
                final LoggingEventBuilder builder;
                if (outputTokens >= maxOutputTokens.get()) {
                    builder = logger.atError();
                } else if (outputTokens >= maxOutputTokens.get() * .8) {
                    builder = logger.atWarn();
                } else {
                    builder = logger.atTrace();
                }

                builder.log(
                        "Output Tokens:  {}/{} ({} for reasoning)",
                        outputTokens,
                        maxOutputTokens.get(),
                        reasoningTokens
                );
            } else {
                logger.trace(
                        "Output Tokens: {} ({} for reasoning)",
                        outputTokens,
                        reasoningTokens
                );
            }
        });
    }

    private static String formatInput(@Nonnull ResponseCreateParams.Input input) {
        return Stream.of(
                        input.text().map(text -> truncateToFirstLine(text, " (truncated)")).stream(),
                        input.response().map(items -> "ResponseInputItem[%d]".formatted(items.size())).stream()
                ).flatMap(Function.identity())
                .findFirst()
                .orElse("-");
    }
}
