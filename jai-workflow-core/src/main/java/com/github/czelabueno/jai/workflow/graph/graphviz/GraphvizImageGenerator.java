package com.github.czelabueno.jai.workflow.graph.graphviz;

import com.github.czelabueno.jai.workflow.WorkflowStateName;
import com.github.czelabueno.jai.workflow.node.Node;
import com.github.czelabueno.jai.workflow.transition.Transition;
import com.github.czelabueno.jai.workflow.graph.GraphImageGenerator;
import guru.nidi.graphviz.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Implementation of {@link GraphImageGenerator} that uses <a href="https://graphviz.org/">Graphviz</a> java library and DOT language to generate workflow images.
 */
public class GraphvizImageGenerator implements GraphImageGenerator {

    private static final Logger log = LoggerFactory.getLogger(GraphvizImageGenerator.class);

    private String dotFormat;
    private static final Format DEFAULT_IMAGE_FORMAT = Format.SVG;

    private GraphvizImageGenerator(GraphvizImageGeneratorBuilder builder) {
        this.dotFormat = builder.dotFormat;
    }

    /**
     * Returns a new builder instance for creating a {@link GraphvizImageGenerator}.
     *
     * @return a new {@link GraphvizImageGeneratorBuilder} instance
     */
    public static GraphvizImageGeneratorBuilder builder() {
        return new GraphvizImageGeneratorBuilder();
    }

    /**
     * Generates a graph image from the given list of transitions and saves it to the specified output path.
     *
     * @param transitions the list of transitions to generate the graph image from
     * @param outputPath  the path to save the generated graph image
     * @throws IOException if an I/O error occurs during image generation
     * @throws IllegalArgumentException if the output path is null or empty
     */
    @Override
    public void generateImage(List<Transition> transitions, String outputPath) throws IOException {
        if (outputPath == null || outputPath.isEmpty()) {
            throw new IllegalArgumentException("Output path can not be null or empty. Cannot generate image.");
        }
        // Generate image using Graphviz from dot format
        log.debug("Generating workflow image..");
        Graphviz.useEngine(new GraphvizJdkEngine()); // Use GraalJS as the default engine
        log.debug("Using default image format: " + DEFAULT_IMAGE_FORMAT);
        if (dotFormat == null) {
            if (transitions == null || transitions.isEmpty()) {
                throw new IllegalArgumentException("Transitions list can not be null or empty when dotFormat is null. Cannot generate image.");
            }
            dotFormat = defaultDotFormat(transitions);
        }
        log.debug("Using Dot format: " + System.lineSeparator() + dotFormat);
        log.debug("Saving workflow image..");
        Graphviz.fromString(dotFormat)
                .render(DEFAULT_IMAGE_FORMAT)
                .toFile(new File(outputPath));
        log.debug("Workflow image saved to: " + outputPath);
    }

    /**
     * Builder class for {@link GraphvizImageGenerator}.
     */
    public static class GraphvizImageGeneratorBuilder {
        private String dotFormat;

        /**
         * Sets the dot format for the graph image.
         *
         * @param dotFormat the dot format string
         * @return the current {@link GraphvizImageGeneratorBuilder} instance
         */
        public GraphvizImageGeneratorBuilder dotFormat(String dotFormat) {
            this.dotFormat = dotFormat;
            return this;
        }

        /**
         * Builds and returns a new {@link GraphvizImageGenerator} instance.
         *
         * @return a new {@link GraphvizImageGenerator} instance
         */
        public GraphvizImageGenerator build() {
            return new GraphvizImageGenerator(this);
        }
    }

    /**
     * Generates the default dot format string from the given list of transitions.
     *
     * @param transitions the list of transitions
     * @return the generated dot format string
     */
    private String defaultDotFormat(List<Transition> transitions) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph workflow {").append(System.lineSeparator());
        sb.append(" ").append("node [style=filled,fillcolor=lightgrey]").append(System.lineSeparator());
        sb.append(" ").append("rankdir=LR;").append(System.lineSeparator());
        sb.append(" ").append("beautify=true").append(System.lineSeparator());
        sb.append(System.lineSeparator());
        for (Transition transition : transitions) {
            if (transition.to() instanceof Node) {
                sb.append(" ") // NodeFrom -> NodeTo
                        .append(transition.from() instanceof Node ?
                                sanitizeNodeName(((Node) transition.from()).getName()) :
                                transition.from().toString().toLowerCase())
                        .append(" -> ")
                        .append(sanitizeNodeName(((Node) transition.to()).getName())).append(";")
                        .append(System.lineSeparator());
            } else if (transition.to() == WorkflowStateName.END && transition.from() instanceof Node) {
                sb.append(" ") // NodeFrom -> END
                        .append(sanitizeNodeName(((Node) transition.from()).getName()))
                        .append(" -> ")
                        .append(((WorkflowStateName) transition.to()).toString().toLowerCase()).append(";")
                        .append(System.lineSeparator())
                        .append(System.lineSeparator());
            } else {
                sb.append(" ") // NodeFrom -> NodeTo
                        .append(sanitizeNodeName(transition.from().toString().toLowerCase()))
                        .append(" -> ")
                        .append(sanitizeNodeName(transition.to().toString().toLowerCase())).append(";")
                        .append(System.lineSeparator());
            }
        }
        sb.append(" ")
                .append(WorkflowStateName.START.toString().toLowerCase()+" [shape=Mdiamond, fillcolor=\"orange\"];")
                .append(System.lineSeparator());
        sb.append(" ")
                .append(WorkflowStateName.END.toString().toLowerCase()+" [shape=Msquare, fillcolor=\"lightgreen\"];")
                .append(System.lineSeparator());
        sb.append("}");
        return sb.toString();
    }

    /**
     * Sanitizes the node name by removing special characters and converting it to camel case.
     *
     * @param nodeName the node name to sanitize
     * @return the sanitized node name
     */
    private static String sanitizeNodeName(String nodeName) {
        // Remove special characters
        String sanitized = nodeName.replaceAll("[^a-zA-Z0-9 ]", "");

        // Convert to camel case
        String[] words = sanitized.split(" ");
        StringBuilder camelCase = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) {
                continue;
            }
            if (i == 0) {
                camelCase.append(words[i].substring(0, 1).toUpperCase());
                if (words[i].length() > 1) {
                    camelCase.append(words[i].substring(1).toLowerCase());
                }
            } else {
                camelCase.append(words[i].substring(0, 1).toUpperCase());
                if (words[i].length() > 1) {
                    camelCase.append(words[i].substring(1).toLowerCase());
                }
            }
        }
        return camelCase.toString();
    }
}
