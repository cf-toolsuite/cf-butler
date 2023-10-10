package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.Workloads;
import io.pivotal.cfapp.domain.Workloads.WorkloadsBuilder;
import io.pivotal.cfapp.domain.WorkloadsFilter;
import io.pivotal.cfapp.service.LegacyWorkloadsService;
import io.pivotal.cfapp.service.TimeKeeperService;
import io.pivotal.cfapp.service.TkServiceUtil;
import io.pivotal.cfapp.util.CsvUtil;
import reactor.core.publisher.Mono;

@RestController
public class LegacyWorkloadsController {

    private final LegacyWorkloadsService service;
    private final TkServiceUtil util;

    @Autowired
    public LegacyWorkloadsController(
            LegacyWorkloadsService service,
            TimeKeeperService tkService) {
        this.service = service;
        this.util = new TkServiceUtil(tkService);
    }


    @GetMapping(value = { "/snapshot/detail/legacy" } )
    public Mono<ResponseEntity<Workloads>> getLegacyWorkloads(@RequestParam(value = "stacks", defaultValue = "", required = false) String stacks,
            @RequestParam(value = "service-offerings", defaultValue = "", required = false) String serviceOfferings
            ) {
        final WorkloadsBuilder builder = Workloads.builder();
        final WorkloadsFilter workloadsFilters = WorkloadsFilter.builder()
                .stacks(CsvUtil.parse(stacks))
                .serviceOfferings(CsvUtil.parse(serviceOfferings))
                .build();
        return service
                .getLegacyApplications(workloadsFilters)
                .map(list -> builder.applications(list))
                .then(service.getLegacyApplicationRelationships(workloadsFilters))
                .map(list -> builder.appRelationships(list))
                .flatMap(dwb -> util
                        .getHeaders()
                        .map(h -> new ResponseEntity<Workloads>(dwb.build(), h, HttpStatus.OK)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
