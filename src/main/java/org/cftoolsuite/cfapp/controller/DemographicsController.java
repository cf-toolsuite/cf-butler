package org.cftoolsuite.cfapp.controller;

import org.cftoolsuite.cfapp.domain.Demographics;
import org.cftoolsuite.cfapp.service.DemographicsService;
import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.cftoolsuite.cfapp.service.TkServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class DemographicsController {

    private final DemographicsService demoService;
    private final TkServiceUtil util;

    @Autowired
    public DemographicsController(
            DemographicsService demoService,
            TimeKeeperService tkService
            ) {
        this.demoService = demoService;
        this.util = new TkServiceUtil(tkService);
    }

    @GetMapping("/snapshot/demographics")
    public Mono<ResponseEntity<Demographics>> getDemographics() {
        return util.getHeaders()
                .flatMap(h -> demoService
                        .getDemographics()
                        .map(d -> new ResponseEntity<>(d, h, HttpStatus.OK)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
