package com.github.czelabueno.jai.workflow.node;

import com.github.czelabueno.jai.workflow.transition.TransitionState;
import lombok.Getter;
import lombok.NonNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a node in a workflow that executes a function with a given input and produces an output.
 * <p>
 * This class implements the {@link TransitionState} interface.
 *
 * @param <T> the type of the input to the function. Normally a stateful bean POJO defined by the user.
 * @param <R> the type of the output from the function. Normally a stateful bean POJO defined by the user.
 */
public class Node<T, R> implements TransitionState {

    @Getter
    private final String name;
    private final Function<T, R> function;
    @Getter
    private T functionInput;
    @Getter
    private R functionOutput;

    /**
     * Constructs a Node with the specified name and function.
     *
     * @param name     the name of the node
     * @param function the function to execute
     * @throws IllegalArgumentException if the node name is empty
     * @throws NullPointerException     if the name or function is null
     */
    public Node(@NonNull String name, @NonNull Function<T, R> function) {
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Node name cannot be empty");
        }
        this.name = name;
        this.function = function;
    }

    /**
     * Executes the function with the given input and stores the input and output.
     *
     * @param input the input to the function
     * @return the output from the function
     * @throws IllegalArgumentException if the input is null
     */
    public R execute(T input) {
        if (input == null) {
            throw new IllegalArgumentException("Function input cannot be null");
        }
        R output = function.apply(input);
        functionInput = input;
        functionOutput = output;
        return output;
    }

    /**
     * Creates a new Node with the specified name and function.
     *
     * @param name     the name of the node
     * @param function the function to execute
     * @param <T>      the type of the input to the function
     * @param <R>      the type of the output from the function
     * @return a new Node instance
     */
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
