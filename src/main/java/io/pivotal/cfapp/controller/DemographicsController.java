package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.Demographics;
import io.pivotal.cfapp.service.DemographicsService;
import io.pivotal.cfapp.service.TkService;
import io.pivotal.cfapp.service.TkServiceUtil;
import reactor.core.publisher.Mono;

@RestController
public class DemographicsController {

    private final DemographicsService demoService;
    private final TkServiceUtil util;

    @Autowired
    public DemographicsController(
        DemographicsService demoService,
        TkService tkService
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