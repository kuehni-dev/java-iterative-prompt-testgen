package dev.kuehni.llmtestgen.llm;

import dev.kuehni.llmtestgen.dto.JavaOptions;
import jakarta.annotation.Nonnull;

public class PromptUtils {

    private static final String GLOBAL_INSTRUCTIONS_JAVA_ONLY_FORMAT = """
            You are a senior Java software engineer. \
            Your response must contain only valid Java %d code. \
            Do not include markdown, code fences, comments outside the code, or any explanatory text. \
            Always include the complete code in your answer, including existing parts instead of placeholders, when asked to edit existing code. \
            For testing, use the Junit %d framework.""";

    private PromptUtils() {}

    @Nonnull
    public static String instructionsForJavaOnlyResponse(@Nonnull JavaOptions javaOptions) {
        return GLOBAL_INSTRUCTIONS_JAVA_ONLY_FORMAT.formatted(
                javaOptions.javaMajorVersion(),
                javaOptions.jUnitMajorVersion()
        );
    }

}
