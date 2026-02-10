package dev.kuehni.llmtestgen.app;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.javaparser.JavaParser;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.ReasoningEffort;
import com.openai.models.ResponsesModel;
import dev.kuehni.llmtestgen.d4j.Defects4J;
import dev.kuehni.llmtestgen.dto.JavaOptions;
import dev.kuehni.llmtestgen.llm.LlmClient;
import dev.kuehni.llmtestgen.llm.openai.OpenAILlmClient;
import dev.kuehni.llmtestgen.llm.openai.OpenAIModelOptions;
import dev.kuehni.llmtestgen.models.Run;
import dev.kuehni.llmtestgen.util.env.Env;
import io.ebean.DB;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static dev.kuehni.llmtestgen.util.text.TextUtils.formatPercentage;
import static dev.kuehni.llmtestgen.util.text.TextUtils.formatStackTrace;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static final int MAX_OUTPUT_TOKENS = Env.maxOutputTokens();

    public static final long NUMBER_OF_RUNS = Env.numberOfRuns();

    public static final int MAX_LLM_REPROMPTS_FOR_FIXING_TEST = Env.maxLlmRepromptsForFixingTests();
    public static final int FEEDBACK_ITERATIONS = Env.feedbackIterations();

    static void main() throws IOException {
        setUpDatabase();

        // Defects4J uses Java 11. And all active projects/bugs in defects4j seem to use JUnit 4.
        final var javaOptions = new JavaOptions(11, 4);
        final var javaParser = new JavaParser();

        final var llmClient = getLlmClient();

        try (final var defects4j = Defects4J.start(Env.defects4JHome())) {
            final var projectIdAndBugIds = Env.defects4jBugs();
            logger.info("Runs: {}, Bugs: {}", NUMBER_OF_RUNS, projectIdAndBugIds.size());

            for (var i = 0; i < NUMBER_OF_RUNS; ++i) {
                logger.info("Progress: {}/{} ({})", i, NUMBER_OF_RUNS, formatPercentage(i * 1.0 / NUMBER_OF_RUNS));
                for (final var bug : projectIdAndBugIds) {
                    final var project = defects4j.getProject(bug.projectId);
                    runForBug(defects4j, project, bug.bugId, llmClient, javaOptions, javaParser);
                }
            }
        }
    }

    private static void runForBug(
            @Nonnull Defects4J defects4j,
            @Nonnull Defects4J.Project project,
            int bugId,
            @Nonnull LlmClient llmClient,
            @Nonnull JavaOptions javaOptions,
            @Nonnull JavaParser javaParser
    ) {
        final var run = new Run(
                UuidCreator.getTimeOrderedEpoch(),
                project.toString(),
                bugId,
                llmClient.getModelName(),
                llmClient.getReasoningEffort(),
                MAX_LLM_REPROMPTS_FOR_FIXING_TEST,
                FEEDBACK_ITERATIONS
        );
        run.save();

        logger.info("Starting run for {}-{} ({})", project, bugId, run.getUuid());
        try {
            final var success = new Runner(
                    run,
                    llmClient,
                    javaOptions,
                    javaParser,
                    defects4j,
                    project,
                    bugId,
                    MAX_LLM_REPROMPTS_FOR_FIXING_TEST,
                    FEEDBACK_ITERATIONS
            ).run();
            run.setSuccess(success);
            if (success) {
                logger.info("Run for {}-{} ({}) succeeded", project, bugId, run.getUuid());
            } else {
                logger.error("Run for {}-{} ({}) failed", project, bugId, run.getUuid());
            }
        } catch (Exception ex) {
            logger.error("Run for {}-{} ({}) crashed", project, bugId, run.getUuid(), ex);
            run.setErrorStackTrace(formatStackTrace(ex));
        }
        run.setCompleted();
        run.save();
    }

    @Nonnull
    private static LlmClient getLlmClient() {
        return getOpenAiLlmClient();
    }

    @Nonnull
    private static OpenAILlmClient getOpenAiLlmClient() {
        final var client = OpenAIOkHttpClient.builder().apiKey(Env.openaiApiKey()).build();
//        final var modelOptions =
//                new OpenAIModelOptions(ResponsesModel.ofChat(ChatModel.GPT_4_1_NANO_2025_04_14))
//                        .withMaxOutputTokens(MAX_OUTPUT_TOKENS);
//        final var modelOptions =
//                new OpenAIModelOptions(ResponsesModel.ofChat(ChatModel.CODEX_MINI_LATEST), ReasoningEffort.LOW)
//                        .withMaxOutputTokens(MAX_OUTPUT_TOKENS);
//        final var modelOptions =
//                new OpenAIModelOptions(ResponsesModel.ofString("gpt-5.2-codex"), ReasoningEffort.MEDIUM)
//                        .withMaxOutputTokens(MAX_OUTPUT_TOKENS);
        final var modelOptions =
                new OpenAIModelOptions(ResponsesModel.ofChat(ChatModel.GPT_5_NANO_2025_08_07), ReasoningEffort.LOW)
                        .withMaxOutputTokens(MAX_OUTPUT_TOKENS);

        return new OpenAILlmClient(client, modelOptions);
    }

    private static void setUpDatabase() throws IOException {
        try (final var scriptStream = Main.class.getResourceAsStream("/db-create-all.sql")) {
            if (scriptStream == null) {
                throw new RuntimeException("Script file not found");
            }
            final var script = new String(scriptStream.readAllBytes(), StandardCharsets.UTF_8);

            DB.sqlUpdate(script).execute();
        }
    }
}
