package dev.langchain4j.workflow.node;

import java.util.Objects;
import java.util.function.Function;

public class Node<T, R> {

    private final String name;
    private final Function<T, R> function;

    public Node(String name, Function<T, R> function) {
        this.name = name;
        this.function = function;
    }

    public String getName() {
        return name;
    }

    public R execute(T input) {
        return function.apply(input);
    }

    public static <T, R> Node<T, R> from(String name, Function<T, R> function) {
        return new Node<>(name, function);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node<?, ?> node = (Node<?, ?>) o;

        if (!Objects.equals(name, node.name)) return false;
        return Objects.equals(function, node.function);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (function != null ? function.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                ", function=" + function +
                '}';
    }
}
