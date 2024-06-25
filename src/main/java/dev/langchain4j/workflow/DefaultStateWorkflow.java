package dev.langchain4j.workflow;

import dev.langchain4j.workflow.graph.GraphImageGenerator;
import dev.langchain4j.workflow.graph.graphviz.GraphvizImageGenerator;
import dev.langchain4j.workflow.transition.Transition;
import dev.langchain4j.workflow.node.Conditional;
import dev.langchain4j.workflow.node.Node;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class DefaultStateWorkflow<T> implements StateWorkflow<T> {

    private static final Logger log = LoggerFactory.getLogger(DefaultStateWorkflow.class);
    private final Map<Node<T,?>, List<Object>> adjList;
    private volatile Node<T,?> startNode;
    private final T statefulBean;
    private final List<Transition<T>> transitions;
    private final GraphImageGenerator<T> graphImageGenerator;

    @Builder
    public DefaultStateWorkflow(@NonNull T statefulBean,
                                @Singular List<Node<T,?>> addNodes,
                                GraphImageGenerator<T> graphImageGenerator) {
        if (addNodes.isEmpty()) {
            throw new IllegalArgumentException("At least one node must be added to the workflow");
        }

        this.statefulBean = statefulBean;
        this.adjList = new ConcurrentHashMap<>();
        this.transitions = Collections.synchronizedList(new ArrayList<>());

        if (graphImageGenerator != null) {
            this.graphImageGenerator = graphImageGenerator;
        } else {
            this.graphImageGenerator = GraphvizImageGenerator.<T>builder().build();
        }

        // Add nodes to adjList if they are not already present
        for (Node<T,?> node : addNodes) {
            this.adjList.putIfAbsent(node, Collections.synchronizedList(new ArrayList<>()));
        }
    }

    @Override
    public void addNode(Node<T, ?> node) {
        adjList.putIfAbsent(node, Collections.synchronizedList(new ArrayList<>()));
    }

    @Override
    public void putEdge(Node<T, ?> from, Node<T, ?> to) {
        adjList.get(from).add(to);
    }

    @Override
    public void putEdge(Node<T, ?> from, Conditional<T> conditional) {
        adjList.get(from).add(conditional);
    }

    @Override
    public void putEdge(Node<T, ?> from, WorkflowStateName state) {
        adjList.get(from).add(state);
    }

    public void startNode(Node<T,?> startNode){
        this.startNode = startNode;
    }

    @Override
    public T run() {
        transitions.clear(); // clean previous transitions
        runNode(startNode);
        return statefulBean;
    }

    private void runNode(Node<T,?> node) {
        if (node == null) return;
        log.debug("STARTING workflow in normally mode..");
        if (node == startNode)
            transitions.add(Transition.from(WorkflowStateName.START, node));
        synchronized (statefulBean){
            node.execute(statefulBean);
        }
        List<Object> nextNodes;
        synchronized (adjList) {
            nextNodes = adjList.get(node);
        }
        for (Object nextNode : nextNodes) {
            if (nextNode instanceof Node) {
                Node<T,?> next = (Node<T,?>) nextNode;
                transitions.add(Transition.from(node, next));
                runNode(next);
            } else if (nextNode instanceof Conditional) {
                Node<T,?> conditionalNode = ((Conditional<T>) nextNode).evaluate(statefulBean);
                transitions.add(Transition.from(node, conditionalNode));
                runNode(conditionalNode);
            } else if (nextNode == WorkflowStateName.END) {
                log.debug("Reached END state");
                transitions.add(Transition.from(node, WorkflowStateName.END));
                return;
            }
        }
    }

    @Override
    public T runStream(Consumer<Node<T, ?>> eventConsumer) {
        transitions.clear(); // clean previous transitions
        log.debug("STARTING workflow in stream mode..");
        Queue<Object> queue = new LinkedBlockingQueue<>();
        queue.add(startNode);
        transitions.add(Transition.from(WorkflowStateName.START, startNode));
        while (!queue.isEmpty()) {
            Object current = queue.poll();
            if (current instanceof Node) {
                Node<T,?> currentNode = (Node<T,?>) current;
                eventConsumer.accept(currentNode);
                synchronized (statefulBean){
                    currentNode.execute(statefulBean);
                }
                List<Object> nextNodes;
                synchronized (adjList) {
                    nextNodes = adjList.get(currentNode);
                }
                if (nextNodes != null) {
                    for (Object next : nextNodes) {
                        if (next instanceof Node) {
                            transitions.add(Transition.from(currentNode, next));
                            queue.add(next);
                        } else if (next instanceof Conditional) {
                            Node<T,?> conditionalNode = ((Conditional<T>) next).evaluate(statefulBean);
                            transitions.add(Transition.from(currentNode, conditionalNode));
                            queue.add(conditionalNode);
                        } else if (next == WorkflowStateName.END) {
                            transitions.add(Transition.from(currentNode, WorkflowStateName.END));
                            return statefulBean;
                        }
                    }
                }
            } else if (current == WorkflowStateName.END) {
                log.debug("Reached END state");
                return statefulBean;
            }
        }
        return statefulBean;
    }

    @Override
    public List<Transition<T>> getComputedTransitions() {
        return new ArrayList<>(transitions);
    }

    public String prettyTransitions() {
        StringBuilder sb = new StringBuilder();
        Object lastTo = null;
        for (Transition<T> transition : transitions) {
            if (transition.getFrom().equals(lastTo)) {
                sb.append(" -> ").append(transition.getTo() instanceof Node ? ((Node<T,?>) transition.getTo()).getName() : transition.getTo().toString());
            } else {
                if (sb.length() > 0) sb.append(" ");
                sb.append(transition.getFrom() instanceof Node ? ((Node<T,?>)transition.getFrom()).getName() : transition.getFrom().toString()).append(" -> ").append(transition.getTo() instanceof Node ? ((Node<T,?>) transition.getTo()).getName() : transition.getTo().toString());
            }
            lastTo = transition.getTo() instanceof Node ? (Node<T,?>) transition.getTo() : transition.getTo();
        }
        return sb.toString();
    }

    @Override
    public void generateWorkflowImage(String outputPath) throws IOException {
        try {
            Path path = Paths.get(outputPath);
            this.graphImageGenerator.generateImage(this.transitions, path.toAbsolutePath().toString()); // Absolute path by default
        } catch (InvalidPathException e) {
            log.warn("Invalid path: " + outputPath + " using default path");
            this.graphImageGenerator.generateImage(this.transitions);
        } catch (IOException e) {
            log.error("Error generating workflow image: " + e.getMessage());
            throw e;
        }
    }
}
