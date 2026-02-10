package dev.kuehni.llmtestgen.fixer;

import com.github.javaparser.ast.CompilationUnit;
import dev.kuehni.llmtestgen.dto.TestClassInfo;
import jakarta.annotation.Nonnull;

public class ValidTestClass extends ParsedTestClass {
    ValidTestClass(@Nonnull TestClassInfo info, @Nonnull String code, @Nonnull CompilationUnit compilationUnit) {
        super(info, code, compilationUnit);
    }
}
