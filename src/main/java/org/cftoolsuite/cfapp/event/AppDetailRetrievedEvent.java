package org.cftoolsuite.cfapp.event;

import java.util.List;

import org.cftoolsuite.cfapp.domain.AppDetail;
import org.springframework.context.ApplicationEvent;

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
