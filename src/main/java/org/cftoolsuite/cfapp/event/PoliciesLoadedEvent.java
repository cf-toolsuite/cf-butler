package org.cftoolsuite.cfapp.event;

import org.cftoolsuite.cfapp.domain.Policies;
import org.springframework.context.ApplicationEvent;

public class PoliciesLoadedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private Policies policies;

    public PoliciesLoadedEvent(Object source) {
        super(source);
    }

    public Policies getPolicies() {
        return policies;
    }

    public PoliciesLoadedEvent policies(Policies policies) {
        this.policies = policies;
        return this;
    }

}
