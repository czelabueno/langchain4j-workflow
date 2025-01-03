package com.github.czelabueno.jai.workflow;

import com.github.czelabueno.jai.workflow.node.Conditional;
import com.github.czelabueno.jai.workflow.node.Node;
import com.github.czelabueno.jai.workflow.transition.Transition;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interface representing a state workflow.
 *
 * @param <T> the type of the stateful bean used in the workflow
 */
public interface StateWorkflow<T> {

    /**
     * Adds a node to the workflow.
     *
     * @param node the node to add
     */
    void addNode(Node<T, ?> node);

    /**
     * Creates an edge between two nodes in the workflow.
     *
     * @param from the starting node of the edge
     * @param to   the ending node of the edge
     */
    void putEdge(Node<T, ?> from, Node<T, ?> to);

    /**
     * Creates an edge between a node and a conditional node in the workflow.
     *
     * @param from        the starting node of the edge
     * @param conditional the conditional node to evaluate
     */
    void putEdge(Node<T, ?> from, Conditional<T> conditional);

    /**
     * Creates an edge between a node and a workflow state in the workflow.
     *
     * @param from  the starting node of the edge
     * @param state the workflow state to transition to
     */
    void putEdge(Node<T, ?> from, WorkflowStateName state);

    /**
     * Sets the starting node of the workflow.
     *
     * @param startNode the starting node
     */
    void startNode(Node<T,?> startNode);

    /**
     * Returns the last node defined in the workflow.
     *
     * @return the last node defined in the workflow
     */
    Node<T,?> getLastNode();

    /**
     * Runs the workflow synchronously.
     *
     * @return the stateful bean after the workflow execution
     */
    T run();

    /**
     * Runs the workflow in stream mode, consuming events with the specified consumer.
     *
     * @param eventConsumer the consumer to process node events
     * @return the stateful bean after the workflow execution
     */
    T runStream(Consumer<Node<T, ?>> eventConsumer);

    /**
     * Returns the list of computed transitions in the workflow.
     *
     * @return the list of computed transitions
     */
    List<Transition> getComputedTransitions();

    /**
     * Generates an image of the workflow and saves it to the specified output path.
     *
     * @param outputPath the path to save the workflow image
     * @throws IOException if an I/O error occurs
     */
    void generateWorkflowImage(String outputPath) throws IOException;

    /**
     * Generates an image of the workflow and saves it to the default path "workflow-image.svg".
     *
     * @throws IOException if an I/O error occurs
     */
    default void generateWorkflowImage() throws IOException {
        generateWorkflowImage("workflow-image.svg");
    }
}
