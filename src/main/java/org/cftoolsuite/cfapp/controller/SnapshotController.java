package org.cftoolsuite.cfapp.controller;

import org.cftoolsuite.cfapp.domain.SnapshotDetail;
import org.cftoolsuite.cfapp.domain.SnapshotSummary;
import org.cftoolsuite.cfapp.service.SnapshotService;
import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.cftoolsuite.cfapp.service.TkServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class SnapshotController {

    private final SnapshotService snapshotService;
    private final TkServiceUtil util;

    @Autowired
    public SnapshotController(
            SnapshotService snapshotService,
            TimeKeeperService tkService
            ) {
        this.snapshotService = snapshotService;
        this.util = new TkServiceUtil(tkService);
    }

    @GetMapping(value = { "/snapshot/detail/ai" }, produces = MediaType.TEXT_PLAIN_VALUE )
    public Mono<ResponseEntity<String>> getApplicationInstanceCsvReport() {
        return util.getTimeCollected()
                .flatMap(tc -> snapshotService
                        .assembleCsvAIReport(tc)
                        .map(ResponseEntity::ok))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = { "/snapshot/detail/relations" }, produces = MediaType.TEXT_PLAIN_VALUE )
    public Mono<ResponseEntity<String>> getApplicationRelationshipsCsvReport() {
        return util.getTimeCollected()
                .flatMap(tc -> snapshotService
                        .assembleCsvRelationshipsReport(tc)
                        .map(ResponseEntity::ok))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/snapshot/detail")
    public Mono<ResponseEntity<SnapshotDetail>> getDetail() {
        return util.getHeaders()
                .flatMap(h -> snapshotService
                        .assembleSnapshotDetail()
                        .map(detail -> new ResponseEntity<SnapshotDetail>(detail, h, HttpStatus.OK)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = { "/snapshot/detail/si" }, produces = MediaType.TEXT_PLAIN_VALUE )
    public Mono<ResponseEntity<String>> getServiceInstanceCsvReport() {
        return util.getTimeCollected()
                .flatMap(tc -> snapshotService
                        .assembleCsvSIReport(tc)
                        .map(ResponseEntity::ok))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/snapshot/summary")
    public Mono<ResponseEntity<SnapshotSummary>> getSummary() {
        return util.getHeaders()
                .flatMap(h -> snapshotService
                        .assembleSnapshotSummary()
                        .map(summary -> new ResponseEntity<SnapshotSummary>(summary, h, HttpStatus.OK)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = { "/snapshot/detail/users" }, produces = MediaType.TEXT_PLAIN_VALUE )
    public Mono<ResponseEntity<String>> getUserAccountCsvReport() {
        return util.getTimeCollected()
                .flatMap(tc -> snapshotService
                        .assembleCsvUserAccountReport(tc)
                        .map(ResponseEntity::ok))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
