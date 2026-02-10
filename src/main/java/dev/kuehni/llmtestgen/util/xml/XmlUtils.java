package dev.kuehni.llmtestgen.util.xml;

import jakarta.annotation.Nonnull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class XmlUtils {
    public XmlUtils() {}

    @Nonnull
    public static Stream<Node> stream(@Nonnull NodeList nodeList) {
        return IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item);
    }

    @Nonnull
    public static <N extends Node> Function<N, Stream<Element>> filteringIsElement() {
        return node -> {
            if (node instanceof Element element) {
                return Stream.of(element);
            }
            return Stream.empty();
        };
    }

    @Nonnull
    public static <E extends Element> Predicate<E> attributeEquals(@Nonnull String name, @Nonnull String value) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(value, "value");

        return elem -> value.equals(elem.getAttribute(name));
    }
}
