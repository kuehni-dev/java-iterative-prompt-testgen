package dev.kuehni.llmtestgen.d4j;

import com.opencsv.CSVParser;
import dev.kuehni.llmtestgen.cobertura.CoverageResult;
import dev.kuehni.llmtestgen.compose.ComposeService;
import dev.kuehni.llmtestgen.major.MutationTestResult;
import dev.kuehni.llmtestgen.util.io.CommandUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.kuehni.llmtestgen.d4j.Defects4JUtils.parseCoverageResult;
import static dev.kuehni.llmtestgen.d4j.Defects4JUtils.parseMutationTestResult;
import static dev.kuehni.llmtestgen.util.functions.FunctionUtils.unchecked;

public class Defects4J implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Defects4J.class);

    private static final String SERVICE_NAME = "defects4j";
    private static final String WORKSPACE_ROOT_DIR_NAME = "workspace";

    @Nonnull
    private final Path path;

    @Nonnull
    private final ComposeService composeService;

    /**
     * Takes ownership of the compose service.
     */
    private Defects4J(@Nonnull Path path, @Nonnull ComposeService composeService) {
        this.path = Objects.requireNonNull(path, "path");
        this.composeService = Objects.requireNonNull(composeService, "composeService");
    }

    @Nonnull
    public static Defects4J start(@Nonnull Path defects4JHome) {
        if (!Files.isDirectory(defects4JHome)) {
            throw new IllegalArgumentException(defects4JHome + " is not a directory");
        }

        patchContainerBuild(defects4JHome);

        final var composeService = ComposeService.up(defects4JHome, SERVICE_NAME);
        try {
            return new Defects4J(defects4JHome, composeService);
        } catch (Exception ex) {
            composeService.close();
            throw new RuntimeException(ex);
        }
    }

    private static void patchContainerBuild(@Nonnull Path defects4JHome) {
        final var dockerfilePath = defects4JHome.resolve("Dockerfile");
        final var composePath = defects4JHome.resolve("docker-compose.yml");

        final var workdirD4jPattern =
                Pattern.compile("(WORKDIR /defects4j\n)(RUN cpanm)");

        final var noLangPattern = Pattern.compile("(environment: *\n)( {6}- TZ=)");
        final var networkingPattern = Pattern.compile("(ports: *\n {6})(- \"8080:8080\" *\n)");
        try {
            var dockerfileContent = Files.readString(dockerfilePath);
            var composeContent = Files.readString(composePath);

            // Check out specific defects4j version
            final var workdirD4jMatcher = workdirD4jPattern.matcher(dockerfileContent);
            dockerfileContent = workdirD4jMatcher.replaceFirst("$1RUN git config --global --replace-all safe.directory '*'\nRUN git checkout 8022adcd685ae8f591f0cb5d71282e5c93798e4d\n$2");

            // Add the LANG variable to stabilize locale
            final var envMatcher = noLangPattern.matcher(composeContent);
            composeContent = envMatcher.replaceFirst("$1      - LANG=C.UTF-8\n$2");

            // Remove exposed port and disable networking
            final var networkingMatcher = networkingPattern.matcher(composeContent);
            composeContent = networkingMatcher.replaceFirst("# $1# $2    network_mode: none\n");

            Files.writeString(dockerfilePath, dockerfileContent);
            Files.writeString(composePath, composeContent);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Nonnull
    public Project getProject(@Nonnull String projectId) {
        return getProjects().stream().filter(it -> it.id.equals(projectId)).findFirst().orElseThrow();
    }

    @Nonnull
    public List<Project> getProjects() {
        final var result = composeService.execCaptureStdout("defects4j", "pids");
        if (result.exitCode != 0) {
            throw new IllegalStateException("Reading pids exited with: " + result.exitCode);
        }
        return result.stdout.lines().map(Project::new).toList();
    }

    @Nonnull
    public List<Integer> getBugs() {
        final var result = composeService.execCaptureStdout("defects4j", "pids");
        if (result.exitCode != 0) {
            throw new IllegalStateException("Reading bids exited with: " + result.exitCode);
        }
        return result.stdout.lines().map(i -> Integer.parseInt(i, 10)).toList();
    }

    @Nonnull
    public List<String> getModifiedClasses(@Nonnull Project project, int bugId) {
        Objects.requireNonNull(project, "project");

        final var result = composeService.execCaptureStdout(
                "defects4j",
                "query",
                "-p",
                project.id,
                "-q",
                "classes.modified"
        );
        final var parser = new CSVParser();
        return result.stdout.lines()
                .map(unchecked(parser::parseLine))
                .filter(line -> line[0].equals(Integer.toString(bugId)))
                .findFirst()
                .map(line -> line[1])
                .map(value -> value.split(";"))
                .map(Arrays::asList)
                .orElseThrow();
    }

    @Nonnull
    public Workspace checkout(@Nonnull Project project, @Nonnull VersionId versionId) {
        Objects.requireNonNull(project, "project");
        Objects.requireNonNull(versionId, "version");

        final var workspace = new Workspace(project, versionId);
        final var result = composeService.execCaptureStdout(
                "defects4j",
                "checkout",
                "-p",
                project.id,
                "-v",
                versionId.toString(),
                "-w",
                workspace.getContainerPath()
        );
        if (result.exitCode != 0) {
            System.out.println(result.stdout);
            throw new IllegalStateException("Checkout failed with exit code: " + result.exitCode);
        }
        return workspace;
    }

    @Nonnull
    public CommandUtils.ExitCodeAndStdout compile(@Nonnull Workspace workspace) {
        return composeService.execCaptureStdout(
                "defects4j",
                "compile",
                "-w",
                workspace.getContainerPath()
        );
    }

    @Nonnull
    public TestResult test(
            @Nonnull Workspace workspace,
            @Nonnull String fullyQualifiedTestClassName,
            @Nonnull String... methodNames
    ) {
        Objects.requireNonNull(workspace, "workspace");
        Objects.requireNonNull(fullyQualifiedTestClassName, "fullyQualifiedTestClassName");
        Objects.requireNonNull(methodNames, "methodNames");

        if (methodNames.length == 0) {
            throw new IllegalArgumentException("methodNames must not be empty");
        }

        final var result = composeService.execCaptureStdout(
                "defects4j",
                "test",
                "-w",
                workspace.getContainerPath(),
                "-t",
                fullyQualifiedTestClassName + "::" + String.join(",", methodNames)
        );
        if (result.exitCode != 0) {
            System.out.println(result.stdout);
            throw new IllegalStateException("Testing failed with exit code: " + result.exitCode);
        }

        final Pattern failingTestsLinePattern = Pattern.compile("^Failing tests: (\\d+)$");

        return result.stdout.lines().map(failingTestsLinePattern::matcher)
                .filter(Matcher::matches)
                .findFirst()
                .map(matcher -> {
                    final var failingTests = Integer.parseInt(matcher.group(1), 10);
                    if (failingTests < 1) {
                        return TestResult.success();
                    }
                    final String failingTestsOutput;
                    try {
                        failingTestsOutput = Files.readString(workspace.getHostPath().resolve("failing_tests"));
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read failing_tests", e);
                    }
                    return TestResult.failure(failingTests, failingTestsOutput);
                }).orElseThrow(() -> {
                    logger.error(
                            "Running {} tests in {} resulted in unexpected output:\n{}",
                            methodNames.length,
                            fullyQualifiedTestClassName,
                            result.stdout
                    );
                    return new IllegalStateException("Test failed with unexpected output");
                });
    }

    @Nonnull
    public MutationTestResult mutationTest(
            @Nonnull Workspace workspace,
            @Nonnull String fullyQualifiedSourceClassName,
            @Nonnull String fullyQualifiedTestClassName,
            @Nonnull String... methodNames
    ) {
        Objects.requireNonNull(workspace, "workspace");
        Objects.requireNonNull(fullyQualifiedSourceClassName, "fullyQualifiedSourceClassName");
        Objects.requireNonNull(fullyQualifiedTestClassName, "fullyQualifiedTestClassName");
        Objects.requireNonNull(methodNames, "methodNames");

        final var result = composeService.execCaptureStdout(
                "defects4j",
                "mutation",
                "-w",
                workspace.getContainerPath(),
                "-t",
                fullyQualifiedTestClassName + "::" + String.join(",", methodNames)
        );
        if (result.exitCode != 0) {
            System.out.println(result.stdout);
            throw new IllegalStateException("Mutation testing failed with exit code: " + result.exitCode);
        }

        return readMutationTestResult(workspace, fullyQualifiedSourceClassName);
    }

    @Nonnull
    private static MutationTestResult readMutationTestResult(
            @Nonnull Workspace workspace,
            @Nonnull String fullyQualifiedSourceClassName
    ) {
        final var mutantsLogPath = workspace.getHostPath().resolve("mutants.log");
        final var killCsvPath = workspace.getHostPath().resolve("kill.csv");
        final String mutantsLog;
        final String killCsv;
        try {
            mutantsLog = Files.readString(mutantsLogPath);
            killCsv = Files.readString(killCsvPath);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read mutation testing result", e);
        }

        return parseMutationTestResult(fullyQualifiedSourceClassName, killCsv, mutantsLog);
    }

    @Nonnull
    public CoverageResult coverage(
            @Nonnull Workspace workspace,
            @Nonnull String fullyQualifiedSourceClassName,
            @Nonnull String fullyQualifiedTestClassName,
            @Nonnull String... methodNames
    ) {
        Objects.requireNonNull(workspace, "workspace");
        Objects.requireNonNull(fullyQualifiedSourceClassName, "fullyQualifiedSourceClassName");
        Objects.requireNonNull(fullyQualifiedTestClassName, "fullyQualifiedTestClassName");
        Objects.requireNonNull(methodNames, "methodNames");

        final var result = composeService.execCaptureStdout(
                "defects4j",
                "coverage",
                "-w",
                workspace.getContainerPath(),
                "-t",
                fullyQualifiedTestClassName + "::" + String.join(",", methodNames)
        );
        if (result.exitCode != 0) {
            System.out.println(result.stdout);
            throw new IllegalStateException("Evaluating coverage failed with exit code: " + result.exitCode);
        }

        return readCoverageResult(workspace, fullyQualifiedSourceClassName);
    }

    @Nonnull
    private static CoverageResult readCoverageResult(
            @Nonnull Workspace workspace,
            @Nonnull String fullyQualifiedTestClassName
    ) {
        final var coverageXmlPath = workspace.getHostPath().resolve("coverage.xml");
        try (final var readStream = Files.newInputStream(coverageXmlPath)) {
            return parseCoverageResult(fullyQualifiedTestClassName, readStream);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read coverage", ex);
        }
    }

    @Nonnull
    private CommandUtils.ExitCodeAndStdout export(@Nonnull String property, @Nonnull Workspace workspace) {
        Objects.requireNonNull(property, "property");
        Objects.requireNonNull(workspace, "workspace");

        return composeService.execCaptureStdout(
                "defects4j",
                "export",
                "-p",
                property,
                "-w",
                workspace.getContainerPath()
        );
    }

    /// Relative path from workspace to sources root
    @Nonnull
    public Path getSourcesRootDir(@Nonnull Workspace workspace) {
        Objects.requireNonNull(workspace, "workspace");

        final var result = export("dir.src.classes", workspace);

        if (result.exitCode != 0) {
            System.out.println(result.stdout);
            throw new IllegalStateException("Getting sources root failed with exit code: " + result.exitCode);
        }
        final String lastLine = result.stdout.lines().toList().getLast();
        return Path.of("", lastLine.split("/"));
    }

    /// Relative path from workspace to test sources root
    @Nonnull
    public Path getTestSourcesRootDir(@Nonnull Workspace workspace) {
        Objects.requireNonNull(workspace, "workspace");

        final var result = export("dir.src.tests", workspace);

        if (result.exitCode != 0) {
            throw new IllegalStateException("Getting sources root failed with exit code: " + result.exitCode);
        }
        final String lastLine = result.stdout.lines().toList().getLast();
        return Path.of("", lastLine.split("/"));
    }


    @Override
    public void close() {
        composeService.close();
    }

    public static class Project {
        @Nonnull
        private final String id;

        private Project(@Nonnull String id) {
            this.id = Objects.requireNonNull(id, "id");
        }

        @Override
        public String toString() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Project project)) return false;
            return Objects.equals(id, project.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }

    public class Workspace {
        @Nonnull
        public final Project project;

        @Nonnull
        public final VersionId versionId;


        private Workspace(@Nonnull Project project, @Nonnull VersionId versionId) {
            this.project = Objects.requireNonNull(project, "project");
            this.versionId = Objects.requireNonNull(versionId, "versionId");
        }

        @Nonnull
        public String getContainerPath() {
            return "/" + WORKSPACE_ROOT_DIR_NAME + "/" + project.id + "/" + versionId;
        }

        @Nonnull
        public Path getHostPath() {
            return path.resolve(WORKSPACE_ROOT_DIR_NAME).resolve(project.id).resolve(versionId.toString());
        }
    }

    public abstract sealed static class VersionId {

        public final int bugId;


        private VersionId(int bugId) {
            this.bugId = bugId;
        }

        public static final class Buggy extends VersionId {
            public Buggy(int bugId) {
                super(bugId);
            }

            @Override
            public String toString() {
                return bugId + "b";
            }
        }

        public static final class Fixed extends VersionId {
            public Fixed(int bugId) {
                super(bugId);
            }

            @Override
            public String toString() {
                return bugId + "f";
            }
        }
    }

    public static final class TestResult {

        public final int failingTests;

        @Nullable
        private final String failingTestsOutput;

        private TestResult(int failingTests, @Nullable String failingTestsOutput) {
            if (failingTests < 0) {
                throw new IllegalArgumentException("Expected failingTests to be non-negative but got " + failingTests);
            }
            if (failingTests == 0 && failingTestsOutput != null) {
                throw new IllegalArgumentException("Expected failingTestsOutput to be null if no tests fail");
            }
            if (failingTests > 0 && (failingTestsOutput == null || failingTestsOutput.isBlank())) {
                throw new IllegalArgumentException(
                        "Expected failingTestsOutput to be non-null and non-blank if tests fail"
                );
            }

            this.failingTests = failingTests;
            this.failingTestsOutput = failingTestsOutput;
        }

        @Nonnull
        private static TestResult success() {
            return new TestResult(0, null);
        }

        @Nonnull
        private static TestResult failure(int failingTests, @Nonnull String failingTestsOutput) {
            if (failingTests <= 0) {
                throw new IllegalArgumentException("Expected failingTests to be positive but got " + failingTests);
            }
            return new TestResult(failingTests, Objects.requireNonNull(failingTestsOutput, "failingTestsOutput"));
        }

        public boolean isSuccess() {
            return failingTests == 0;
        }

        @Nonnull
        public String getFailingTestsOutput() {
            return Objects.requireNonNull(failingTestsOutput, "failingTestsOutput");
        }
    }
}
