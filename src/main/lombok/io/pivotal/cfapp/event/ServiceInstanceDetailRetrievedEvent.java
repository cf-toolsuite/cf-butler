package io.pivotal.cfapp.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.ServiceInstanceDetail;

public class ServiceInstanceDetailRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<ServiceInstanceDetail> detail;

    public ServiceInstanceDetailRetrievedEvent(Object source) {
        super(source);
    }

    public ServiceInstanceDetailRetrievedEvent detail(List<ServiceInstanceDetail> detail) {
        this.detail = detail;
        return this;
    }

    public List<ServiceInstanceDetail> getDetail() {
        return detail;
    }


}
