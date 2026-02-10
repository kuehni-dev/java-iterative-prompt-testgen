package dev.kuehni.llmtestgen.util.java;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import jakarta.annotation.Nonnull;

import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JavaUtils {

    public static final String JAVA_FILE_EXTENSION = ".java";

    @Nonnull
    public static Path fullyQualifiedClassNameToPath(@Nonnull String className) {
        Objects.requireNonNull(className, "className");

        final String[] parts = className.split("\\.");
        parts[parts.length - 1] = parts[parts.length - 1] + JAVA_FILE_EXTENSION;
        return Path.of("", parts);
    }

    @Nonnull
    public static String pathToFullyQualifiedName(@Nonnull Path pathRelativeToSourcesRoot) {
        Objects.requireNonNull(pathRelativeToSourcesRoot, "pathRelativeToSourcesRoot");

        final Path packagePath;
        if (pathRelativeToSourcesRoot.toString().endsWith(JAVA_FILE_EXTENSION)) {
            final var parent = pathRelativeToSourcesRoot.getParent();
            final var basename = pathRelativeToSourcesRoot.getFileName().toString();
            final var primaryTypeName = basename.substring(0, JAVA_FILE_EXTENSION.length());
            packagePath = parent.resolve(primaryTypeName);
        } else {
            packagePath = pathRelativeToSourcesRoot;
        }

        return StreamSupport.stream(packagePath.spliterator(), false)
                .map(Path::toString)
                .collect(Collectors.joining("."));
    }

    @Nonnull
    public static ClassOrInterfaceDeclaration getPrimaryClass(@Nonnull CompilationUnit compilationUnit) {
        final var types = compilationUnit.getTypes();
        if (types.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one top-level type for compilation unit but got " + types.size());
        }
        final var classOrInterfaceDeclaration = types.getFirst().orElseThrow().asClassOrInterfaceDeclaration();
        if (classOrInterfaceDeclaration.isInterface()) {
            throw new IllegalStateException("Expected primary type to be a class but got an interface");
        }
        return classOrInterfaceDeclaration;
    }

    public static boolean isTestMethod(@Nonnull MethodDeclaration method) {
        return method.getAnnotations()
                .stream()
                .anyMatch(annotation -> "Test".equals(annotation.getName().getIdentifier()));
    }
}
