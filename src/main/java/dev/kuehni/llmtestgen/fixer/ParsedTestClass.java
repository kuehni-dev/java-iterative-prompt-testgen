package dev.kuehni.llmtestgen.fixer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import dev.kuehni.llmtestgen.dto.GeneratedTestClass;
import dev.kuehni.llmtestgen.dto.TestClassInfo;
import dev.kuehni.llmtestgen.util.java.JavaUtils;
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class ParsedTestClass extends GeneratedTestClass {

    @Nonnull
    public final CompilationUnit compilationUnit;

    public ParsedTestClass(
            @Nonnull TestClassInfo info,
            @Nonnull String code,
            @Nonnull CompilationUnit compilationUnit
    ) {
        super(info, code);
        this.compilationUnit = Objects.requireNonNull(compilationUnit, "compilationUnit");
    }

    public void write() throws IOException {
        final var path = info.testSourcesRoot.resolve(info.pathRelativeToTestSourcesRoot);
        Files.createDirectories(path.getParent());
        Files.writeString(path, compilationUnit.toString());
    }

    @Nonnull
    public String[] testMethodNames() {
        final var primaryClass = JavaUtils.getPrimaryClass(compilationUnit);
        return primaryClass.getMethods()
                .stream()
                .filter(JavaUtils::isTestMethod)
                .map(MethodDeclaration::getNameAsString)
                .toArray(String[]::new);
    }

    @Nonnull
    ValidTestClass toValid() {
        return new ValidTestClass(info, code, compilationUnit.clone());
    }
}
