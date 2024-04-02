package org.cftoolsuite.cfapp.event;

import java.util.List;

import org.cftoolsuite.cfapp.domain.Space;
import org.springframework.context.ApplicationEvent;

public class SpacesRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<Space> spaces;

    public SpacesRetrievedEvent(Object source) {
        super(source);
    }

    public List<Space> getSpaces() {
        return spaces;
    }

    public SpacesRetrievedEvent spaces(List<Space> spaces) {
        this.spaces = spaces;
        return this;
    }

}
