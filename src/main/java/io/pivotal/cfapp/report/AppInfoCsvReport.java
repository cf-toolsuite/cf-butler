package io.pivotal.cfapp.report;

import java.time.LocalDateTime;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.task.AppInfoRetrievedEvent;

public class AppInfoCsvReport  {
    
	private ButlerSettings appSettings;
	
	public AppInfoCsvReport(ButlerSettings appSettings) {
		this.appSettings = appSettings;
	}

    public String generatePreamble() {
        StringBuffer preamble = new StringBuffer();
        preamble.append("Application inventory detail from ");
        preamble.append(appSettings.getApiHost());
        preamble.append(" generated ");
        preamble.append(LocalDateTime.now());
        preamble.append(".");
        return preamble.toString();
    }
    
    public String generateDetail(AppInfoRetrievedEvent event) {
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
    
}