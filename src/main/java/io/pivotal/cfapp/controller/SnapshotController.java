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
import reactor.core.publisher.Mono;

@RestController
public class SnapshotController {

	private final SnapshotService service;

	@Autowired
	public SnapshotController(
		SnapshotService service) {
		this.service = service;
	}

	@GetMapping("/snapshot/detail")
	public Mono<ResponseEntity<SnapshotDetail>> getDetail() {
		return service
					.assembleSnapshotDetail()
							.map(detail -> ResponseEntity.ok(detail))
							.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping("/snapshot/summary")
	public Mono<ResponseEntity<SnapshotSummary>> getSummary() {
		return service
					.assembleSnapshotSummary()
					.map(summary -> ResponseEntity.ok(summary))
					.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping(value = { "/snapshot/detail/si" }, produces = MediaType.TEXT_PLAIN_VALUE )
	public Mono<ResponseEntity<String>> getServiceInstanceCsvReport() {
		return service
					.assembleCsvSIReport()
					.map(r -> ResponseEntity.ok(r));
	}

	@GetMapping(value = { "/snapshot/detail/ai" }, produces = MediaType.TEXT_PLAIN_VALUE )
	public Mono<ResponseEntity<String>> getApplicationInstanceCsvReport() {
		return service
					.assembleCsvAIReport()
					.map(r -> ResponseEntity.ok(r));
	}

}
