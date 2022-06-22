package me.zero.expressions;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Brady
 * @since 6/21/2022
 */
public final class Utils {

    private Utils() {}

    public static <T> List<List<T>> splitList(List<T> list, Predicate<T> condition) {
        // Forms pairs of indices to derive subLists from
        var indices = Stream.of(
            IntStream.of(-1),
            IntStream.range(0, list.size()).filter(i -> condition.test(list.get(i))),
            IntStream.of(list.size())
        ).flatMapToInt(stream -> stream).toArray();

        return IntStream.range(0, indices.length - 1)
            .mapToObj(i -> list.subList(indices[i] + 1, indices[i + 1]))
            .collect(Collectors.toList());
    }
}
