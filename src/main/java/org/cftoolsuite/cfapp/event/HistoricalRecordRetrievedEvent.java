package org.cftoolsuite.cfapp.event;

import java.util.Collections;
import java.util.List;

import org.cftoolsuite.cfapp.domain.HistoricalRecord;
import org.springframework.context.ApplicationEvent;

public class HistoricalRecordRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private transient List<HistoricalRecord> records;

    public HistoricalRecordRetrievedEvent(Object source) {
        super(source);
    }

    public List<HistoricalRecord> getRecords() {
        return records == null ? Collections.emptyList() : Collections.unmodifiableList(records);
    }

    public HistoricalRecordRetrievedEvent records(List<HistoricalRecord> records) {
        this.records = records;
        return this;
    }

}
