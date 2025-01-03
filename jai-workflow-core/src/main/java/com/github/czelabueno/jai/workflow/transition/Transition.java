package com.github.czelabueno.jai.workflow.transition;

import com.github.czelabueno.jai.workflow.node.Node;
import com.github.czelabueno.jai.workflow.WorkflowStateName;
import lombok.NonNull;

/**
 * Represents a transition between two states in a workflow.
 * The states can be instances of {@link Node} or {@link WorkflowStateName}.
 *
 */
public record Transition(TransitionState from, TransitionState to) {

    /**
     * Constructs a Transition with the specified from and to states.
     *
     * @param from the starting state of the transition, must be an instance of {@link Node} or {@link WorkflowStateName}
     * @param to   the ending state of the transition, must be an instance of {@link Node} or {@link WorkflowStateName}
     * @throws IllegalArgumentException if the from state is {@link WorkflowStateName#END},
     *                                  if the to state is {@link WorkflowStateName#START},
     *                                  or if the transition is from {@link WorkflowStateName#START} to {@link WorkflowStateName#END}
     * @throws NullPointerException if the from or to state is null
     */
    public Transition(@NonNull TransitionState from, @NonNull TransitionState to) {
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

    /**
     * Creates a new Transition with the specified from and to states.
     *
     * @param from the starting state of the transition, must be an instance of {@link Node} or {@link WorkflowStateName}
     * @param to   the ending state of the transition, must be an instance of {@link Node} or {@link WorkflowStateName}
     * @return a new Transition instance
     */
    public static Transition from(TransitionState from, TransitionState to) {
        return new Transition(from, to);
    }

    /**
     * Returns a string representation of the transition.
     *
     * @return a string representation of the transition in the format "from -> to"
     */
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
