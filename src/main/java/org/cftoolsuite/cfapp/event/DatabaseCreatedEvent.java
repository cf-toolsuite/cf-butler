package org.cftoolsuite.cfapp.event;

import org.springframework.context.ApplicationEvent;

public class DatabaseCreatedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    public DatabaseCreatedEvent(Object source) {
        super(source);
    }

}
