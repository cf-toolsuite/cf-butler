package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.SnapshotDetail;
import io.pivotal.cfapp.domain.SnapshotSummary;
import io.pivotal.cfapp.service.SnapshotService;
import io.pivotal.cfapp.service.TkService;
import io.pivotal.cfapp.service.TkServiceUtil;
import reactor.core.publisher.Mono;

@RestController
public class SnapshotController {

	private final SnapshotService service;
	private final TkServiceUtil util;

	@Autowired
	public SnapshotController(
		SnapshotService service,
		TkService tkService
	) {
		this.service = service;
		this.util = new TkServiceUtil(tkService);
	}

	@GetMapping("/snapshot/detail")
	public Mono<ResponseEntity<SnapshotDetail>> getDetail() {
		return util.getHeaders()
				.flatMap(h -> service
								.assembleSnapshotDetail()
								.map(detail -> new ResponseEntity<SnapshotDetail>(detail, h, HttpStatus.OK)))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping("/snapshot/summary")
	public Mono<ResponseEntity<SnapshotSummary>> getSummary() {
		return util.getHeaders()
				.flatMap(h -> service
								.assembleSnapshotSummary()
								.map(summary -> new ResponseEntity<SnapshotSummary>(summary, h, HttpStatus.OK)))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping(value = { "/snapshot/detail/si" }, produces = MediaType.TEXT_PLAIN_VALUE )
	public Mono<ResponseEntity<String>> getServiceInstanceCsvReport() {
		return util.getTimeCollected()
				.flatMap(tc -> service
								.assembleCsvSIReport(tc)
								.map(r -> ResponseEntity.ok(r)))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping(value = { "/snapshot/detail/ai" }, produces = MediaType.TEXT_PLAIN_VALUE )
	public Mono<ResponseEntity<String>> getApplicationInstanceCsvReport() {
		return util.getTimeCollected()
				.flatMap(tc -> service
								.assembleCsvAIReport(tc)
								.map(r -> ResponseEntity.ok(r)))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping(value = { "/snapshot/detail/users" }, produces = MediaType.TEXT_PLAIN_VALUE )
	public Mono<ResponseEntity<String>> getUserAccountCsvReport() {
		return util.getTimeCollected()
				.flatMap(tc -> service
								.assembleCsvUserAccountReport(tc)
								.map(r -> ResponseEntity.ok(r)))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}

}
