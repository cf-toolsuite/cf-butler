package io.pivotal.cfapp.report;

import java.time.LocalDateTime;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.task.ServiceInstanceDetailRetrievedEvent;

public class ServiceInstanceDetailCsvReport {

	private ButlerSettings settings;

	public ServiceInstanceDetailCsvReport(ButlerSettings settings) {
		this.settings = settings;
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

}
