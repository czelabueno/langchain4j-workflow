package dev.langchain4j.workflow;

public class DefaultWorkflow<T> {

    private T statefulBean;

    public DefaultWorkflow(T statefulBean) {
        this.statefulBean = statefulBean;
    }

    public static <T> DefaultWorkflow<T> addStatefulBan(T statefulBean) {
        return new DefaultWorkflow<>(statefulBean);
    }

    public StateWorkflow<T> build() {
        return new StateWorkflow<>(statefulBean);
    }
}
