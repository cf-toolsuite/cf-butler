package io.pivotal.cfapp.task;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEvent;

public class TkRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private LocalDateTime lastCollected;

    public TkRetrievedEvent(Object source) {
        super(source);
    }

    public TkRetrievedEvent lastCollected(LocalDateTime lastCollected) {
        this.lastCollected = lastCollected;
        return this;
    }

    public LocalDateTime getLastCollected() {
        return lastCollected;
    }

}
