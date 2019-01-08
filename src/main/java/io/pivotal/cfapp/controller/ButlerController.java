package io.pivotal.cfapp.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.report.HistoricalRecordCsvReport;
import io.pivotal.cfapp.service.HistoricalRecordService;
import io.pivotal.cfapp.service.PoliciesService;
import io.pivotal.cfapp.task.HistoricalRecordRetrievedEvent;
import reactor.core.publisher.Mono;

@RestController
public class ButlerController {

	private final PoliciesService policiesService;
	private final HistoricalRecordService historicalRecordService;
	private final HistoricalRecordCsvReport report;
	
	@Autowired
	public ButlerController(
			ButlerSettings settings,
			PoliciesService policiesService,
			HistoricalRecordService historicalRecordService) {
		this.policiesService = policiesService;
		this.historicalRecordService = historicalRecordService;
		this.report = new HistoricalRecordCsvReport(settings);
	}

	@GetMapping(value = { "/report" }, produces = MediaType.TEXT_PLAIN_VALUE)
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
		        			report.generateDetail(event))));
	}
	
	@PostMapping("/policies")
	public Mono<ResponseEntity<Policies>> establishPolicies(@RequestBody Policies policies) {
		return policiesService.save(policies)
								.map(p -> ResponseEntity.ok(p));
	}
	
	@GetMapping(value = { "/policies" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Policies>> listPolicies() {
		return policiesService.findAll()
								.map(p -> ResponseEntity.ok(p))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/policies")
	public Mono<ResponseEntity<Void>> deletePolicies() {
		return policiesService.deleteAll()
								.map(p -> ResponseEntity.ok(p));
	}
}
