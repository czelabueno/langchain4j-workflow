package com.github.czelabueno.jai.workflow.node;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NodeTest {

    @Test
    void test_valid_constructor() {
        Node node = Node.from("node1", (String s) -> s + "1");
        assertThat(node.getName()).isEqualTo("node1");
    }

    @Test
    void test_null_node_name_constructor() {
        NullPointerException ilegal = assertThrows(NullPointerException.class, () -> Node.from(null, (String s) -> s + "1"));
        assertThat(ilegal.getMessage()).isEqualTo("name is marked non-null but is null");
    }

    @Test
    void test_illegal_empty_node_name() {
        IllegalArgumentException ilegal = assertThrows(IllegalArgumentException.class, () -> Node.from("", (String s) -> s + "1"));
        assertThat(ilegal.getMessage()).isEqualTo("Node name cannot be empty");
    }

    @Test
    void test_null_node_function_constructor() {
        NullPointerException ilegal = assertThrows(NullPointerException.class, () -> Node.from("node", null));
        assertThat(ilegal.getMessage()).isEqualTo("function is marked non-null but is null");
    }

    @Test
    void test_execute_valid_input() {
        Node node = Node.from("node1", (String s) -> s + "1");
        assertThat(node.getName()).isEqualTo("node1");
        assertThat(node.execute("test")).isEqualTo("test1");
        assertThat(node.getFunctionInput()).isEqualTo("test");
    }

    @Test
    void test_execute_function() {
        Function<Integer, String> sumToString = num -> {
            num += 1;
            return num.toString();
        };
        Node node = Node.from("node1", sumToString);
        assertThat(node.execute(1)).isEqualTo("2");
        assertThat(node.getFunctionInput()).isEqualTo(1);
        assertThat(node.getFunctionOutput()).isEqualTo("2");
    }

    @Test
    void test_execute_null_input() {
        Node node = Node.from("node1", (String s) -> s + "1");
        IllegalArgumentException ilegal = assertThrows(IllegalArgumentException.class, () -> node.execute(null));
        assertThat(ilegal.getMessage()).isEqualTo("Function input cannot be null");
    }

    @Test
    void test_equals_and_hash() {
        Function<String, String> function = (String s) -> s + "1";
        Node node1 = Node.from("node1", function);
        Node node2 = Node.from("node1", function);

        assertThat(node1)
                .isEqualTo(node1)
                .isNotEqualTo(null)
                .isNotEqualTo(new Object())
                .isEqualTo(node2)
                .hasSameHashCodeAs(node2);

        assertThat(Node.from("node1", (String s) -> s + "1")) // other function
                .isNotEqualTo(node1);
    }

    @Test
    void test_hashCode() {
        Function<String, String> function = (String s) -> s + "1";
        Node node1 = Node.from("node1", function);
        Node node2 = Node.from("node1", function);

        assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
    }

    @Test
    void test_toString() {
        Function<String, String> function = (String s) -> s + "1";
        Node node = Node.from("node1", function);

        assertThat(node.toString()).isEqualTo("Node{name='node1', function=" + function + "}");
    }
}
