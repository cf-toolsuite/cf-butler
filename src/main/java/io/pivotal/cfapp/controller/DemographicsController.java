package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.Demographics;
import io.pivotal.cfapp.service.DemographicsService;
import reactor.core.publisher.Mono;

@RestController
public class DemographicsController {

    private final DemographicsService service;

    @Autowired
    public DemographicsController(
        DemographicsService service
    ) {
        this.service = service;
    }

    @GetMapping("/snapshot/demographics")
    public Mono<ResponseEntity<Demographics>> getDemographics() {
        return service.getDemographics()
                .map(d -> ResponseEntity.ok(d))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}