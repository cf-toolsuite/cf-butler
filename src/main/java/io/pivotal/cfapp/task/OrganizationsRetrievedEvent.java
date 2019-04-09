package io.pivotal.cfapp.task;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.Organization;

public class OrganizationsRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<Organization> organizations;

    public OrganizationsRetrievedEvent(Object source) {
        super(source);
    }

    public OrganizationsRetrievedEvent organizations(List<Organization> organizations) {
        this.organizations = organizations;
        return this;
    }

    public List<Organization> getOrganizations() {
        return organizations;
    }

}
