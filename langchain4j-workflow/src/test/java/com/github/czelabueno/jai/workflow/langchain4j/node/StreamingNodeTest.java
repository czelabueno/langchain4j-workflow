package com.github.czelabueno.jai.workflow.langchain4j.node;

import com.github.czelabueno.jai.workflow.langchain4j.AbstractStatefulBean;
import com.github.czelabueno.jai.workflow.langchain4j.node.StreamingNode;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

class StreamingNodeTest {

    private StreamingChatLanguageModel model;
    private MyStatefulBean statefulBean;
    private List<ChatMessage> messages;

    class MyStatefulBean extends AbstractStatefulBean{
        List<String> documents;

        public MyStatefulBean(List<String> documents) {
            this.documents = documents;
        }

        @Override
        public String toString() {
            return "MyStatefulBean{" +
                    "documents=" + documents +
                    '}';
        }
    }

    @BeforeEach
    void setUp() {
        model = mock(StreamingChatLanguageModel.class);
        statefulBean = new MyStatefulBean(List.of("document1", "document2"));
        messages = List.of(new UserMessage("What is the weather today?"));
    }

    @Test
    void should_create_streaming_node_using_from() {
        // given
        StreamingNode<MyStatefulBean> node = StreamingNode.from("streamingNode1", messages, model);
        // then
        assertThat(node).isNotNull();
        assertThat(node.getName()).isEqualTo("streamingNode1");
    }

    @Test
    void should_streaming_function_with_valid_inputs() {
        // given
        List<String> tokens = Arrays.asList("The", "weather", "is", "sunny", "today.");
        doAnswer(invocation -> {
            StreamingResponseHandler<AiMessage> handler = invocation.getArgument(1);
            tokens.forEach(handler::onNext);
            handler.onComplete(new Response<>(new AiMessage("The weather is sunny today.")));
            return null;
        }).when(model).generate(anyList(), any(StreamingResponseHandler.class));
        // when
        StreamingNode<MyStatefulBean> node = StreamingNode.from("streamingNode1", messages, model);
        node.execute(statefulBean);
        // then
        StepVerifier.create(statefulBean.getGenerationStream())
                .expectNext("The", "weather", "is", "sunny", "today.")
                .verifyComplete();
        assertThat(statefulBean.getGeneration()).isEqualTo("The weather is sunny today.");
    }

    @Test
    void should_throw_null_pointer_exception_if_streamingChatLanguageModel_is_null() {
        // then
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> StreamingNode.from("streamingNode1", messages, null))
                .withMessage("streamingChatLanguageModel is marked non-null but is null");
    }

    @Test
    void should_throw_illegal_argument_exception() {
        // then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> StreamingNode.from(null, messages, model))
                .withMessage("name cannot be null or blank");
    }
}
