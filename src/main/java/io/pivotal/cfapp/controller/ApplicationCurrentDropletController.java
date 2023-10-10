package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.service.ApplicationCurrentDropletService;
import reactor.core.publisher.Mono;

@RestController
public class ApplicationCurrentDropletController {

    private final ApplicationCurrentDropletService service;

    @Autowired
    public ApplicationCurrentDropletController(ApplicationCurrentDropletService service) {
        this.service = service;
    }

    @GetMapping("/apps/{guid}/droplets/current")
    public Mono<ResponseEntity<String>> getCurrentDroplet(@PathVariable("guid") String guid) {
        return service
            .getCurrentDroplet(guid)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
