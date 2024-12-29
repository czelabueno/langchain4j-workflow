package com.github.czelabueno.jai.workflow.langchain4j;

import lombok.Data;

/**
 * AbstractStatefulBean is an abstract class that represents a stateful bean which is responsible for holding the state of the workflow.
 * The state is a combination of a question, input data, output data and a response generation.
 * Every execution of the workflow initiates a state, which is then transferred among the nodes during their execution.
 * <br>
 * Example:
 * <pre>
 * <code>
 * public class MyStatefulBean extends AbstractStatefulBean {
 *     // my input/output data fields
 *   }
 * </code>
 * </pre>
 */
@Data
public abstract class AbstractStatefulBean {

    private String question;
    private String generation;
}
