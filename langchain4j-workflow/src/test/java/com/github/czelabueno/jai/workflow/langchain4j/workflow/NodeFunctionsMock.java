package com.github.czelabueno.jai.workflow.langchain4j.workflow;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.document.transformer.jsoup.HtmlToTextDocumentTransformer;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.structured.StructuredPromptProcessor;
import dev.langchain4j.rag.content.Content;
import com.github.czelabueno.jai.workflow.langchain4j.workflow.prompt.GenerateAnswerPrompt;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class NodeFunctionsMock {

    private NodeFunctionsMock() {
    }

    public static StatefulBeanMock retrieve(StatefulBeanMock statefulBean, String... uris) {
        // Parse uris to documents
        List<Document> documents = new ArrayList<>();
        for (String uri : uris) {
            Document document = UrlDocumentLoader.load(uri,new TextDocumentParser());
            HtmlToTextDocumentTransformer transformer = new HtmlToTextDocumentTransformer(null, null, false);
            document = transformer.transform(document);
            documents.add(document);
        }
        // Mock retrieval that only gets the first 7 segments as a relevant document
        List<TextSegment> segments = DocumentSplitters
                .recursive(300,0)
                .splitAll(documents);
        List<Content> relevantDocuments = segments.stream()
                .limit(7)
                .map(segment -> new Content(segment.text()))
                .collect(toList());
        statefulBean.setDocuments(relevantDocuments.stream()
                .map(Content::textSegment)
                .map(TextSegment::text)
                .toList());
        return statefulBean;
    }

    public static StatefulBeanMock gradeDocuments(StatefulBeanMock statefulBean) {
        // Mock grading that return that doc is relevant
        List<String> docs = statefulBean.getDocuments();
        List<String> filteredDocs = docs.stream()
                .filter(doc -> doc.length() > 0) // feeble filter to return the first doc
                .toList();
        statefulBean.setDocuments(filteredDocs);
        statefulBean.setWebSearch("No"); // do not require go to web search because doc is relevant
        return statefulBean;
    }

    public static StatefulBeanMock generate(StatefulBeanMock statefulBean, ChatLanguageModel model) {
        String generation = model.generate(generateUserMessageFromStatefulBean(statefulBean).singleText());
        statefulBean.setGeneration(generation);
        return statefulBean;
    }

    public static UserMessage generateUserMessageFromStatefulBean(StatefulBeanMock statefulBean) {
        return UserMessage.from(answerPrompt(statefulBean).text());
    }

    private static Prompt answerPrompt(StatefulBeanMock statefulBean) {
        String question = statefulBean.getQuestion();
        String context = String.join("\n\n", statefulBean.getDocuments());
        GenerateAnswerPrompt generateAnswerPrompt = new GenerateAnswerPrompt(question, context);
        return StructuredPromptProcessor.toPrompt(generateAnswerPrompt);
    }
}
