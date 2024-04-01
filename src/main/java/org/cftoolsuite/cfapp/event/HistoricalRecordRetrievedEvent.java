package org.cftoolsuite.cfapp.event;

import java.util.List;

import org.cftoolsuite.cfapp.domain.HistoricalRecord;
import org.springframework.context.ApplicationEvent;

public class HistoricalRecordRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<HistoricalRecord> records;

    public HistoricalRecordRetrievedEvent(Object source) {
        super(source);
    }

    public List<HistoricalRecord> getRecords() {
        return records;
    }

    public HistoricalRecordRetrievedEvent records(List<HistoricalRecord> records) {
        this.records = records;
        return this;
    }

}
