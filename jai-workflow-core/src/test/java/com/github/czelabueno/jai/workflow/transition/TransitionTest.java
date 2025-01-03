package com.github.czelabueno.jai.workflow.transition;

import com.github.czelabueno.jai.workflow.WorkflowStateName;
import com.github.czelabueno.jai.workflow.node.Node;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TransitionTest {

    @Test
    void should_build_transition_using_from() {
        Transition transition = Transition.from(
                Node.from("from", s -> s + "1"),
                Node.from("to", s -> s + "2")
        );

        assertThat(((Node)transition.from()).getName()).isEqualTo("from");
        assertThat(((Node)transition.to()).getName()).isEqualTo("to");

        assertThat(transition).hasToString("from -> to");
    }
    // Transition Node to Node
    @Test
    void should_build_transition_using_nodes() {
        // given
        Node<String, String> from = new Node("node1", s -> s + "1");
        Node<String, String> to = new Node("node2", s -> s + "2");
        // when
        Transition transition = Transition.from(from, to);
        // then
        assertThat(transition.from()).isEqualTo(from);
        assertThat(transition.to()).isEqualTo(to);

        assertThat(transition).hasToString("node1 -> node2");
    }
    // Transition Node to WorkflowState
    @Test
    void should_build_transition_using_node_and_workflowState() {
        // given
        Node<String, String> from = new Node("node1", s -> s + "1");
        WorkflowStateName to = WorkflowStateName.END;
        // when
        Transition transition = Transition.from(from, to);
        // then
        assertThat(transition.from()).isEqualTo(from);
        assertThat(transition.to()).isEqualTo(to);

        assertThat(transition).hasToString("node1 -> END");
    }
    // Transition WorkflowState to Node
    @Test
    void should_build_transition_using_workflowState_and_node() {
        // given
        WorkflowStateName from = WorkflowStateName.START;
        Node<String, String> to = new Node("node2", s -> s + "2");
        // when
        Transition transition = Transition.from(from, to);
        // then
        assertThat(transition.from()).isEqualTo(from);
        assertThat(transition.to()).isEqualTo(to);

        assertThat(transition).hasToString("START -> node2");
    }

    @Test
    void should_throw_illegalArgumentException_when_transition_from_END() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Transition.from(WorkflowStateName.END, Node.from("to", s -> s + "1")))
                .withMessage("Cannot transition from an END state");
    }

    @Test
    void should_throw_illegalArgumentException_when_transition_to_START() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Transition.from(Node.from("from", s -> s + "2"), WorkflowStateName.START))
                .withMessage("Cannot transition to a START state");
    }

    @Test
    void should_throw_illegalArgumentException_when_transition_from_START_to_END() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Transition.from(WorkflowStateName.START, WorkflowStateName.END))
                .withMessage("Cannot transition from START to END state");
    }
}
