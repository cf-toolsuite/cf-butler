package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.Workloads;
import io.pivotal.cfapp.domain.Workloads.WorkloadsBuilder;
import io.pivotal.cfapp.service.LegacyWorkloadsService;
import io.pivotal.cfapp.service.TkService;
import io.pivotal.cfapp.service.TkServiceUtil;
import reactor.core.publisher.Mono;

@RestController
public class LegacyWorkloadsController {

    private final LegacyWorkloadsService service;
    private final TkServiceUtil util;

    @Autowired
    public LegacyWorkloadsController(
        LegacyWorkloadsService service,
        TkService tkService) {
        this.service = service;
        this.util = new TkServiceUtil(tkService);
    }


    @GetMapping(value = { "/snapshot/detail/legacy" } )
    public Mono<ResponseEntity<Workloads>> getLegacyWorkloads(@RequestParam(value = "stacks", required = false) String stacks,
    @RequestParam(value = "services", required = false) String cfServices
    ) {
        final WorkloadsBuilder builder = Workloads.builder();
        Mono<ResponseEntity<Workloads>> result = null;
        if (stacks !=null && cfServices == null) {
            result = service
                    .getLegacyStackApplications(stacks)
                    .map(list -> builder.applications(list))
                    .flatMap(dwb -> util
                                    .getHeaders()
                                        .map(h -> new ResponseEntity<Workloads>(dwb.build(), h, HttpStatus.OK)))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        } else if (cfServices !=null && stacks == null) {
            result = service
                    .getLegacyServiceApplications(cfServices)
                    .map(list -> builder.appRelationship(list))
                    .flatMap(dwb -> util
                                    .getHeaders()
                                        .map(h -> new ResponseEntity<Workloads>(dwb.build(), h, HttpStatus.OK)))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        } else {
            result = Mono.just(ResponseEntity.badRequest().build());
        }
		return result;
    }
}