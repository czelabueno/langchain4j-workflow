package com.github.czelabueno.jai.workflow.node;

import com.github.czelabueno.jai.workflow.transition.TransitionState;
import lombok.NonNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a conditional node in a workflow that evaluates a condition function.
 * <p>
 * Implements the {@link TransitionState} interface.
 *
 * @param <T> the stateful bean POJO defined by the user. It is used to store the state of the workflow.
 */
public class Conditional<T> implements TransitionState {

    private final Function<T, Node<T,?>> condition;

    /**
     * Constructs a Conditional with the specified condition function.
     *
     * @param condition the condition function to evaluate
     * @throws NullPointerException if the condition function is null
     */
    public Conditional(@NonNull Function<T, Node<T,?>> condition) {
        this.condition = Objects.requireNonNull(condition, "Condition function cannot be null");
    }

    /**
     * Evaluates the condition function with the given stateful bean.
     *
     * @param input the stateful bean as input to the condition function
     * @return the resulting Node from the condition function
     * @throws NullPointerException if the input is null
     */
    public Node<T,?> evaluate(T input) {
        Objects.requireNonNull(input, "Function Input cannot be null");
        return condition.apply(input);
    }

    /**
     * Creates a new Conditional with the specified condition function.
     *
     * @param condition the condition function to evaluate
     * @param <T> the stateful bean as input to the condition function
     * @return a new Conditional instance
     */
    public static <T> Conditional<T> eval(Function<T, Node<T,?>> condition) {
        return new Conditional<>(condition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Conditional<?> that = (Conditional<?>) o;

        return Objects.equals(condition, that.condition);
    }

    @Override
    public int hashCode() {
        return condition != null ? condition.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Conditional{" +
                "condition=" + condition +
                '}';
    }
}
