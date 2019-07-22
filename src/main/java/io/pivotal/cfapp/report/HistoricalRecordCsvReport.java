package io.pivotal.cfapp.report;

import java.time.LocalDateTime;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.HistoricalRecord;
import io.pivotal.cfapp.event.HistoricalRecordRetrievedEvent;

public class HistoricalRecordCsvReport {

	private PasSettings settings;

	public HistoricalRecordCsvReport(PasSettings settings) {
		this.settings = settings;
	}

    public String generatePreamble() {
    	StringBuffer preamble = new StringBuffer();
        preamble.append("Historical records from ");
        preamble.append(settings.getApiHost());
        preamble.append(" generated ");
        preamble.append(LocalDateTime.now());
        preamble.append(".");
        return preamble.toString();
    }

    public String generateDetail(HistoricalRecordRetrievedEvent event) {
    	StringBuffer detail = new StringBuffer();
        detail.append("\n");
        detail.append(HistoricalRecord.headers());
        detail.append("\n");
        event.getRecords()
                .forEach(a -> {
                    detail.append(a.toCsv());
                    detail.append("\n");
                });
        return detail.toString();
    }

}
