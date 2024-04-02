package org.cftoolsuite.cfapp.report;

import java.time.LocalDateTime;

import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.event.AppDetailRetrievedEvent;

public class AppDetailCsvReport  {

    private PasSettings appSettings;

    public AppDetailCsvReport(PasSettings appSettings) {
        this.appSettings = appSettings;
    }

    public String generateDetail(AppDetailRetrievedEvent event) {
        StringBuffer details = new StringBuffer();
        details.append("\n");
        details.append(AppDetail.headers());
        details.append("\n");
        event.getDetail()
        .forEach(a -> {
            details.append(a.toCsv());
            details.append("\n");
        });
        return details.toString();
    }

    public String generatePreamble(LocalDateTime collectionTime) {
        StringBuffer preamble = new StringBuffer();
        preamble.append("Application inventory detail from ");
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
