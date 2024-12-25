package dev.langchain4j.workflow.node;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

class ConditionalTest {

    @Test
    void test_validate_constructor() {
        Function<String, Node> condition = s -> Node.from(s, (String s1) -> s1 + "1");
        Conditional conditional = new Conditional(condition);
        assertThat(conditional).isNotNull();
    }

    @Test
    void test_null_condition_constructor() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> new Conditional(null))
                .withMessage("Condition function cannot be null");
    }

    @Test
    void test_evaluate_valid_input() {
        Function<String, Node> condition = (String s) -> {
            if (s.equals("test")) {
                return Node.from("node1", (String s1) -> s1 + "1");
            }
            return null;
        };
        Conditional conditional = new Conditional(condition);
        Node node = conditional.evaluate("test");
        assertThat(node).isNotNull();
        assertThat(node.getName()).isEqualTo("node1");

        Node nullNode = conditional.evaluate("other");
        assertThat(nullNode).isNull();
    }

    @Test
    void test_evaluate_null_input() {
        Function<String, Node> condition = s -> Node.from(s, (String s1) -> s1 + "1");
        Conditional conditional = new Conditional(condition);
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> conditional.evaluate(null))
                .withMessage("Function Input cannot be null");
    }

    @Test
    void test_eval_static_method() {
        Function<String, Node<String, ?>> condition = s -> Node.from(s, (String s1) -> s1 + "1");
        Conditional<String> conditional = Conditional.eval(condition);
        assertThat(conditional).isNotNull();
    }

    @Test
    void test_equals_and_hash() {
        Function<String, Node> condition1 = s -> Node.from(s, (String s1) -> s1 + "1");
        Function<String, Node> condition2 = s -> Node.from(s, (String s1) -> s1 + "2");
        Conditional conditional1 = new Conditional(condition1);
        Conditional conditional2 = new Conditional(condition1);
        Conditional conditional3 = new Conditional(condition2);
        assertThat(conditional1)
                .isEqualTo(conditional1)
                .isNotEqualTo(null)
                .isNotEqualTo(new Object())
                .isEqualTo(conditional2)
                .isNotEqualTo(conditional3)
                .hasSameHashCodeAs(conditional2);
    }

    @Test
    void test_toString() {
        Function<String, Node> condition = s -> Node.from(s, (String s1) -> s1 + "1");
        Conditional conditional = new Conditional(condition);
        assertThat(conditional.toString()).isEqualTo("Conditional{condition=" + condition + "}");
    }
}
