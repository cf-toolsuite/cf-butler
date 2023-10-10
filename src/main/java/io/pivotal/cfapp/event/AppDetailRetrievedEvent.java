package io.pivotal.cfapp.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.AppDetail;

public class AppDetailRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<AppDetail> detail;

    public AppDetailRetrievedEvent(Object source) {
        super(source);
    }

    public AppDetailRetrievedEvent detail(List<AppDetail> detail) {
        this.detail = detail;
        return this;
    }

    public List<AppDetail> getDetail() {
        return detail;
    }

}
