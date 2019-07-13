package io.pivotal.cfapp.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.Space;

public class SpacesRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<Space> spaces;

    public SpacesRetrievedEvent(Object source) {
        super(source);
    }

    public SpacesRetrievedEvent spaces(List<Space> spaces) {
        this.spaces = spaces;
        return this;
    }

    public List<Space> getSpaces() {
        return spaces;
    }

}
