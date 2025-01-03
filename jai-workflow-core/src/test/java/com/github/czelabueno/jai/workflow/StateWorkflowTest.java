package com.github.czelabueno.jai.workflow;

import com.github.czelabueno.jai.workflow.node.Conditional;
import com.github.czelabueno.jai.workflow.node.Node;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StateWorkflowTest {
    class MyStatefulBean {
        int value = 0;

        @Override
        public String toString() {
            return "MyStatefulBean{" +
                    "value=" + value +
                    '}';
        }
    }

    private MyStatefulBean myStatefulBean;
    private StateWorkflow<MyStatefulBean> myWorkflow;
    private Node<MyStatefulBean, String> node1;
    private Node<MyStatefulBean, String> node2;
    private Node<MyStatefulBean, String> node3;
    private Node<MyStatefulBean, String> node4;

    @BeforeEach
    void setUp() {
        myStatefulBean = new MyStatefulBean();
        // Define functions for nodes
        Function<MyStatefulBean, String> node1Func = obj -> {
            obj.value +=1;
            System.out.println("Node 1: [" + obj.value + "]");
            return "Node1: processed function";
        };
        Function<MyStatefulBean, String> node2Func = obj -> {
            obj.value +=2;
            System.out.println("Node 2: [" + obj.value + "]");
            return "Node2: processed function";
        };
        Function<MyStatefulBean, String> node3Func = obj -> {
            obj.value +=3;
            System.out.println("Node 3: [" + obj.value + "]");
            return "Node3: processed function";
        };
        Function<MyStatefulBean, String> node4Func = obj -> {
            obj.value +=4;
            System.out.println("Node 4: [" + obj.value + "]");
            return "Node4: processed function";
        };

        node1 = Node.from("node1", node1Func);
        node2 = Node.from("node2", node2Func);
        node3 = Node.from("node3", node3Func);
        node4 = Node.from("node4", node4Func);

        myWorkflow = DefaultStateWorkflow.<MyStatefulBean>builder()
                .statefulBean(myStatefulBean)
                .addNodes(asList(node1, node2, node3, node4))
                .build();
    }

    @Test
    void should_add_transitions_and_run_workflow_and_return_statefulbean_modified() {
        myWorkflow.putEdge(node1, node2);
        myWorkflow.startNode(node1);
        myWorkflow.run();
        assertEquals(2, myWorkflow.getComputedTransitions().size()); // start -> node1 -> node2
        assertEquals(3, myStatefulBean.value);
    }

    @Test
    void should_add_transitions_and_run_stream_workflow_and_return_statefulbean_modified() {
        myWorkflow.putEdge(node1, node2);
        myWorkflow.startNode(node1);
        myWorkflow.runStream(node -> {
            assertThat(node.getName()).containsIgnoringCase("node");
            assertThat(node.getFunctionInput()).isNotNull(); // stateful bean must not be null
            assertThat(node.getFunctionOutput()).asString().containsIgnoringCase("processed function");
        });
        assertEquals(2, myWorkflow.getComputedTransitions().size()); // start -> node1 -> node2
        assertEquals(3, myStatefulBean.value);
    }

    @Test
    void should_start_node2_and_run_workflow_and_return_statefulbean_modified() {
        // given
        myWorkflow.putEdge(node1, node2);
        myWorkflow.putEdge(node2, node3);
        // when
        myWorkflow.startNode(node2); // start from node2
        myWorkflow.run();
        // then
        assertEquals(2, myWorkflow.getComputedTransitions().size()); // start -> node2 -> node3
        assertEquals(5, myStatefulBean.value);
    }

    @Test
    void should_run_workflow_with_conditional_node_and_return_statefulbean_modified() {
        myWorkflow.putEdge(node1, node2);
        myWorkflow.putEdge(node2, node3);
        myWorkflow.putEdge(node3, Conditional.eval(obj -> {
            if (obj.value > 6) {
                return node4;
            } else {
                return node2; // expected return node2
            }
        }));
        myWorkflow.startNode(node1);
        myWorkflow.run();
        assertEquals(6, myWorkflow.getComputedTransitions().size()); // start -> node1 -> node2 -> node3 -> node2 -> node3 -> node4
        assertEquals(15, myStatefulBean.value);
    }

    @Test
    void should_run_workflow_with_workflow_state_name_end() {
        myWorkflow.putEdge(node1, node2);
        myWorkflow.putEdge(node2, WorkflowStateName.END);
        myWorkflow.startNode(node1);
        myWorkflow.run();
        assertEquals(3, myWorkflow.getComputedTransitions().size()); // start -> node1 -> node2 -> end
        assertEquals(3, myStatefulBean.value);
    }

    @Test
    void should_run_workflow_print_pretty_transitions() {
        myWorkflow.putEdge(node1, node2);
        myWorkflow.startNode(node1);
        myWorkflow.run();
        String transitions = ((DefaultStateWorkflow)myWorkflow).prettyTransitions();
        assertThat(2).isEqualTo(myWorkflow.getComputedTransitions().size());
        assertThat(transitions).containsPattern("node\\d+ -> node\\d+");
    }

    @SneakyThrows(IOException.class)
    @Test
    void should_generate_workflow_image() {
        myWorkflow.putEdge(node1, node2);
        myWorkflow.putEdge(node2, WorkflowStateName.END);
        myWorkflow.startNode(node1);
        myWorkflow.run();
        String imagePath = "image/my-workflow-from-test.svg";
        myWorkflow.generateWorkflowImage(imagePath);
        Path filePath = Paths.get(imagePath);
        assertThat(Files.exists(filePath)).isTrue();
    }

    @Test
    void should_throw_illegalArgumentException_for_inconsistent_start_transition(){
        // given
        myWorkflow.putEdge(node1, WorkflowStateName.START);
        myWorkflow.putEdge(node2, node3);
        // when
        myWorkflow.startNode(node1);
        // then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> myWorkflow.run())
                .withMessage("Cannot transition to a START state");
    }
}
