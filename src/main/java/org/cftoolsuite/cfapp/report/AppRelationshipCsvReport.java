package org.cftoolsuite.cfapp.report;

import java.time.LocalDateTime;

import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.domain.AppRelationship;
import org.cftoolsuite.cfapp.event.AppRelationshipRetrievedEvent;

public class AppRelationshipCsvReport  {

    private PasSettings appSettings;

    public AppRelationshipCsvReport(PasSettings appSettings) {
        this.appSettings = appSettings;
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

}
