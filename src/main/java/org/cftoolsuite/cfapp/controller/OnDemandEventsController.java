package org.cftoolsuite.cfapp.controller;

import java.util.List;

import org.cftoolsuite.cfapp.domain.Event;
import org.cftoolsuite.cfapp.service.EventsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@Profile("on-demand")
@RestController
public class OnDemandEventsController {

    private final EventsService service;

    @Autowired
    public OnDemandEventsController(EventsService service) {
        this.service = service;
    }

    @GetMapping("/events/{id}")
    public Mono<ResponseEntity<List<Event>>> getEvents(
            @PathVariable("id") String id,
            @RequestParam(value = "numberOfEvents", required = false) Integer numberOfEvents,
            // if you specify types[] they must be comma-separated
            @RequestParam(value = "types[]", required = false) String[] types
            ) {
        if (types == null) {
            return service
                    .getEvents(id, numberOfEvents)
                    .flatMapMany(json -> service.toFlux(json))
                    .collectList()
                    .map(ResponseEntity::ok)
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        } else {
            return service
                    .getEvents(id, types)
                    .collectList()
                    .map(ResponseEntity::ok)
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        }
    }

}
