package dev.kuehni.llmtestgen.dto;

import jakarta.annotation.Nonnull;

import java.util.Objects;

public class GeneratedTestClass {

    @Nonnull
    public final TestClassInfo info;

    @Nonnull
    public final String code;

    protected GeneratedTestClass(@Nonnull TestClassInfo info, @Nonnull String code) {
        this.info = Objects.requireNonNull(info, "info");
        this.code = Objects.requireNonNull(code, "code");
    }
}
