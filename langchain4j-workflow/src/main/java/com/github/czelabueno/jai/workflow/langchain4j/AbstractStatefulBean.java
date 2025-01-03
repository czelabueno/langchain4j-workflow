package com.github.czelabueno.jai.workflow.langchain4j;

import lombok.Data;
import reactor.core.publisher.Flux;

/**
 * AbstractStatefulBean is an abstract class that represents a stateful bean which is responsible for holding the state of the workflow.
 * The state is a combination of a question, input data, output data and a response generation.
 * Every execution of the workflow initiates a state, which is then transferred among the nodes during their execution.
 *
 * Here is the simplest example of a stateful bean:
 * <pre>{@code
 * public class MyStatefulBean extends AbstractStatefulBean {
 *     private List<String> documents;
 *     // other additional input/output fields that you want to store
 * }
 * }</pre>
 */
@Data
public abstract class AbstractStatefulBean {

    private String question;
    private String generation;
    private Flux<String> generationStream;
}
