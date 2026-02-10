package dev.kuehni.llmtestgen.app;


import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static java.lang.Math.min;

public class Sampler {
    private static final int SAMPLE_SIZE = 10;
    private static final long SEED = 20251228;

    static void main() {
        sample(ELIGIBLE_BUGS_JACKSON_CORE).stream().map(formatterFor("JacksonCore")).forEach(System.out::println);
        System.out.println();
        sample(ELIGIBLE_BUGS_GSON).stream().map(formatterFor("Gson")).forEach(System.out::println);
        System.out.println();
        sample(ELIGIBLE_BUGS_LANG).stream().map(formatterFor("Lang")).forEach(System.out::println);
    }

    @Nonnull
    private static List<Integer> sample(@Nonnull List<Integer> bugs) {
        final var bag = new ArrayList<>(bugs);
        Collections.shuffle(bag, new Random(SEED));
        final var sample = bag.subList(0, min(SAMPLE_SIZE, bag.size()));
        Collections.sort(sample);
        return sample;
    }

    @Nonnull
    private static Function<Integer, String> formatterFor(@Nonnull String project) {
        return bug -> "%s,%d".formatted(project, bug);
    }


    public static final List<Integer> ELIGIBLE_BUGS_JACKSON_CORE = List.of(3, 4, 5, 6, 7, 8, 10, 11, 13, 14, 15, 16);

    public static final List<Integer> ELIGIBLE_BUGS_GSON = List.of(3, 5, 6, 7, 8, 10, 11, 12, 13, 15);

    public static final List<Integer> ELIGIBLE_BUGS_LANG = List.of(
            1,
            3,
            4,
            5,
            6,
            7,
            8,
            9,
            10,
            11,
            12,
            13,
            14,
            15,
            16,
            21,
            22,
            23,
            24,
            26,
            27,
            28,
            29,
            30,
            31,
            32,
            33,
            34,
            35,
            36,
            37,
            38,
            39,
            40,
            41
    );
}
