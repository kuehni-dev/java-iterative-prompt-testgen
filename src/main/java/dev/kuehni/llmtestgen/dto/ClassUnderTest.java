package dev.kuehni.llmtestgen.dto;

import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static dev.kuehni.llmtestgen.util.java.JavaUtils.fullyQualifiedClassNameToPath;

public class ClassUnderTest {

    @Nonnull
    public final Path sourcesRoot;

    @Nonnull
    public final Path pathRelativeToSourcesRoot;

    @Nonnull
    public final String code;

    @Nonnull
    public final String packageName;

    @Nonnull
    public final String simpleName;

    @Nonnull
    public final String fullyQualifiedName;

    private ClassUnderTest(
            @Nonnull Path sourcesRoot,
            @Nonnull Path pathRelativeToSourcesRoot,
            @Nonnull String code,
            @Nonnull String packageName,
            @Nonnull String simpleName
    ) {
        this.sourcesRoot = Objects.requireNonNull(sourcesRoot, "sourcesRoot");
        this.pathRelativeToSourcesRoot = Objects.requireNonNull(pathRelativeToSourcesRoot, "pathRelativeToSourcesRoot");
        this.code = Objects.requireNonNull(code, "code");
        this.packageName = Objects.requireNonNull(packageName, "packageName");
        this.simpleName = Objects.requireNonNull(simpleName, "simpleName");
        this.fullyQualifiedName = packageName + '.' + simpleName;
    }

    @Nonnull
    public static ClassUnderTest read(@Nonnull String fullyQualifiedName, @Nonnull Path sourcesRoot)
            throws IOException {
        Objects.requireNonNull(fullyQualifiedName, "fullyQualifiedName");
        Objects.requireNonNull(sourcesRoot, "sourcesRoot");

        final var pathRelativeToSourcesRoot = fullyQualifiedClassNameToPath(fullyQualifiedName);
        final var sourceCode = Files.readString(sourcesRoot.resolve(pathRelativeToSourcesRoot));
        final var packageName = fullyQualifiedName.substring(0, fullyQualifiedName.lastIndexOf('.'));
        final var simpleName = fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1);

        return new ClassUnderTest(sourcesRoot, pathRelativeToSourcesRoot, sourceCode, packageName, simpleName);
    }
}
