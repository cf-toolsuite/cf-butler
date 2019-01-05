package io.pivotal.cfapp.task;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.AppDetail;

public class AppInfoRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<AppDetail> detail;

    public AppInfoRetrievedEvent(Object source) {
        super(source);
    }

    public AppInfoRetrievedEvent detail(List<AppDetail> detail) {
        this.detail = detail;
        return this;
    }

    public List<AppDetail> getDetail() {
        return detail;
    }

}
