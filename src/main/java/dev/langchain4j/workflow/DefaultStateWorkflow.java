package dev.langchain4j.workflow;

import dev.langchain4j.workflow.transition.Transition;
import dev.langchain4j.workflow.node.Conditional;
import dev.langchain4j.workflow.node.Node;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class DefaultStateWorkflow<T> implements StateWorkflow<T> {

    private final Map<Node<T,?>, List<Object>> adjList;
    private volatile Node<T,?> startNode;
    private final T statefulBean;
    private final List<Transition<T>> transitions;

    @Builder
    public DefaultStateWorkflow(@NonNull T statefulBean,
                                @Singular List<Node<T,?>> addNodes) {
        if (addNodes.isEmpty()) {
            throw new IllegalArgumentException("At least one node must be added to the workflow");
        }

        this.statefulBean = statefulBean;
        this.adjList = new ConcurrentHashMap<>();
        this.transitions = Collections.synchronizedList(new ArrayList<>());

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
    public void putEdge(Node<T, ?> from, WorkflowState state) {
        adjList.get(from).add(state);
    }

    public void startNode(Node<T,?> startNode){
        this.startNode = startNode;
    }

    @Override
    public void run() {
        transitions.clear(); // clean previous transitions
        runNode(startNode);
    }

    private void runNode(Node<T,?> node) {
        if (node == null) return;
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
            } else if (nextNode == WorkflowState.END) {
                System.out.println("Reached END state");
                transitions.add(Transition.from(node, WorkflowState.END));
                return;
            }
        }
    }

    @Override
    public void runStream(Consumer<Node<T, ?>> eventConsumer) {
        transitions.clear(); // clean previous transitions
        Queue<Object> queue = new LinkedBlockingQueue<>();
        queue.add(startNode);
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
                        } else if (next == WorkflowState.END) {
                            System.out.println("Reached END state");
                            transitions.add(Transition.from(currentNode, WorkflowState.END));
                            return;
                        }
                    }
                }
            }
        }
    }

    public List<Transition<T>> getComputedTransitions() {
        return new ArrayList<>(transitions);
    }

    public String prettyTransitions() {
        StringBuilder sb = new StringBuilder();
        Node<T,?> lastTo = null;
        for (Transition<T> transition : transitions) {
            if (transition.getFrom().equals(lastTo)) {
                sb.append(" -> ").append(transition.getTo() instanceof Node ? ((Node<T,?>) transition.getTo()).getName() : transition.getTo().toString());
            } else {
                if (sb.length() > 0) sb.append(" ");
                sb.append(transition.getFrom().getName()).append(" -> ").append(transition.getTo() instanceof Node ? ((Node<T,?>) transition.getTo()).getName() : transition.getTo().toString());
            }
            lastTo = transition.getTo() instanceof Node ? (Node<T,?>) transition.getTo() : null;
        }
        return sb.toString();
    }

    // TODO - Allow override of default dot format
    @Override
    public String generateDotFormat(){
        StringBuilder sb = new StringBuilder();
        sb.append("digraph workflow {").append(System.lineSeparator());
        sb.append(" ").append("node [style=filled,fillcolor=lightgrey]").append(System.lineSeparator());
        sb.append(" ").append("rankdir=LR;").append(System.lineSeparator());
        sb.append(" ").append("beautify=true").append(System.lineSeparator());
        sb.append(System.lineSeparator());
        for (Transition<T> transition : transitions) {
            if (transition.getTo() instanceof Node) {
                sb.append(" ") // NodeFrom -> NodeTo
                    .append(transition.getFrom().getName())
                    .append(" -> ")
                    .append(((Node<T,?>) transition.getTo()).getName()).append(";")
                    .append(System.lineSeparator());
            } else if (transition.getTo() == WorkflowState.END) {
                sb.append(" ") // NodeFrom -> END
                    .append(transition.getFrom().getName())
                    .append(" -> ")
                    .append(((WorkflowState) transition.getTo()).toString().toLowerCase()).append(";")
                    .append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(" ").append(((WorkflowState) transition.getTo()).toString().toLowerCase()+" [shape=Msquare];")
                    .append(System.lineSeparator());
            }
        }
        sb.append("}");
        return sb.toString();
    }

    // TODO - Move to a separate class in graph package
    @Override
    public void generateWorkflowImage(String outputPath) throws IOException {
        String dotFormat = generateDotFormat();
        // Generate image using Graphviz
        Graphviz.fromString(dotFormat)
            .render(Format.SVG)
            .toFile(new File(outputPath));
    }
}
