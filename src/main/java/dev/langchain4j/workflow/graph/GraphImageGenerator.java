package dev.langchain4j.workflow.graph;

import dev.langchain4j.workflow.transition.Transition;

import java.io.IOException;
import java.util.List;

public interface GraphImageGenerator<T> {

    default void generateImage(List<Transition<T>> transitions) throws IOException {
        if (transitions.isEmpty()) {
            throw new IllegalArgumentException(String.format("transitions list can not be empty, transition list size {%s}", 0));
        }
        generateImage(transitions, "workflow-image.svg");
    }
    void generateImage(List<Transition<T>> transitions, String outputPath) throws IOException;
}
