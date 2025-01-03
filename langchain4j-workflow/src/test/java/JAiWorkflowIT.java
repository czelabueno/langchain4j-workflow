import com.github.czelabueno.jai.workflow.StateWorkflow;
import com.github.czelabueno.jai.workflow.WorkflowStateName;
import com.github.czelabueno.jai.workflow.langchain4j.internal.DefaultJAiWorkflow;
import com.github.czelabueno.jai.workflow.langchain4j.node.StreamingNode;
import com.github.czelabueno.jai.workflow.node.Node;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModelName;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import com.github.czelabueno.jai.workflow.langchain4j.workflow.NodeFunctionsMock;
import com.github.czelabueno.jai.workflow.langchain4j.workflow.StatefulBeanMock;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

// JAiWorkflowIT is an integration test class that demonstrates how to use JAiWorkflow with LangChain4j to build agentic systems and orchestrated AI workflows.
// The workflow tested in this class is a simple example that retrieves documents, grades them, and generates a summary of the documents using the Mistral AI API.
//
// Workflow definition:
// START -> Retrieve Node -> Grade Documents Node -> Generate Node -> END
//
// The setUp method initializes the JAiWorkflow and JAiWorkflowStreaming objects with the MistralAiChatModel and MistralAiStreamingChatModel classes, respectively.
// These models are used to generate AI responses in both synchronous and streaming modes.
//
// The should_answer_question method tests the synchronous answer method of the JAiWorkflow class by providing a question and checking if the answer contains the expected text.
// The should_answer_stream_question method tests the streaming answerStream method of the JAiWorkflow class by providing a question and checking if the answer contains the expected tokens.
//
// This integration test class showcases how JAiWorkflow and LangChain4j can be combined to create complex AI-driven workflows that can process and generate information in a structured manner.
class JAiWorkflowIT {

    String[] documents = new String[]{
            "https://lilianweng.github.io/posts/2023-06-23-agent/",
            "https://lilianweng.github.io/posts/2023-03-15-prompt-engineering/",
            "https://lilianweng.github.io/posts/2023-10-25-adv-attack-llm/"
    };

    ChatLanguageModel model = MistralAiChatModel.builder()
            .apiKey(System.getenv("MISTRAL_AI_API_KEY"))
            .modelName(MistralAiChatModelName.MISTRAL_LARGE_LATEST)
            .temperature(0.0)
            .build();

    StreamingChatLanguageModel streamingModel = MistralAiStreamingChatModel.builder()
            .apiKey(System.getenv("MISTRAL_AI_API_KEY"))
            .modelName(MistralAiChatModelName.MISTRAL_LARGE_LATEST)
            .temperature(0.0)
            .build();

    DefaultJAiWorkflow jAiWorkflow;
    DefaultJAiWorkflow jAiWorkflowStreaming;

    @BeforeEach()
    void setUp() {
        // Define a stateful bean to store the state of the workflow
        StatefulBeanMock statefulBean = new StatefulBeanMock();

        // Define nodes with your custom functions
        Node<StatefulBeanMock, StatefulBeanMock> retrieveNode = Node.from("Retrieve Node", obj -> NodeFunctionsMock.retrieve(obj, documents));
        Node<StatefulBeanMock, StatefulBeanMock> gradeDocumentsNode = Node.from("Grade Documents Node", obj -> NodeFunctionsMock.gradeDocuments(obj));
        Node<StatefulBeanMock, StatefulBeanMock> generateNode = Node.from("Generate Node", obj -> NodeFunctionsMock.generate(obj, model));
        StreamingNode<StatefulBeanMock> generateStreamingNode = StreamingNode.from(
                "Generate Node",
                obj -> NodeFunctionsMock.generateUserMessageFromStatefulBean(obj),
                streamingModel);

        // Build workflows of the synchronous and streaming ways
        jAiWorkflow = buildWorkflow(statefulBean, false, retrieveNode, gradeDocumentsNode, generateNode);
        jAiWorkflowStreaming = buildWorkflow(statefulBean, true, retrieveNode, gradeDocumentsNode, generateStreamingNode);
        // Define your workflow transitions using edges and the entry point of the workflow
        StateWorkflow workflow = jAiWorkflow.workflow();
        workflow.putEdge(retrieveNode, gradeDocumentsNode);
        workflow.putEdge(gradeDocumentsNode, generateNode);
        workflow.putEdge(generateNode, WorkflowStateName.END);
        workflow.startNode(retrieveNode);

        StateWorkflow workflowStreaming = jAiWorkflowStreaming.workflow();
        workflowStreaming.putEdge(retrieveNode, gradeDocumentsNode);
        workflowStreaming.putEdge(gradeDocumentsNode, generateStreamingNode);
        workflowStreaming.putEdge(generateStreamingNode, WorkflowStateName.END);
        workflowStreaming.startNode(retrieveNode);
    }

    @Test
    void should_answer_question() {
        // given
        String question = "Summarizes the importance of building agents with LLMs";

        // when
        String answer = jAiWorkflow.answer(question);

        // then
        assertThat(answer).containsIgnoringWhitespaces("brain of an autonomous agent system");
    }

    @Test
    void should_answer_stream_with_non_streamingNode_throw_IllegalStateException() {
        // given
        String question = "Summarizes the importance of building agents with LLMs";

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> jAiWorkflow.answerStream(question))
                .withMessage("The last node of the workflow must be a StreamingNode to run in stream mode");
    }

    @Test
    void should_answer_stream_question() {
        // given
        String question = "Summarizes the importance of building agents with LLMs";
        List<String> expectedTokens = Arrays.asList("building", "agent", "system","general","problem", "solver");

        // when
        Flux<String> tokens = jAiWorkflowStreaming.answerStream(question);

        // then
        StepVerifier.create(tokens)
                .expectNextMatches(token -> expectedTokens.stream().anyMatch(token.toLowerCase()::contains))
                .expectNextCount(1)
                .thenCancel()
                .verify();
        String answer = tokens.collectList().block().stream().collect(joining());
        assertThat(expectedTokens)
                .anySatisfy(token -> assertThat(answer).containsIgnoringWhitespaces(token));
    }

    private DefaultJAiWorkflow<StatefulBeanMock> buildWorkflow(StatefulBeanMock statefulBean, Boolean runStream, List<Node<StatefulBeanMock, ?>> nodes) {
        return DefaultJAiWorkflow.<StatefulBeanMock>builder()
                .statefulBean(statefulBean)
                .runStream(runStream)
                .nodes(nodes)
                .build();
    }

    private DefaultJAiWorkflow<StatefulBeanMock> buildWorkflow(StatefulBeanMock statefulBean, Boolean runStream, Node<StatefulBeanMock, ?>... nodes) {
        return buildWorkflow(statefulBean, runStream, Arrays.asList(nodes));
    }
}
