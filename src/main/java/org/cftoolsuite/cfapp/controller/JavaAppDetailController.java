package org.cftoolsuite.cfapp.controller;

import java.util.Map;

import org.cftoolsuite.cfapp.service.JavaAppDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
public class JavaAppDetailController {

    private JavaAppDetailService service;

    @Autowired
    public JavaAppDetailController(JavaAppDetailService service) {
        this.service = service;
    }

    @GetMapping("/snapshot/detail/ai/spring")
    public ResponseEntity<Flux<Map<String, String>>> getSpringApplications() {
        return
            ResponseEntity
                .ok()
                .body(service.findSpringApplications());
    }

    @GetMapping("/snapshot/summary/ai/spring")
    public ResponseEntity<Mono<Map<String, Integer>>> calculateSpringDependencyFrequency() {
        return
            ResponseEntity
                .ok()
                .body(service.calculateSpringDependencyFrequency());
    }

}
