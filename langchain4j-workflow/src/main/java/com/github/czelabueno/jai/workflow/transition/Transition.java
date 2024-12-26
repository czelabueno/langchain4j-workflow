package com.github.czelabueno.jai.workflow.transition;

import com.github.czelabueno.jai.workflow.node.Node;
import com.github.czelabueno.jai.workflow.WorkflowStateName;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Transition {
    private final Object from; // // Can be Node<T,?> or WorflowState
    private final Object to; // Can be Node<T,?> or WorflowState

    public Transition(@NonNull Object from, @NonNull Object to) {
        if (from == WorkflowStateName.END) {
            throw new IllegalArgumentException("Cannot transition from an END state");
        }
        if (to == WorkflowStateName.START) {
            throw new IllegalArgumentException("Cannot transition to a START state");
        }
        if (from == WorkflowStateName.START && to == WorkflowStateName.END) {
            throw new IllegalArgumentException("Cannot transition from START to END state");
        }
        this.from = from;
        this.to = to;
    }

    public static Transition from(Object from, Object to) {
        return new Transition(from, to);
    }

    @Override
    public String toString() {
        String transition = "";
        if (from instanceof Node) {
            transition = ((Node) from).getName() + " -> ";
        } else if (from instanceof WorkflowStateName) {
            transition = ((WorkflowStateName) from).toString() + " -> ";
        } else {
            transition = from.toString() + " -> ";
        }
        if (to instanceof Node) {
            transition = transition + ((Node) to).getName();
        } else if (to instanceof WorkflowStateName) {
            transition = transition + ((WorkflowStateName) to).toString();
        } else {
            transition = transition + to.toString();
        }
        return transition;
    }
}
