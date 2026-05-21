package org.cftoolsuite.cfapp.event;

import java.util.Collections;
import java.util.List;

import org.cftoolsuite.cfapp.domain.Organization;
import org.springframework.context.ApplicationEvent;

public class OrganizationsRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private transient List<Organization> organizations;

    public OrganizationsRetrievedEvent(Object source) {
        super(source);
    }

    public List<Organization> getOrganizations() {
        return organizations == null ? Collections.emptyList() : Collections.unmodifiableList(organizations);
    }

    public OrganizationsRetrievedEvent organizations(List<Organization> organizations) {
        this.organizations = organizations;
        return this;
    }

}
