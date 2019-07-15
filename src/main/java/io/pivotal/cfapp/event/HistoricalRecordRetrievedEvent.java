package io.pivotal.cfapp.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.HistoricalRecord;

public class HistoricalRecordRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<HistoricalRecord> records;

    public HistoricalRecordRetrievedEvent(Object source) {
        super(source);
    }

    public HistoricalRecordRetrievedEvent records(List<HistoricalRecord> records) {
        this.records = records;
        return this;
    }

    public List<HistoricalRecord> getRecords() {
        return records;
    }

}
