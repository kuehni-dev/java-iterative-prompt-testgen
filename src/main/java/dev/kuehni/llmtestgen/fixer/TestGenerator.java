package dev.kuehni.llmtestgen.fixer;

import dev.kuehni.llmtestgen.dto.JavaOptions;
import dev.kuehni.llmtestgen.llm.LlmClient;
import dev.kuehni.llmtestgen.llm.Metadata;
import dev.kuehni.llmtestgen.models.Iteration;
import dev.kuehni.llmtestgen.models.Prompt;
import jakarta.annotation.Nonnull;

import java.util.Objects;

import static dev.kuehni.llmtestgen.llm.PromptUtils.instructionsForJavaOnlyResponse;

public class TestGenerator {

    private static final String INITIAL_GEN_PROMPT_FORMAT = """
            Generate a complete test class for the following code:
            
            ```
            %s
            ```""";


    @Nonnull
    private final LlmClient llmClient;

    @Nonnull
    private final Metadata metadata;

    @Nonnull
    private final JavaOptions javaOptions;

    public TestGenerator(
            @Nonnull LlmClient llmClient,
            @Nonnull Metadata metadata,
            @Nonnull JavaOptions javaOptions
    ) {
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient");
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.javaOptions = Objects.requireNonNull(javaOptions, "javaOptions");
    }

    @Nonnull
    public String generateInitialTestClass(@Nonnull String classUnderTest, @Nonnull Iteration initialIteration) {
        Objects.requireNonNull(classUnderTest, "classUnderTest");

        final var metadata = this.metadata.withNewPromptId();

        final var prompt = new Prompt(Prompt.Motive.INITIAL);
        prompt.save();
        initialIteration.setGenerationPrompt(prompt);
        initialIteration.save();

        return llmClient.ask(
                metadata,
                prompt,
                instructionsForJavaOnlyResponse(javaOptions),
                INITIAL_GEN_PROMPT_FORMAT.formatted(classUnderTest)
        ).extractCode();
    }
}
