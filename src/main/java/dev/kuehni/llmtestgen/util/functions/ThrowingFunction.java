package dev.kuehni.llmtestgen.util.functions;

@FunctionalInterface
public interface ThrowingFunction<T, R, X extends Throwable> {
    R apply(T t) throws X;
}
