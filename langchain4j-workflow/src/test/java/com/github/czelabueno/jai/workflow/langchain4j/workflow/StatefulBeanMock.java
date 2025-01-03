package com.github.czelabueno.jai.workflow.langchain4j.workflow;

import com.github.czelabueno.jai.workflow.langchain4j.AbstractStatefulBean;
import lombok.Data;

import java.util.List;

@Data
public class StatefulBeanMock extends AbstractStatefulBean {

    private List<String> documents;
    private String webSearch;

    public StatefulBeanMock() {
    }
}
