package com.github.czelabueno.jai.workflow.graph;

import com.github.czelabueno.jai.workflow.transition.Transition;
import lombok.NonNull;

import java.io.IOException;
import java.util.List;

public interface GraphImageGenerator {

    default void generateImage(List<Transition> transitions) throws IOException {
        generateImage(transitions, "workflow-image.svg");
    }
    void generateImage(List<Transition> transitions, @NonNull String outputPath) throws IOException;
}
