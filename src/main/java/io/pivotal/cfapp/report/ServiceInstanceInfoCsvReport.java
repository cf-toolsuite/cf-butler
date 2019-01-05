package io.pivotal.cfapp.report;

import java.time.LocalDateTime;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.ServiceDetail;
import io.pivotal.cfapp.task.ServiceInfoRetrievedEvent;

public class ServiceInstanceInfoCsvReport {

	private ButlerSettings settings;

	public ServiceInstanceInfoCsvReport(ButlerSettings settings) {
		this.settings = settings;
	}

    public String generatePreamble() {
    	StringBuffer preamble = new StringBuffer();
        preamble.append("Service inventory detail from ");
        preamble.append(settings.getApiHost());
        preamble.append(" generated ");
        preamble.append(LocalDateTime.now());
        preamble.append(".");
        return preamble.toString();
    }

    public String generateDetail(ServiceInfoRetrievedEvent event) {
    	StringBuffer detail = new StringBuffer();
        detail.append("\n");
        detail.append(ServiceDetail.headers());
        detail.append("\n");
        event.getDetail()
                .forEach(a -> { 
                    detail.append(a.toCsv());
                    detail.append("\n");
                });
        return detail.toString();
    }

}
