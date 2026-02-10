package dev.kuehni.llmtestgen.major;

import jakarta.annotation.Nonnull;

public enum MutantState {
    /**
     * At least one test failed when the mutant was active.
     */
    FAIL,
    /**
     * The test run exceeded the allowed time after the mutation.
     */
    TIMEOUT,
    /**
     * Running the mutated code threw an exception that the tests did not expect.
     */
    EXCEPTION,
    /**
     * All tests passed despite the mutation.
     */
    LIVE,
    /**
     * No test executed the mutated code.
     */
    UNCOVERED;

    @Nonnull
    public static MutantState parse(@Nonnull String text) {
        if (text.equals("FAIL")) {
            return FAIL;
        }
        if (text.equals("TIME")) {
            return TIMEOUT;
        }
        if (text.equals("EXC")) {
            return EXCEPTION;
        }
        if (text.equals("LIVE")) {
            return LIVE;
        }
        if (text.equals("UNCOV")) {
            return UNCOVERED;
        }
        throw new IllegalStateException("Unsupported value: " + text);
    }

    /**
     * Whether the mutant was killed. This includes:
     * <ul>
     *     <li>{@link MutantState#FAIL}: detected by a test</li>
     *     <li>{@link MutantState#TIMEOUT}: detected, because tests ran out of time</li>
     *     <li>{@link MutantState#EXCEPTION}: detected, because a test crashed</li>
     * </ul>
     *
     * @see #isSurviving()
     */
    public boolean isKilled() {
        return this == FAIL || this == TIMEOUT || this == EXCEPTION;
    }

    /**
     * Whether the mutant is still surviving. This includes:
     * <ul>
     *     <li>{@link MutantState#LIVE}: tests covered the code but didn't detect the mutation</li>
     *     <li>{@link MutantState#UNCOVERED}: no tests covered the mutation</li>
     * </ul>
     *
     * @see #isKilled()
     */
    public boolean isSurviving() {
        return !isKilled();
    }

    /**
     * Whether this is not {@link MutantState#UNCOVERED}.
     */
    public boolean isCovered() {
        return !isUncovered();
    }

    /**
     * Whether this is {@link MutantState#UNCOVERED}.
     */
    public boolean isUncovered() {
        return this == UNCOVERED;
    }
}
