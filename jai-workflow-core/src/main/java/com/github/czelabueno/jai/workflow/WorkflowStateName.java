package com.github.czelabueno.jai.workflow;

import com.github.czelabueno.jai.workflow.transition.TransitionState;

/**
 * Enum representing the possible states in a workflow.
 * <p>
 * This class implements the {@link TransitionState} interface.
 */
public enum WorkflowStateName implements TransitionState {
    /**
     * The starting state of the workflow.
     */
    START,

    /**
     * The ending state of the workflow.
     */
    END
}
