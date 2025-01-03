package com.github.czelabueno.jai.workflow.langchain4j.internal;

import com.github.czelabueno.jai.workflow.DefaultStateWorkflow;
import com.github.czelabueno.jai.workflow.StateWorkflow;
import com.github.czelabueno.jai.workflow.langchain4j.AbstractStatefulBean;
import com.github.czelabueno.jai.workflow.langchain4j.JAiWorkflow;
import com.github.czelabueno.jai.workflow.langchain4j.node.StreamingNode;
import com.github.czelabueno.jai.workflow.node.Node;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * DefaultJAiWorkflow is a default implementation of the JAiWorkflow interface.
 * It defines the workflow for processing user messages and generating AI responses.
 *
 * @param <T> the type of the stateful bean, which extends AbstractStatefulBean
 */
public class DefaultJAiWorkflow<T extends AbstractStatefulBean> implements JAiWorkflow {

    private static final Logger log = LoggerFactory.getLogger(DefaultJAiWorkflow.class);

    private final Boolean runStream;
    private final Boolean generateWorkflowImage;
    private final Path workflowImageOutputPath;
    private final T statefulBean;
    private DefaultStateWorkflow<T> workflow;

    /**
     * Constructs a new DefaultJAiWorkflow with the specified parameters.
     *
     * @param statefulBean the stateful bean holding the state of the workflow
     * @param nodes the list of nodes to be processed in the workflow
     * @param runStream flag indicating whether to run the workflow in stream mode
     * @param generateWorkflowImage flag indicating whether to generate a workflow image
     * @param workflowImageOutputPath the output path for the workflow image
     */
    @Builder
    public DefaultJAiWorkflow(T statefulBean,
                              List<Node<T,?>> nodes,
                              Boolean runStream,
                              Boolean generateWorkflowImage,
                              Path workflowImageOutputPath) {
        this.statefulBean = ensureNotNull(statefulBean, "%s cannot be null. jAI workflow cannot created without stateful bean definition", "statefulBean");
        ensureNotNull(nodes, "%s cannot be null. jAI workflow cannot created without nodes definition", "nodes");
        this.workflow = createWorkflow(statefulBean, nodes);
        this.runStream = getOrDefault(runStream, false);
        // check if workflowOutputPath is valid
        this.generateWorkflowImage = workflowImageOutputPath != null || getOrDefault(generateWorkflowImage, false);
        this.workflowImageOutputPath = workflowImageOutputPath;
    }

    /**
     * Returns the current workflow.
     *
     * @return the current workflow
     */
    public StateWorkflow<T> workflow() {
        return this.workflow;
    }

    /**
     * Sets the workflow to the specified workflow.
     *
     * @param workflow the workflow to be set
     */
    public void setWorkflow(DefaultStateWorkflow<T> workflow) {
        this.workflow = workflow;
    }

    @Override
    public AiMessage answer(UserMessage question) {
        // Define a stateful bean
        this.statefulBean.setQuestion(question.singleText());
        // Run workflow in stream mode or not
        if (this.runStream) {
            workflow().runStream(node -> log.debug("Node processed: " + node.getName()));
        } else {
            workflow().run();
        }
        generateWorkflowImageIfNeeded();
        return AiMessage.from(this.statefulBean.getGeneration());
    }

    @Override
    public Flux<String> answerStream(UserMessage question) {
        if (!runStream || !isLastNodeAStreamingNode(workflow())) {
            throw new IllegalStateException("The last node of the workflow must be a StreamingNode to run in stream mode");
        }
        // Define a stateful bean
        this.statefulBean.setQuestion(question.singleText());
        // Run workflow in stream mode or not
        if (this.runStream) {
            workflow().runStream(node -> {
                if (node instanceof StreamingNode) {
                    log.debug("StreamingNode processed: " + node.getName());
                }
                log.debug("Node processed: " + node.getName());
            });
        }
        generateWorkflowImageIfNeeded();
        return this.statefulBean.getGenerationStream();
    }

    private DefaultStateWorkflow<T> createWorkflow(
            T statefulBean,
            List<Node<T, ?>> nodes) {
        return DefaultStateWorkflow.<T>builder()
                .statefulBean(statefulBean)
                .addNodes(nodes)
                .build();
    }

    private Boolean isLastNodeAStreamingNode(StateWorkflow<T> workflow) {
        return workflow.getLastNode() instanceof StreamingNode;
    }

    private void generateWorkflowImageIfNeeded() {
        // Generate workflow image if required
        if (generateWorkflowImage) {
            try {
                if (workflowImageOutputPath != null) {
                    workflow().generateWorkflowImage(workflowImageOutputPath.toAbsolutePath().toString());
                } else {
                    workflow().generateWorkflowImage();
                }
            } catch (IOException e) { // Generate image is not blocking the workflow execution
                log.error("Error generating workflow image", e);
            }
        }
    }
}
