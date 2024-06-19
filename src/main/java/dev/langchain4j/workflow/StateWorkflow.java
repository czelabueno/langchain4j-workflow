package dev.langchain4j.workflow;

import dev.langchain4j.workflow.node.Conditional;
import dev.langchain4j.workflow.node.Node;

import java.io.IOException;
import java.util.function.Consumer;

public interface StateWorkflow<T> {
    void addNode(Node<T, ?> node);

    void putEdge(Node<T, ?> from, Node<T, ?> to);

    void putEdge(Node<T, ?> from, Conditional<T> conditional);

    void putEdge(Node<T, ?> from, WorkflowState state);

    void run();

    void runStream(Consumer<Node<T, ?>> eventConsumer);

    String generateDotFormat();

    void generateWorkflowImage(String outputPath) throws IOException;
}
