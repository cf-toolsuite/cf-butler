package io.pivotal.cfapp.report;

import java.time.LocalDateTime;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.AppRelationship;
import io.pivotal.cfapp.task.AppRelationshipRetrievedEvent;

public class AppRelationshipCsvReport  {

	private ButlerSettings appSettings;

	public AppRelationshipCsvReport(ButlerSettings appSettings) {
		this.appSettings = appSettings;
	}

    public String generatePreamble() {
        StringBuffer preamble = new StringBuffer();
        preamble.append("Application relationships from ");
        preamble.append(appSettings.getApiHost());
        preamble.append(" generated ");
        preamble.append(LocalDateTime.now());
        preamble.append(".");
        return preamble.toString();
    }

    public String generateDetail(AppRelationshipRetrievedEvent event) {
        StringBuffer details = new StringBuffer();
        details.append("\n");
        details.append(AppRelationship.headers());
        details.append("\n");
        event.getRelations()
                .forEach(a -> {
                    details.append(a.toCsv());
                    details.append("\n");
                });
        return details.toString();
    }
    
}