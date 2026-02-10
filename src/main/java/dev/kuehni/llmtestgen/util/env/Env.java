package dev.kuehni.llmtestgen.util.env;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.Nonnull;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class Env {

    private final static Dotenv dotenv = Dotenv.load();

    private Env() {}

    @Nonnull
    public static String require(@Nonnull String key) {
        final var value = System.getenv(key);
        if (value != null) {
            return value;
        }

        final var dotenvValue = dotenv.get(key);
        if (dotenvValue != null) {
            return dotenvValue;
        }

        throw new IllegalStateException("Environment variable '%s' is not set".formatted(key));
    }

    @Nonnull
    public static String openaiApiKey() {
        return require("OPENAI_API_KEY");
    }


    public static int maxOutputTokens() {
        return Integer.parseInt(require("MAX_OUTPUT_TOKENS"), 10);
    }

    @Nonnull
    public static Path defects4JHome() {
        return Path.of(require("DEFECTS4J_HOME"));
    }

    @Nonnull
    public static List<ProjectIdAndBugId> defects4jBugs() {
        final var envName = "DEFECTS4J_BUGS";

        final var raw = require(envName);
        return raw.lines().filter(line -> !line.isBlank()).map(line -> {
            final var parts = line.split(",", 2);
            if (parts.length != 2) {
                throw new IllegalStateException("Missing comma in line of %s: %s".formatted(envName, line));
            }
            final int bugId;
            try {
                bugId = Integer.parseInt(parts[1].trim(), 10);
            } catch (NumberFormatException ex) {
                throw new IllegalStateException(
                        "Bug id is not an integer in line of %s: %s".formatted(envName, line),
                        ex
                );
            }
            return new ProjectIdAndBugId(parts[0].trim(), bugId);
        }).toList();
    }

    @Nonnull
    public static String composeProgram() {
        return require("COMPOSE_PROGRAM");
    }

    public static int numberOfRuns() {
        return Integer.parseInt(require("RUNS"), 10);
    }

    public static int maxLlmRepromptsForFixingTests() {
        return Integer.parseInt(require("MAX_LLM_REPROMPTS_FOR_FIXING_TEST"), 10);
    }

    public static int feedbackIterations() {
        return Integer.parseInt(require("FEEDBACK_ITERATIONS"), 10);
    }

    public static final class ProjectIdAndBugId {
        @Nonnull
        public final String projectId;

        public final int bugId;

        private ProjectIdAndBugId(@Nonnull String projectId, int bugId) {
            this.projectId = Objects.requireNonNull(projectId, "projectId");
            this.bugId = bugId;
        }
    }
}
