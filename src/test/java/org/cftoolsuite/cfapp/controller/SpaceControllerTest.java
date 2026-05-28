package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.cftoolsuite.cfapp.domain.Space;
import org.cftoolsuite.cfapp.service.SpaceService;
import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SpaceControllerTest {

    private SpaceService spaceService;
    private TimeKeeperService tkService;
    private SpaceController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        spaceService = mock(SpaceService.class);
        tkService = mock(TimeKeeperService.class);
        controller = new SpaceController(spaceService, tkService);
    }

    @Test
    void listAllSpaces_whenDataAvailable_returnsOkWithList() {
        Space space = Space.builder().spaceId("space1").spaceName("dev").build();

        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(spaceService.findAll()).thenReturn(Flux.just(space));

        Mono<ResponseEntity<List<Space>>> result = controller.listAllSpaces();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().size());
                    assertEquals("dev", response.getBody().get(0).getSpaceName());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(spaceService).findAll();
    }

    @Test
    void listAllSpaces_whenEmpty_returnsOkWithEmptyList() {
        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(spaceService.findAll()).thenReturn(Flux.empty());

        Mono<ResponseEntity<List<Space>>> result = controller.listAllSpaces();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertTrue(response.getBody().isEmpty());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(spaceService).findAll();
    }

    @Test
    void listAllSpaces_whenTimeKeeperEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<List<Space>>> result = controller.listAllSpaces();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(spaceService);
    }

    @Test
    void spacesCount_whenDataAvailable_returnsOkWithCount() {
        Space space1 = Space.builder().spaceId("s1").spaceName("dev").build();
        Space space2 = Space.builder().spaceId("s2").spaceName("prod").build();

        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(spaceService.findAll()).thenReturn(Flux.just(space1, space2));

        Mono<ResponseEntity<Long>> result = controller.spacesCount();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(2L, response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(spaceService).findAll();
    }

    @Test
    void spacesCount_whenEmpty_returnsOkWithZero() {
        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(spaceService.findAll()).thenReturn(Flux.empty());

        Mono<ResponseEntity<Long>> result = controller.spacesCount();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(0L, response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(spaceService).findAll();
    }

    @Test
    void spacesCount_whenTimeKeeperEmpty_returnsOkWithZero() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<Long>> result = controller.spacesCount();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(0L, response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(spaceService);
    }
}
