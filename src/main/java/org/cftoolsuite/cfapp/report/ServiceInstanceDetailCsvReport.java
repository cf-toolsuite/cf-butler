package org.cftoolsuite.cfapp.report;

import java.time.LocalDateTime;

import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.domain.ServiceInstanceDetail;
import org.cftoolsuite.cfapp.event.ServiceInstanceDetailRetrievedEvent;

public class ServiceInstanceDetailCsvReport {

    private PasSettings settings;

    public ServiceInstanceDetailCsvReport(PasSettings settings) {
        this.settings = settings;
    }

    public String generateDetail(ServiceInstanceDetailRetrievedEvent event) {
        StringBuffer detail = new StringBuffer();
        detail.append("\n");
        detail.append(ServiceInstanceDetail.headers());
        detail.append("\n");
        event.getDetail()
        .forEach(a -> {
            detail.append(a.toCsv());
            detail.append("\n");
        });
        return detail.toString();
    }

    public String generatePreamble(LocalDateTime collectionTime) {
        StringBuffer preamble = new StringBuffer();
        preamble.append("Service inventory detail from ");
        preamble.append(settings.getApiHost());
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
