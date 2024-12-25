package dev.langchain4j.workflow.graph.graphviz;

import dev.langchain4j.workflow.WorkflowStateName;
import dev.langchain4j.workflow.graph.GraphImageGenerator;
import dev.langchain4j.workflow.node.Node;
import dev.langchain4j.workflow.transition.Transition;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GraphvizImageGenerator implements GraphImageGenerator {

    private static final Logger log = LoggerFactory.getLogger(GraphvizImageGenerator.class);

    private String dotFormat;
    private static final Format DEFAULT_IMAGE_FORMAT = Format.SVG;

    private GraphvizImageGenerator(GraphvizImageGeneratorBuilder builder) {
        this.dotFormat = builder.dotFormat;
    }

    public static GraphvizImageGeneratorBuilder builder() {
        return new GraphvizImageGeneratorBuilder();
    }

    @Override
    public void generateImage(List<Transition> transitions, String outputPath) throws IOException {
        if (outputPath == null || outputPath.isEmpty()) {
            throw new IllegalArgumentException("Output path can not be null or empty. Cannot generate image.");
        }
        // Generate image using Graphviz from dot format
        log.debug("Generating workflow image..");
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

    public static class GraphvizImageGeneratorBuilder {
        private String dotFormat;

        public GraphvizImageGeneratorBuilder dotFormat(String dotFormat) {
            this.dotFormat = dotFormat;
            return this;
        }

        public GraphvizImageGenerator build() {
            return new GraphvizImageGenerator(this);
        }
    }

    private String defaultDotFormat(List<Transition> transitions) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph workflow {").append(System.lineSeparator());
        sb.append(" ").append("node [style=filled,fillcolor=lightgrey]").append(System.lineSeparator());
        sb.append(" ").append("rankdir=LR;").append(System.lineSeparator());
        sb.append(" ").append("beautify=true").append(System.lineSeparator());
        sb.append(System.lineSeparator());
        for (Transition transition : transitions) {
            if (transition.getTo() instanceof Node) {
                sb.append(" ") // NodeFrom -> NodeTo
                        .append(transition.getFrom() instanceof Node ?
                                sanitizeNodeName(((Node) transition.getFrom()).getName()) :
                                transition.getFrom().toString().toLowerCase())
                        .append(" -> ")
                        .append(sanitizeNodeName(((Node) transition.getTo()).getName())).append(";")
                        .append(System.lineSeparator());
            } else if (transition.getTo() == WorkflowStateName.END && transition.getFrom() instanceof Node) {
                sb.append(" ") // NodeFrom -> END
                        .append(sanitizeNodeName(((Node) transition.getFrom()).getName()))
                        .append(" -> ")
                        .append(((WorkflowStateName) transition.getTo()).toString().toLowerCase()).append(";")
                        .append(System.lineSeparator())
                        .append(System.lineSeparator());
            } else {
                sb.append(" ") // NodeFrom -> NodeTo
                        .append(sanitizeNodeName(transition.getFrom().toString().toLowerCase()))
                        .append(" -> ")
                        .append(sanitizeNodeName(transition.getTo().toString().toLowerCase())).append(";")
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
