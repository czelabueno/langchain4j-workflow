package com.github.czelabueno.jai.workflow.langchain4j;

import lombok.Data;

@Data
public abstract class AbstractStatefulBean {

    private String question;
    private String generation;
}
