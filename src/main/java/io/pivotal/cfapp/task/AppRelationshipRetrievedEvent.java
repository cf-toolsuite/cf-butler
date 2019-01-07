package io.pivotal.cfapp.task;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.AppRelationship;

public class AppRelationshipRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<AppRelationship> relations;

    public AppRelationshipRetrievedEvent(Object source) {
        super(source);
    }

    public AppRelationshipRetrievedEvent relations(List<AppRelationship> relations) {
        this.relations = relations;
        return this;
    }

    public List<AppRelationship> getRelations() {
        return relations;
    }

}
