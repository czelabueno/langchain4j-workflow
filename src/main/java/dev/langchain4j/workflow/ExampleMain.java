package dev.langchain4j.workflow;

import dev.langchain4j.workflow.node.Conditional;
import dev.langchain4j.workflow.node.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

public class ExampleMain {
        public static void main(String[] args) throws IOException {

            // Define a stateful bean
            class MyStatefulBean {
                int value = 0;
            }
            MyStatefulBean myStatefulBean = new MyStatefulBean();

            // Define functions for nodes
            Function<MyStatefulBean, String> node1Func = obj -> {
                obj.value +=1;
                System.out.println("Node 1: [" + obj.value + "]");
                return "Node1: function proceed";
            };
            Function<MyStatefulBean, String> node2Func = obj -> {
                obj.value +=2;
                System.out.println("Node 2: [" + obj.value + "]");
                return "Node2: function proceed";
            };
            Function<MyStatefulBean, String> node3Func = obj -> {
                obj.value +=3;
                System.out.println("Node 3: [" + obj.value + "]");
                return "Node3: function proceed";
            };
            Function<MyStatefulBean, String> node4Func = obj -> {
                obj.value +=4;
                System.out.println("Node 4: [" + obj.value + "]");
                return "Node4: function proceed";
            };

            // Create nodes
            Node<MyStatefulBean, String> node1 = Node.from("node1", node1Func);
            Node<MyStatefulBean, String> node2 = Node.from("node2", node2Func);
            Node<MyStatefulBean, String> node3 = Node.from("node3", node3Func);
            Node<MyStatefulBean, String> node4 = Node.from("node4", node4Func);


            // Create workflow
            DefaultStateWorkflow<MyStatefulBean> workflow = DefaultStateWorkflow.<MyStatefulBean>builder() //DefaultWorkflowTmp.addStatefulBan(myStatefulBean).build();
                    .statefulBean(myStatefulBean)
                    .addNodes(Arrays.asList(node1, node2, node3))
                    .build();

            // Add nodes to workflow
            /*workflow.addNode(node1);
            workflow.addNode(node2);
            workflow.addNode(node3); */
            workflow.addNode(node4);

            // Define edges
            workflow.putEdge(node1, node2);
            workflow.putEdge(node2, node3);
            workflow.putEdge(node3, Conditional.eval(obj -> {
                System.out.println("Stateful Value [" + obj.value + "]");
                if (obj.value > 6) {
                    return node4;
                } else {
                    return node2;
                }
            }));
            workflow.putEdge(node4, WorkflowState.END);

            // Start workflow
            workflow.startNode(node1);

            // Run workflow normally
            //workflow.run();

            // Run workflow in streaming mode
            workflow.runStream(node -> {
                System.out.println("Processing node: " + node.getName());
            });

            // Print transitions
            String transitions = workflow.prettyTransitions();
            System.out.println("Transitions: \n");
            System.out.println(transitions);

            // Dot format generated
            System.out.println(workflow.generateDotFormat());

            // Generate workflow image
            workflow.generateWorkflowImage("workflow.svg");
        }
}
