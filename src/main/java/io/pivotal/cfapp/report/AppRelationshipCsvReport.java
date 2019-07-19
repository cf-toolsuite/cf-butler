package io.pivotal.cfapp.report;

import java.time.LocalDateTime;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.AppRelationship;
import io.pivotal.cfapp.event.AppRelationshipRetrievedEvent;

public class AppRelationshipCsvReport  {

	private PasSettings appSettings;

	public AppRelationshipCsvReport(PasSettings appSettings) {
		this.appSettings = appSettings;
	}

    public String generatePreamble(LocalDateTime collectionTime) {
        StringBuffer preamble = new StringBuffer();
        preamble.append("Application relationships from ");
        preamble.append(appSettings.getApiHost());
        if (collectionTime != null) {
            preamble.append(" collected ");
            preamble.append(collectionTime);
            preamble.append(" and");
        }
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