package com.github.czelabueno.jai.workflow.langchain4j.node;

import com.github.czelabueno.jai.workflow.langchain4j.AbstractStatefulBean;
import com.github.czelabueno.jai.workflow.node.Node;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotBlank;

/**
 * StreamingNode is a specialized type of {@link Node} that handles streaming responses from a {@link StreamingChatLanguageModel}.
 * It extends the generic Node class with specific types for stateful beans and reactive streams.
 *
 * @param <T> the type of the stateful bean, which extends AbstractStatefulBean
 */
public class StreamingNode<T extends AbstractStatefulBean> extends Node<T, Flux<String>> {

    /**
     * Constructs a new StreamingNode with the specified name, messages, and StreamingChatLanguageModel.
     *
     * @param name the name of the node
     * @param messages the list of ChatMessage to be processed by the streamingChatLanguageModel
     * @param doUserMessage a function to generate a user message from the stateful bean
     * @param streamingChatLanguageModel the streaming chat language model to generate responses
     */
    public StreamingNode(String name,
                         List<ChatMessage> messages,
                         Function<T, ChatMessage> doUserMessage,
                         @NonNull StreamingChatLanguageModel streamingChatLanguageModel) {
        super(ensureNotBlank(name, "name"), (T statefulBean) -> streamingFunction(statefulBean, messages, doUserMessage, streamingChatLanguageModel));
    }

    /**
     * Creates a new StreamingNode from the specified parameters.
     *
     * @param name the name of the node
     * @param messages the list of ChatMessage to be processed by the streamingChatLanguageModel
     * @param doUserMessage a function to generate a user message from the stateful bean
     * @param streamingChatLanguageModel the streaming chat language model to generate responses
     * @param <T> the type of the stateful bean, which extends AbstractStatefulBean
     * @return a new StreamingNode instance
     */
    public static <T extends AbstractStatefulBean> StreamingNode<T> from(String name,
                                                                         List<ChatMessage> messages,
                                                                         Function<T, ChatMessage> doUserMessage,
                                                                         @NonNull StreamingChatLanguageModel streamingChatLanguageModel) {
        return new StreamingNode(name, messages, doUserMessage, streamingChatLanguageModel);
    }

    /**
     * Creates a new StreamingNode from the specified parameters.
     *
     * @param name the name of the node
     * @param doUserMessage a function to generate a user message from the stateful bean
     * @param streamingChatLanguageModel the streaming chat language model to generate responses
     * @param <T> the type of the stateful bean, which extends AbstractStatefulBean
     * @return a new StreamingNode instance
     */
    public static <T extends AbstractStatefulBean> StreamingNode<T> from(String name,
                                                                         Function<T, ChatMessage> doUserMessage,
                                                                         @NonNull StreamingChatLanguageModel streamingChatLanguageModel) {
        return from(name, null, doUserMessage, streamingChatLanguageModel);
    }

    /**
     * Creates a new StreamingNode from the specified parameters.
     *
     * @param name the name of the node
     * @param messages the list of ChatMessage to be processed by the streamingChatLanguageModel
     * @param streamingChatLanguageModel the streaming chat language model to generate responses
     * @param <T> the type of the stateful bean, which extends AbstractStatefulBean
     * @return a new StreamingNode instance
     */
    public static <T extends AbstractStatefulBean> StreamingNode<T> from(String name,
                                                                         List<ChatMessage> messages,
                                                                         @NonNull StreamingChatLanguageModel streamingChatLanguageModel) {
        return from(name, messages, null, streamingChatLanguageModel);
    }

    /**
     * Creates a new StreamingNode from the specified parameters.
     *
     * @param name the name of the node
     * @param streamingChatLanguageModel the streaming chat language model to generate responses
     * @param <T> the type of the stateful bean, which extends AbstractStatefulBean
     * @return a new StreamingNode instance
     */
    public static <T extends AbstractStatefulBean> StreamingNode<T> from(String name,
                                                                         @NonNull StreamingChatLanguageModel streamingChatLanguageModel) {
        return from(name, null, null, streamingChatLanguageModel);
    }

    /**
     * A static function that handles the token of responses from the StreamingChatLanguageModel.
     * It sets up a sink to collect the streamed tokens and completes the stateful bean with the final response.
     *
     * @param statefulBean the stateful bean holding the state of the workflow
     * @param messages the list of ChatMessage to be processed by the streamingChatLanguageModel
     * @param doUserMessage a function to generate a user message from the stateful bean
     * @param streamingChatLanguageModel the streaming chat language model to generate responses
     * @param <T> the type of the stateful bean, which extends AbstractStatefulBean
     * @return a Flux stream of the generated tokens
     */
    private static <T extends AbstractStatefulBean> Flux<String> streamingFunction(
            T statefulBean,
            List<ChatMessage> messages,
            Function<T, ChatMessage> doUserMessage,
            StreamingChatLanguageModel streamingChatLanguageModel) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        CompletableFuture<AiMessage> futureResponse = new CompletableFuture<>();
        if (messages == null || messages.isEmpty()) {
            messages = doUserMessage != null ?
                    List.of(doUserMessage.apply(statefulBean)) :
                    List.of(UserMessage.from(getOrDefault(statefulBean.getQuestion(),"No question provided.")));
        }

        streamingChatLanguageModel.generate(
                messages,
                new StreamingResponseHandler<AiMessage>() {
                    @Override
                    public void onNext(String token) {
                        sink.tryEmitNext(token);
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        futureResponse.complete(response.content());
                        sink.tryEmitComplete();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        sink.tryEmitError(throwable);
                    }
                }
        );
        statefulBean.setGenerationStream(sink.asFlux().cache());
        statefulBean.setGeneration(futureResponse.join().text());
        return statefulBean.getGenerationStream();
    }
}
