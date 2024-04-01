package org.cftoolsuite.cfapp.event;

import org.springframework.context.ApplicationEvent;

public class StacksRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    public StacksRetrievedEvent(Object source) {
        super(source);
    }

}
