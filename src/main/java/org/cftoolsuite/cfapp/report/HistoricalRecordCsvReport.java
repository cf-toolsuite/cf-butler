package org.cftoolsuite.cfapp.report;

import java.time.LocalDateTime;

import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.domain.HistoricalRecord;
import org.cftoolsuite.cfapp.event.HistoricalRecordRetrievedEvent;

public class HistoricalRecordCsvReport {

    private PasSettings settings;

    public HistoricalRecordCsvReport(PasSettings settings) {
        this.settings = settings;
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

    public String generatePreamble() {
        StringBuffer preamble = new StringBuffer();
        preamble.append("Historical records from ");
        preamble.append(settings.getApiHost());
        preamble.append(" generated ");
        preamble.append(LocalDateTime.now());
        preamble.append(".");
        return preamble.toString();
    }

}
