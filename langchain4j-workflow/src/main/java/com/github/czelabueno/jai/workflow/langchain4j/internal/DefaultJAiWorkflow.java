package com.github.czelabueno.jai.workflow.langchain4j.internal;

import com.github.czelabueno.jai.workflow.DefaultStateWorkflow;
import com.github.czelabueno.jai.workflow.StateWorkflow;
import com.github.czelabueno.jai.workflow.langchain4j.AbstractStatefulBean;
import com.github.czelabueno.jai.workflow.langchain4j.JAiWorkflow;
import com.github.czelabueno.jai.workflow.node.Node;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

class DefaultJAiWorkflow<T extends AbstractStatefulBean> implements JAiWorkflow {

    private static final Logger log = LoggerFactory.getLogger(DefaultJAiWorkflow.class);

    private final Boolean runStream;
    private final Boolean generateWorkflowImage;
    private final Path workflowImageOutputPath;
    private final T statefulBean;
    private final List<Node<T,T>> nodes;
    private DefaultStateWorkflow<T> workflow;

    @Builder
    public DefaultJAiWorkflow(T statefulBean,
                              List<Node<T,T>> nodes,
                              Boolean runStream,
                              Boolean generateWorkflowImage,
                              Path workflowImageOutputPath) {
        this.runStream = getOrDefault(runStream, false);
        // check if workflowOutputPath is valid
        this.generateWorkflowImage = workflowImageOutputPath != null || getOrDefault(generateWorkflowImage, false);
        this.workflowImageOutputPath = workflowImageOutputPath;
        this.statefulBean = ensureNotNull(statefulBean, "statefulBean");
        this.nodes = ensureNotNull(nodes, "%s cannot be null. jAI workflow cannot created without nodes definition", "nodes");
        this.workflow = createWorkflow(statefulBean);
    }

    public StateWorkflow<T> workflow() {
        if (workflow == null) {
            workflow = createWorkflow(statefulBean);
        }
        return workflow;
    }

    public void setWorkflow(DefaultStateWorkflow<T> workflow) {
        this.workflow = workflow;
    }

    @Override
    public AiMessage answer(UserMessage question) {
        // Define a stateful bean
        this.statefulBean.setQuestion(question.text());
        // Run workflow in stream mode or not
        if (this.runStream) {
            workflow().runStream(node -> {
                log.debug("Node processed: " + node.getName());
            });
        } else {
            workflow().run();
        }
        // Generate workflow image if required
        if (this.generateWorkflowImage) {
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
        return AiMessage.from(this.statefulBean.getGeneration());
    }

    @Override
    public List<String> answerStream(UserMessage question) {
        return null; // TODO: Implement streaming response and condition last node execution has a StreamingChatCompletion node
    }

    private DefaultStateWorkflow<T> createWorkflow(T statefulBean) {
        return DefaultStateWorkflow.<T>builder()
                .statefulBean(statefulBean)
                .addNodes(nodes)
                .build();
    }
}
