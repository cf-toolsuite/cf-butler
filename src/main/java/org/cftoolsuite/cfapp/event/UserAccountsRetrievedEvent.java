package org.cftoolsuite.cfapp.event;

import java.util.List;

import org.cftoolsuite.cfapp.domain.UserAccounts;
import org.springframework.context.ApplicationEvent;

public class UserAccountsRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<UserAccounts> detail;

    public UserAccountsRetrievedEvent(Object source) {
        super(source);
    }

    public UserAccountsRetrievedEvent detail(List<UserAccounts> detail) {
        this.detail = detail;
        return this;
    }

    public List<UserAccounts> getDetail() {
        return detail;
    }

}
