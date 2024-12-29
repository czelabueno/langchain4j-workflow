package com.github.czelabueno.jai.workflow.node;

import lombok.Getter;
import lombok.NonNull;

import java.util.Objects;
import java.util.function.Function;

public class Node<T, R> {

    @Getter
    private final String name;
    private final Function<T, R> function;
    @Getter
    private T functionInput;
    @Getter
    private R functionOutput;

    public Node(@NonNull String name, @NonNull Function<T, R> function) {
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Node name cannot be empty");
        }
        this.name = name;
        this.function = function;
    }

    public R execute(T input) {
        if (input == null) {
            throw new IllegalArgumentException("Function input cannot be null");
        }
        R output = function.apply(input);
        functionInput = input;
        functionOutput = output;
        return output;
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
