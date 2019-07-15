package io.pivotal.cfapp.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.report.HistoricalRecordCsvReport;
import io.pivotal.cfapp.service.HistoricalRecordService;
import io.pivotal.cfapp.event.HistoricalRecordRetrievedEvent;
import reactor.core.publisher.Mono;

@RestController
public class HistoricalReportController {

	private final HistoricalRecordService historicalRecordService;
	private final HistoricalRecordCsvReport report;

	@Autowired
	public HistoricalReportController(
			ButlerSettings butlerSettings,
			HistoricalRecordService historicalRecordService) {
		this.historicalRecordService = historicalRecordService;
		this.report = new HistoricalRecordCsvReport(butlerSettings);
	}

	@GetMapping(value = { "/policies/report" }, produces = MediaType.TEXT_PLAIN_VALUE)
	public Mono<ResponseEntity<String>> generateReport() {
		return historicalRecordService
				.findAll()
				.collectList()
		        .map(r ->
					new HistoricalRecordRetrievedEvent(this)
						.records(r)
				)
				.delayElement(Duration.ofMillis(500))
		        .map(event -> ResponseEntity.ok(
		        	String.join(
		        			"\n\n",
		        			report.generatePreamble(),
							report.generateDetail(event))))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

}
