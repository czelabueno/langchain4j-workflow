package dev.langchain4j.workflow.graph;

import dev.langchain4j.workflow.transition.Transition;
import lombok.NonNull;

import java.io.IOException;
import java.util.List;

public interface GraphImageGenerator {

    default void generateImage(List<Transition> transitions) throws IOException {
        generateImage(transitions, "workflow-image.svg");
    }
    void generateImage(List<Transition> transitions, @NonNull String outputPath) throws IOException;
}
