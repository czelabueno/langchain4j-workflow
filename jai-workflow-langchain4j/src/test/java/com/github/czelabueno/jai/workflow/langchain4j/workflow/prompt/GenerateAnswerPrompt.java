package com.github.czelabueno.jai.workflow.langchain4j.workflow.prompt;

import dev.langchain4j.model.input.structured.StructuredPrompt;

@StructuredPrompt({
        "You are an assistant for question-answering tasks. ",
        "Use the following pieces of retrieved context to answer the question. ",
        "If you don't know the answer, just say that you don't know. ",
        "Use three sentences maximum and keep the answer concise.",

        "Question: {{question}} \n\n",
        "Context: {{context}} \n\n",
        "Answer:"
})
public class GenerateAnswerPrompt {

    private String question;
    private String context;

    public GenerateAnswerPrompt(String question, String context) {
        this.question = question;
        this.context = context;
    }
}
