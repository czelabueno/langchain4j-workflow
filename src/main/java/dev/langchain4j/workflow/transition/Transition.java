package dev.langchain4j.workflow.transition;

import dev.langchain4j.workflow.node.Node;
import dev.langchain4j.workflow.WorkflowStateName;
import lombok.Getter;

@Getter
public class Transition<T> {
    private final Node<T, ?> from;
    private final Object to; // Can be Node<T,?> or WorflowState

    public Transition(Node<T, ?> from, Object to) {
        this.from = from;
        this.to = to;
    }

    public static <T> Transition<T> from(Node<T, ?> from, Object to) {
        return new Transition<>(from, to);
    }

    @Override
    public String toString() {
        if (to instanceof Node) {
            return from.getName() + " -> " + ((Node<T, ?>) to).getName();
        } else if (to instanceof WorkflowStateName) {
            return from.getName() + " -> " + ((WorkflowStateName) to).toString();
        }
        return "";
    }
}
