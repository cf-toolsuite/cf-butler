package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.cftoolsuite.cfapp.domain.Event;
import org.cftoolsuite.cfapp.domain.event.Events;
import org.cftoolsuite.cfapp.service.EventsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OnDemandEventsControllerTest extends ControllerTestBase {

    private EventsService service;
    private OnDemandEventsController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        service = mock(EventsService.class);
        controller = new OnDemandEventsController(service);
    }

    @Test
    void getEvents_whenNoTypes_returnsOk() {
        Event event = Event.builder().build();
        Events events = Events.builder().build();

        when(service.getEvents("app-1", 10)).thenReturn(Mono.just(events));
        when(service.toFlux(events)).thenReturn(Flux.just(event));

        Mono<ResponseEntity<List<Event>>> result = controller.getEvents("app-1", 10, null);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().size());
                })
                .verifyComplete();
    }

    @Test
    void getEvents_whenNoTypes_empty_returnsOk() {
        Events events = Events.builder().build();

        when(service.getEvents("app-1", 10)).thenReturn(Mono.just(events));
        when(service.toFlux(events)).thenReturn(Flux.empty());

        Mono<ResponseEntity<List<Event>>> result = controller.getEvents("app-1", 10, null);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertTrue(response.getBody().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getEvents_whenTypesProvided_returnsOk() {
        Event event = Event.builder().build();
        String[] types = new String[]{"audit", "security"};

        when(service.getEvents("app-1", types)).thenReturn(Flux.just(event));

        Mono<ResponseEntity<List<Event>>> result = controller.getEvents("app-1", null, types);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().size());
                })
                .verifyComplete();
    }

    @Test
    void getEvents_whenTypesProvided_empty_returnsOk() {
        String[] types = new String[]{"audit"};

        when(service.getEvents("app-1", types)).thenReturn(Flux.empty());

        Mono<ResponseEntity<List<Event>>> result = controller.getEvents("app-1", null, types);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertTrue(response.getBody().isEmpty());
                })
                .verifyComplete();
    }
}
