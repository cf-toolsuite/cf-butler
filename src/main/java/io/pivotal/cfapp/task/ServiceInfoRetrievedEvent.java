package io.pivotal.cfapp.task;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.ServiceDetail;

public class ServiceInfoRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<ServiceDetail> detail;

    public ServiceInfoRetrievedEvent(Object source) {
        super(source);
    }

    public ServiceInfoRetrievedEvent detail(List<ServiceDetail> detail) {
        this.detail = detail;
        return this;
    }

    public List<ServiceDetail> getDetail() {
        return detail;
    }


}
