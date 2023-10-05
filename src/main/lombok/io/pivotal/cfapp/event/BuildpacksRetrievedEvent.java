package io.pivotal.cfapp.event;

import org.springframework.context.ApplicationEvent;

public class BuildpacksRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    public BuildpacksRetrievedEvent(Object source) {
        super(source);
    }

}
