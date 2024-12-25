package dev.langchain4j.workflow.graph.graphviz;

import dev.langchain4j.workflow.transition.Transition;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraphvizImageGeneratorTest {

    GraphvizImageGenerator.GraphvizImageGeneratorBuilder builder;
    String dotFormat = "digraph { a -> b; }";

    @BeforeEach
    void setUp() {
        builder = GraphvizImageGenerator.builder();
    }

    @Test
    void test_builder_and_doFormat() {
        // given
        assertThat(builder).isNotNull();
        // when
        GraphvizImageGenerator generator = builder.dotFormat(dotFormat).build();
        // then
        assertThat(generator).isNotNull();
    }

    @Test
    void test_generateImage_invalid_transitions_and_doFormat() {
        // given
        List<Transition> transitions = Collections.EMPTY_LIST;
        // when
        GraphvizImageGenerator generator = builder.build(); // built without dotFormat
        // then
        assertThat(generator).isNotNull();
        assertThatThrownBy(() -> generator.generateImage(transitions)) // empty transitions
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transitions list can not be null or empty when dotFormat is null. Cannot generate image.");
    }

    @Test
    void test_generate_Image_invalid_outputPath() {
        // given
        List<Transition> transitions = Arrays.asList(new Transition("a", "b"));
        // when
        GraphvizImageGenerator generator = builder.build();
        // then
        assertThat(generator).isNotNull();
        assertThatThrownBy(() -> generator.generateImage(transitions, "")) // empty output path
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Output path can not be null or empty. Cannot generate image.");
    }


    @SneakyThrows
    @Test
    void test_generate_Image_with_custom_dotFormat() {
        // given
        GraphvizImageGenerator generator = builder.dotFormat("digraph { a -> b -> c -> d; }").build();
        // when
        assertThat(generator).isNotNull();
        generator.generateImage(null); // transitions are not required when dotFormat is provided
        // then
        Path path = Paths.get("workflow-image.svg");
        assertThat(Files.exists(path)).isTrue();
    }

    @SneakyThrows
    @Test
    void test_generate_Image_with_custom_outputPath() {
        // given
        GraphvizImageGenerator generator = builder.dotFormat(dotFormat).build();
        // when
        assertThat(generator).isNotNull();
        String customOutputPath = "image/my-workflow-from-test.svg";
        generator.generateImage(null, customOutputPath);
        // then
        Path path = Paths.get(customOutputPath);
        assertThat(Files.exists(path)).isTrue();
    }

    @SneakyThrows
    @Test
    void test_generate_Image_with_transition_and_default_dotFormat() {
        // given
        List<Transition> transitions = Arrays.asList(new Transition("a", "b"));
        GraphvizImageGenerator generator = builder.build();
        // when
        assertThat(generator).isNotNull();
        generator.generateImage(transitions);
        // then
        Path path = Paths.get("workflow-image.svg");
        assertThat(Files.exists(path)).isTrue();
    }

    @SneakyThrows
    @Test
    void test_generate_Image_is_SVG_format() {
        // given
        List<Transition> transitions = Arrays.asList(new Transition("a", "b"));
        GraphvizImageGenerator generator = builder.build();
        // when
        assertThat(generator).isNotNull();
        generator.generateImage(transitions);
        // then
        Path path = Paths.get("workflow-image.svg");
        assertThat(Files.exists(path)).isTrue();
        String content = String.join("\n", Files.readAllLines(path));
        assertThat(content.trim()).startsWith("<svg");
    }
}
