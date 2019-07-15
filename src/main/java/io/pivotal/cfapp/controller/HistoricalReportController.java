package io.pivotal.cfapp.controller;

import java.time.Duration;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.event.HistoricalRecordRetrievedEvent;
import io.pivotal.cfapp.report.HistoricalRecordCsvReport;
import io.pivotal.cfapp.service.HistoricalRecordService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
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
	public Mono<ResponseEntity<String>> generateReport(@RequestParam(required = false, value = "start") LocalDate start, @RequestParam(required = false, value = "end") LocalDate end) {
		Mono<ResponseEntity<String>> result = null;
		boolean hasDateRange = start != null && end != null;
		boolean hasValidDateRange = hasDateRange && start.isBefore(end);
		if (!hasDateRange) {
			result = historicalRecordService
					.findAll()
					.collectList()
					.map(r ->
						new HistoricalRecordRetrievedEvent(this)
							.records(r)
					)
					.delayElement(Duration.ofMillis(500))
					.map(e -> ResponseEntity.ok(
								String.join(
										"\n\n",
										report.generatePreamble(),
										report.generateDetail(e))))
					.defaultIfEmpty(ResponseEntity.notFound().build());
		}
		if (hasValidDateRange) {
			result = historicalRecordService
					.findByDateRange(start, end)
					.collectList()
					.map(r ->
						new HistoricalRecordRetrievedEvent(this)
							.records(r)
					)
					.delayElement(Duration.ofMillis(500))
					.map(e -> ResponseEntity.ok(
								String.join(
										"\n\n",
										report.generatePreamble(),
										report.generateDetail(e))))
					.defaultIfEmpty(ResponseEntity.notFound().build());
		} else {
			log.error("The start date must be before end date when fetching historical records contrained by a date range.");
			result = Mono.just(ResponseEntity.badRequest().build());
		}
		return result;
	}
}
