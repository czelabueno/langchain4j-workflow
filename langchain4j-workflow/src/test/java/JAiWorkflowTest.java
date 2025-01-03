import com.github.czelabueno.jai.workflow.langchain4j.JAiWorkflow;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

class JAiWorkflowTest {

    private JAiWorkflow jAiWorkflow;
    private UserMessage userMessage;
    private AiMessage aiMessage;

    @BeforeEach
    void setUp() {
        jAiWorkflow = mock(JAiWorkflow.class);
        userMessage = new UserMessage("What is the weather today?");
        aiMessage = new AiMessage("The weather is sunny today.");
    }

    @Test
    void should_answer_with_valid_question() {
        // given
        when(jAiWorkflow.answer(userMessage)).thenReturn(aiMessage);
        // when
        AiMessage response = jAiWorkflow.answer(userMessage);
        // then
        assertThat(response).isNotNull();
        assertThat(response.text()).isEqualTo("The weather is sunny today.");
        verify(jAiWorkflow, times(1)).answer(userMessage);
    }

    @Test
    void should_throw_exception_with_null_question() {
        // when
        when(jAiWorkflow.answer((String) null)).thenThrow(new NullPointerException("question"));
        // then
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> jAiWorkflow.answer((String) null))
                .withMessage("question");
        verify(jAiWorkflow, times(1)).answer((String) null);
    }

    @Test
    void should_answer_stream_with_valid_question() {
        // given
        when(jAiWorkflow.answerStream(userMessage)).thenReturn(Flux.just("The", "weather", "is", "sunny", "today."));
        // when
        Flux<String> response = jAiWorkflow.answerStream(userMessage);
        // then
        assertThat(response).isNotNull();
        StepVerifier.create(response)
                .expectNext("The", "weather", "is", "sunny", "today.")
                .verifyComplete();
        verify(jAiWorkflow, times(1)).answerStream(userMessage);
    }

    @Test
    void should_throw_exception_with_null_question_stream() {
        // when
        when(jAiWorkflow.answerStream((String) null)).thenThrow(new NullPointerException("question"));
        // then
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> jAiWorkflow.answerStream((String) null))
                .withMessage("question");
        verify(jAiWorkflow, times(1)).answerStream((String) null);
    }

    @Test
    void should_answer_stream_with_non_streamingNode() {
        // given
        when(jAiWorkflow.answerStream(userMessage)).thenThrow(new IllegalStateException("The last node of the workflow must be a StreamingNode to run in stream mode"));
        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> jAiWorkflow.answerStream(userMessage))
                .withMessage("The last node of the workflow must be a StreamingNode to run in stream mode");
        verify(jAiWorkflow, times(1)).answerStream(userMessage);
    }
}
