package com.github.czelabueno.jai.workflow.langchain4j;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.List;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

public interface JAiWorkflow {

    default String answer(String question){
        ensureNotNull(question, "question");
        return answer(new UserMessage(question)).text();
    }
    AiMessage answer(UserMessage question);

    default List<String> answerStream(String question){
        ensureNotNull(question, "question");
        return answerStream(new UserMessage(question));
    }
    List<String> answerStream(UserMessage question);

}
