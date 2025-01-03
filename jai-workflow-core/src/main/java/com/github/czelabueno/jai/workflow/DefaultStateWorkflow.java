package com.github.czelabueno.jai.workflow;

import com.github.czelabueno.jai.workflow.node.Conditional;
import com.github.czelabueno.jai.workflow.node.Node;
import com.github.czelabueno.jai.workflow.transition.Transition;
import com.github.czelabueno.jai.workflow.graph.GraphImageGenerator;
import com.github.czelabueno.jai.workflow.graph.graphviz.GraphvizImageGenerator;
import com.github.czelabueno.jai.workflow.transition.TransitionState;
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
    private final Map<Node<T,?>, List<TransitionState>> adjList;
    private volatile Node<T,?> startNode;
    private final T statefulBean;
    private final List<Transition> transitions;
    private GraphImageGenerator graphImageGenerator;

    @Builder
    public DefaultStateWorkflow(@NonNull T statefulBean,
                                @Singular List<Node<T,?>> addNodes,
                                GraphImageGenerator graphImageGenerator) {
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

    public void setGraphImageGenerator(GraphImageGenerator graphImageGenerator) {
        this.graphImageGenerator = graphImageGenerator;
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

    @Override
    public void startNode(Node<T,?> startNode){
        this.startNode = startNode;
    }

    @Override
    public Node<T, ?> getLastNode() {
        if (adjList.isEmpty() || adjList == null)
            throw new IllegalStateException("No nodes added to the workflow");

        return adjList.entrySet()
                .stream()
                .filter(entry -> entry.getValue().contains(WorkflowStateName.END))
                .<Node<T, ?>>map(Map.Entry::getKey)
                .findFirst()
                .orElseGet(() -> adjList.keySet()
                        .<Node<T, ?>>stream()
                        .reduce((first, second) -> second)
                        .orElseThrow());
    }

    @Override
    public T run() {
        transitions.clear(); // clean previous transitions
        log.debug("STARTING workflow in normal mode..");
        runNode(startNode);
        return statefulBean;
    }

    private void runNode(Node<T,?> node) {
        if (node == null) return;
        log.debug("Running node name: " + node.getName() + "..");
        if (node == startNode)
            transitions.add(Transition.from(WorkflowStateName.START, node));
        synchronized (statefulBean){
            node.execute(statefulBean);
        }
        List<TransitionState> nextNodes;
        synchronized (adjList) {
            nextNodes = adjList.get(node);
        }
        for (TransitionState nextNode : nextNodes) {
            if (nextNode instanceof WorkflowStateName) {
                WorkflowStateName next = (WorkflowStateName) nextNode;
                if (next == WorkflowStateName.END) {
                    log.debug("Reached END state");
                    transitions.add(Transition.from(node, WorkflowStateName.END));
                    return;
                }
                transitions.add(Transition.from(node, next));
            } else if (nextNode instanceof Node) {
                Node<T,?> next = (Node<T,?>) nextNode;
                transitions.add(Transition.from(node, next));
                runNode(next);
            } else if (nextNode instanceof Conditional) {
                Node<T,?> conditionalNode = ((Conditional<T>) nextNode).evaluate(statefulBean);
                transitions.add(Transition.from(node, conditionalNode));
                runNode(conditionalNode);
            }
        }
    }

    @Override
    public T runStream(Consumer<Node<T, ?>> eventConsumer) {
        transitions.clear(); // clean previous transitions
        log.debug("STARTING workflow in stream mode..");
        Queue<TransitionState> queue = new LinkedBlockingQueue<>();
        queue.add(startNode);
        transitions.add(Transition.from(WorkflowStateName.START, startNode));
        while (!queue.isEmpty()) {
            TransitionState current = queue.poll();
            if (current instanceof Node) {
                Node<T,?> currentNode = (Node<T,?>) current;
                //eventConsumer.accept(currentNode);
                synchronized (statefulBean){
                    currentNode.execute(statefulBean);
                }
                eventConsumer.accept(currentNode);
                List<TransitionState> nextNodes;
                synchronized (adjList) {
                    nextNodes = adjList.get(currentNode);
                }
                if (nextNodes != null) {
                    for (TransitionState next : nextNodes) {
                        if (next instanceof WorkflowStateName) {
                            WorkflowStateName nextState = (WorkflowStateName) next;
                            if (nextState == WorkflowStateName.END) {
                                transitions.add(Transition.from(currentNode, WorkflowStateName.END));
                                return statefulBean;
                            }
                            transitions.add(Transition.from(currentNode, next));
                            queue.add(next);
                        } else if (next instanceof Node) {
                            transitions.add(Transition.from(currentNode, next));
                            queue.add(next);
                        } else if (next instanceof Conditional) {
                            Node<T,?> conditionalNode = ((Conditional<T>) next).evaluate(statefulBean);
                            transitions.add(Transition.from(currentNode, conditionalNode));
                            queue.add(conditionalNode);
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
    public List<Transition> getComputedTransitions() {
        return new ArrayList<>(transitions);
    }

    public String prettyTransitions() {
        StringBuilder sb = new StringBuilder();
        Object lastTo = null;
        for (Transition transition : transitions) {
            if (transition.from().equals(lastTo)) {
                sb.append(" -> ").append(transition.to() instanceof Node ? ((Node) transition.to()).getName() : transition.to().toString());
            } else {
                if (sb.length() > 0) sb.append(" ");
                sb.append(transition.from() instanceof Node ? ((Node)transition.from()).getName() : transition.from().toString()).append(" -> ").append(transition.to() instanceof Node ? ((Node) transition.to()).getName() : transition.to().toString());
            }
            lastTo = transition.to() instanceof Node ? (Node) transition.to() : transition.to();
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
