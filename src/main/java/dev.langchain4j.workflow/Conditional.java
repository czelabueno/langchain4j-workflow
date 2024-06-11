package dev.langchain4j.workflow;

import java.util.function.Function;

public class Conditional<T> {

    private final Function<T, Node<T,?>> condition;

    public Conditional(Function<T, Node<T,?>> condition) {
        this.condition = condition;
    }

    public Node<T,?> evaluate(T input) {
        return condition.apply(input);
    }

    public static <T> Conditional<T> eval(Function<T, Node<T,?>> condition) {
        return new Conditional<>(condition);
    }
}
