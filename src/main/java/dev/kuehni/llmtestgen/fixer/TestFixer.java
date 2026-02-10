package dev.kuehni.llmtestgen.fixer;

import com.github.javaparser.Problem;
import dev.kuehni.llmtestgen.dto.ClassUnderTest;
import dev.kuehni.llmtestgen.dto.GeneratedTestClass;
import dev.kuehni.llmtestgen.dto.JavaOptions;
import dev.kuehni.llmtestgen.llm.LlmClient;
import dev.kuehni.llmtestgen.llm.Metadata;
import dev.kuehni.llmtestgen.models.FixerStage;
import dev.kuehni.llmtestgen.models.Prompt;
import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.kuehni.llmtestgen.llm.PromptUtils.instructionsForJavaOnlyResponse;

public class TestFixer {

    private static final String SYNTAX_FIX_PROMPT_FORMAT = """
            Fix the syntax errors in the following test class.
            
            %s
            
            Test class to fix:
            
            ```
            %s
            ```""";

    private static final String COMPILATION_FIX_PROMPT_FORMAT = """
            Fix the javac compilation errors in the following test class:
            
            ```
            %s
            ```
            
            Test class to fix:
            
            ```
            %s
            ```""";


    @Nonnull
    private final LlmClient llmClient;

    @Nonnull
    private final Metadata metadata;

    @Nonnull
    private final JavaOptions javaOptions;

    public TestFixer(
            @Nonnull LlmClient llmClient,
            @Nonnull Metadata metadata,
            @Nonnull JavaOptions javaOptions
    ) {
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient");
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.javaOptions = Objects.requireNonNull(javaOptions, "javaOptions");
    }

    @Nonnull
    public String fixSyntax(
            @Nonnull GeneratedTestClass generatedTestClass,
            @Nonnull List<Problem> problems,
            @Nonnull FixerStage triggeringStage
    ) {
        Objects.requireNonNull(generatedTestClass, "generatedTestClass");
        Objects.requireNonNull(problems, "problems");

        final var problemsDescription =
                problems.stream().map(Problem::getVerboseMessage).collect(Collectors.joining("\n- ", "- ", ""));

        final var prompt = new Prompt(Prompt.Motive.FIX);
        prompt.save();
        triggeringStage.setFollowUpPrompt(prompt);
        triggeringStage.save();

        return llmClient.ask(
                metadata.withNewPromptId(),
                prompt,
                instructionsForJavaOnlyResponse(javaOptions),
                SYNTAX_FIX_PROMPT_FORMAT.formatted(problemsDescription, generatedTestClass.code)
        ).extractCode();
    }

    @Nonnull
    public String fixCompilationErrors(
            @Nonnull ParsedTestClass parsedTest,
            @Nonnull String javacOutput,
            @Nonnull FixerStage triggeringStage
    ) {
        Objects.requireNonNull(parsedTest, "validTest");
        Objects.requireNonNull(javacOutput, "javacOutput");

        final var prompt = new Prompt(Prompt.Motive.FIX);
        prompt.save();
        triggeringStage.setFollowUpPrompt(prompt);
        triggeringStage.save();

        return llmClient.ask(
                metadata.withNewPromptId(),
                prompt,
                instructionsForJavaOnlyResponse(javaOptions),
                COMPILATION_FIX_PROMPT_FORMAT.formatted(javacOutput, parsedTest.code)
        ).extractCode();
    }

    @Nonnull
    public String fixFailingTests(
            @Nonnull ParsedTestClass parsedTest,
            @Nonnull String testingOutput,
            @Nonnull ClassUnderTest classUnderTest,
            @Nonnull FixerStage triggeringStage
    ) {
        Objects.requireNonNull(parsedTest, "validTest");
        Objects.requireNonNull(testingOutput, "testingOutput");
        Objects.requireNonNull(classUnderTest, "classUnderTest");

        final var prompt = new Prompt(Prompt.Motive.FIX);
        prompt.save();
        triggeringStage.setFollowUpPrompt(prompt);
        triggeringStage.save();

        return llmClient.ask(
                metadata.withNewPromptId(),
                prompt,
                instructionsForJavaOnlyResponse(javaOptions),
                """
                        This is a java class that should be tested (do not modify this file):
                        
                        ```
                        %s
                        ```
                        
                        Running the associated test class fails with the following errors.
                        
                        ```
                        %s
                        ```""".formatted(classUnderTest.code, testingOutput),
                """
                        Fix the errors in the following test class:
                        
                        ```
                        %s
                        ```""".formatted(parsedTest.code)
        ).extractCode();
    }
}
