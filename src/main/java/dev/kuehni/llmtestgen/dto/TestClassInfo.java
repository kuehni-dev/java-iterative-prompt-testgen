package dev.kuehni.llmtestgen.dto;

import com.github.javaparser.ast.CompilationUnit;
import dev.kuehni.llmtestgen.fixer.ParsedTestClass;
import dev.kuehni.llmtestgen.util.java.JavaUtils;
import jakarta.annotation.Nonnull;

import java.nio.file.Path;
import java.util.Objects;

public class TestClassInfo {

    @Nonnull
    public final Path testSourcesRoot;

    @Nonnull
    public final Path pathRelativeToTestSourcesRoot;

    @Nonnull
    public final String packageName;

    @Nonnull
    public final String simpleName;

    @Nonnull
    public final String fullyQualifiedName;

    public TestClassInfo(
            @Nonnull Path testSourcesRoot,
            @Nonnull Path pathRelativeToTestSourcesRoot,
            @Nonnull String packageName,
            @Nonnull String simpleName
    ) {
        this.testSourcesRoot = Objects.requireNonNull(testSourcesRoot, "testSourcesRoot");
        this.pathRelativeToTestSourcesRoot =
                Objects.requireNonNull(pathRelativeToTestSourcesRoot, "pathRelativeToTestSourcesRoot");
        this.packageName = Objects.requireNonNull(packageName, "packageName");
        this.simpleName = Objects.requireNonNull(simpleName, "simpleName");

        this.fullyQualifiedName = packageName + "." + simpleName;
    }

    @Nonnull
    public static TestClassInfo from(
            @Nonnull ClassUnderTest classUnderTest,
            @Nonnull Path testSourcesRoot,
            @Nonnull String nameSuffix
    ) {
        Objects.requireNonNull(classUnderTest, "classUnderTest");
        Objects.requireNonNull(testSourcesRoot, "testSourcesRoot");
        Objects.requireNonNull(nameSuffix, "nameSuffix");

        final var simpleName = classUnderTest.simpleName + nameSuffix;
        final var filename = simpleName + JavaUtils.JAVA_FILE_EXTENSION;

        return new TestClassInfo(
                testSourcesRoot,
                classUnderTest.pathRelativeToSourcesRoot.getParent().resolve(filename),
                classUnderTest.packageName,
                simpleName
        );
    }

    @Nonnull
    public GeneratedTestClass generated(@Nonnull String generatedTestCode) {
        return new GeneratedTestClass(this, generatedTestCode);
    }

    @Nonnull
    public ParsedTestClass parsed(@Nonnull CompilationUnit compilationUnit) {
        Objects.requireNonNull(compilationUnit, "compilationUnit");

        compilationUnit.setPackageDeclaration(packageName);
        JavaUtils.getPrimaryClass(compilationUnit).setName(simpleName);

        return new ParsedTestClass(this, compilationUnit.toString(), compilationUnit);
    }
}
