package dev.langchain4j.workflow.transition;

import dev.langchain4j.workflow.node.Node;
import dev.langchain4j.workflow.WorkflowStateName;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Transition<T> {
    private final Object from; // // Can be Node<T,?> or WorflowState
    private final Object to; // Can be Node<T,?> or WorflowState

    public Transition(@NonNull Object from, @NonNull Object to) {
        if (from == WorkflowStateName.END) {
            throw new IllegalArgumentException("Cannot transition from an END state");
        }
        if (to == WorkflowStateName.START) {
            throw new IllegalArgumentException("Cannot transition to a START state");
        }
        this.from = from;
        this.to = to;
    }

    public static <T> Transition<T> from(Object from, Object to) {
        return new Transition<>(from, to);
    }

    @Override
    public String toString() {
        String transition = "";
        if (from instanceof Node) {
            transition = ((Node<T, ?>) from).getName() + " -> ";
        } else if (from instanceof WorkflowStateName) {
            transition = ((WorkflowStateName) from).toString() + " -> ";
        }
        if (to instanceof Node) {
            transition = transition + ((Node<T, ?>) to).getName();
        } else if (to instanceof WorkflowStateName) {
            transition = transition + ((WorkflowStateName) to).toString();
        }
        return transition;
    }
}
