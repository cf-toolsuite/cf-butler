package io.pivotal.cfapp.task;

import io.pivotal.cfapp.domain.HistoricalRecord;
import io.pivotal.cfapp.service.HistoricalRecordService;

class HistoricalRecordPersister implements Runnable {

	private final HistoricalRecordService service;
	private final HistoricalRecord record;
	
	HistoricalRecordPersister(HistoricalRecordService service, HistoricalRecord record) {
		this.service = service;
		this.record = record;
	}
	
	@Override
	public void run() {
		service.save(record);
	}

	static HistoricalRecordPersister create(HistoricalRecordService service, HistoricalRecord record) {
		return new HistoricalRecordPersister(service, record);
	}
}
