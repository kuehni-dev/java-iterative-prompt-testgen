package dev.kuehni.llmtestgen.util.functions;

import jakarta.annotation.Nonnull;

import java.util.Objects;
import java.util.function.Function;

public class FunctionUtils {

    /**
     * Returns a function that calls the passed function {@code function} and returns its result. Converts any exception
     * thrown by it to an unchecked exception. Other subclasses of {@link Throwable}, like {@link Error}, are not
     * caught.
     *
     * @param <T> The type of the input to the function.
     * @param <R> The type of the result of the function.
     * @param <X> The type of the exception that may be thrown by the function.
     * @throws RuntimeException If {@code function} throws an exception.
     */
    @Nonnull
    public static <T, R, X extends Exception> Function<T, R> unchecked(@Nonnull ThrowingFunction<T, R, X> function) {
        Objects.requireNonNull(function, "function");

        return t -> {
            try {
                return function.apply(t);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}
