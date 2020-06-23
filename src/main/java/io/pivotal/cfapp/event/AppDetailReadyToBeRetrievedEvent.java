package io.pivotal.cfapp.event;

import org.springframework.context.ApplicationEvent;

public class AppDetailReadyToBeRetrievedEvent extends ApplicationEvent {
 
    private static final long serialVersionUID = 1L;

    public AppDetailReadyToBeRetrievedEvent(Object source) {
        super(source);
    }

}