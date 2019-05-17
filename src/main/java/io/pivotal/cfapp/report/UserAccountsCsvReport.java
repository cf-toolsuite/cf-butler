package io.pivotal.cfapp.report;

import java.time.LocalDateTime;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.UserAccounts;
import io.pivotal.cfapp.task.AppDetailRetrievedEvent;
import io.pivotal.cfapp.task.UserAccountsRetrievedEvent;

public class UserAccountsCsvReport  {

	private ButlerSettings appSettings;

	public UserAccountsCsvReport(ButlerSettings appSettings) {
		this.appSettings = appSettings;
	}

    public String generatePreamble() {
        StringBuffer preamble = new StringBuffer();
        preamble.append("User accounts from ");
        preamble.append(appSettings.getApiHost());
        preamble.append(" generated ");
        preamble.append(LocalDateTime.now());
        preamble.append(".");
        return preamble.toString();
    }

    public String generateDetail(UserAccountsRetrievedEvent event) {
        StringBuffer details = new StringBuffer();
        details.append("\n");
        details.append(UserAccounts.headers());
        details.append("\n");
        event.getDetail()
                .forEach(a -> {
                    details.append(a.toCsv());
                    details.append("\n");
                });
        return details.toString();
    }

}