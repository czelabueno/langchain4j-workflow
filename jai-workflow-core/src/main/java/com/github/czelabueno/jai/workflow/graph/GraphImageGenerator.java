package com.github.czelabueno.jai.workflow.graph;

import com.github.czelabueno.jai.workflow.transition.Transition;
import lombok.NonNull;

import java.io.IOException;
import java.util.List;

/**
 * Interface for generating graph images from workflow transitions computed.
 */
public interface GraphImageGenerator {

    /**
     * Generates a graph image from the given list of transitions and saves it to the default output path.
     *
     * @param transitions the list of transitions to generate the graph image from
     * @throws IOException if an I/O error occurs during image generation
     */
    default void generateImage(List<Transition> transitions) throws IOException {
        generateImage(transitions, "workflow-image.svg");
    }

    /**
     * Generates a graph image from the given list of transitions and saves it to the specified output path.
     *
     * @param transitions the list of transitions to generate the graph image from
     * @param outputPath  the path to save the generated graph image
     * @throws IOException if an I/O error occurs during image generation
     * @throws IllegalArgumentException if the output path is null or empty
     */
    void generateImage(List<Transition> transitions, @NonNull String outputPath) throws IOException;
}
