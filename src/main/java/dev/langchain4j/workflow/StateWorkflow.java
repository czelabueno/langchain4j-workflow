package dev.langchain4j.workflow;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class StateWorkflow<T> {

    private final Map<Node<T,?>, List<Object>> adjList = new ConcurrentHashMap<>();
    private volatile Node<T,?> startNode;
    private final T statefulBean;
    private final List<Transition<T>> transitions = Collections.synchronizedList(new ArrayList<>());

    public StateWorkflow(T statefulBean) {
        this.statefulBean = statefulBean;
    }

    public void addNode(Node<T,?> node) {
        adjList.putIfAbsent(node, Collections.synchronizedList(new ArrayList<>()));
    }

    public void putEdge(Node<T,?> from, Node<T,?> to) {
        adjList.get(from).add(to);
    }

    public void putEdge(Node<T,?> from, Conditional<T> conditional) {
        adjList.get(from).add(conditional);
    }

    public void putEdge(Node<T,?> from, WorkflowState state) {
        adjList.get(from).add(state);
    }

    public void startNode(Node<T,?> startNode){
        this.startNode = startNode;
    }

    public void run() {
        transitions.clear();
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

    public void runStream(Consumer<Node<T,?>> eventConsumer) {
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

    public List<Transition<T>> getTransitions() {
        return new ArrayList<>(transitions);
    }
    public String prettyTransitions() {
        StringBuilder sb = new StringBuilder();
        Node<T,?> lastTo = null;
        for (Transition<T> transition : transitions) {
            if (transition.getFrom().equals(lastTo)) {
                sb.append(" -> ").append(transition.getTo() instanceof Node ? ((Node<T,?>) transition.getTo()).getName() : transition.getTo().toString());
            } else {
                if (!sb.isEmpty()) {
                    sb.append(" ");
                }
                sb.append(transition.getFrom().getName()).append(" -> ").append(transition.getTo() instanceof Node ? ((Node<T,?>) transition.getTo()).getName() : transition.getTo().toString());
            }
            lastTo = transition.getTo() instanceof Node ? (Node<T,?>) transition.getTo() : null;
        }
        return sb.toString();
    }
}
