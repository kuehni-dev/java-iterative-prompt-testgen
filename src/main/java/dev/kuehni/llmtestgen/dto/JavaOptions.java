package dev.kuehni.llmtestgen.dto;

public record JavaOptions(int javaMajorVersion, int jUnitMajorVersion) {
    public JavaOptions {
        if (javaMajorVersion < 1) {
            throw new IllegalArgumentException("Java major version must be at least 1");
        }
        if (jUnitMajorVersion < 1) {
            throw new IllegalArgumentException("JUnit major version must be at least 1");
        }
    }
}
