package io.pivotal.cfapp.controller;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.AppEvent;
import io.pivotal.cfapp.service.AppEventsService;
import reactor.core.publisher.Mono;

@Profile("on-demand")
@RestController
public class OnDemandAppEventsController {

    private final AppEventsService service;

    public OnDemandAppEventsController(AppEventsService service) {
        this.service = service;
    }

    @GetMapping("/events/{id}")
    public Mono<ResponseEntity<List<AppEvent>>> getEvents(
        @PathVariable("id") String id,
        @RequestParam(value = "numberOfEvents", required = false) Integer numberOfEvents
    ) {
        return service
                .getEvents(id, numberOfEvents)
                    .flatMapMany(json -> service.toFlux(json))
                    .collectList()
                    .map(r -> ResponseEntity.ok(r))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}