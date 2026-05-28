package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.cftoolsuite.cfapp.domain.Space;
import org.cftoolsuite.cfapp.service.SpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import org.springframework.http.HttpStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SpaceControllerTest extends ControllerTestBase {

    private SpaceService spaceService;
    private SpaceController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        spaceService = mock(SpaceService.class);
        controller = new SpaceController(spaceService, tkService);
    }

    @Test
    void listAllSpaces_whenDataAvailable_returnsOkWithList() {
        Space space = Space.builder().spaceId("space1").spaceName("dev").build();

        mockTimeKeeper();
        when(spaceService.findAll()).thenReturn(Flux.just(space));

        Mono<ResponseEntity<List<Space>>> result = controller.listAllSpaces();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().size());
                    assertEquals("dev", response.getBody().get(0).getSpaceName());
                })
                .verifyComplete();

        verify(spaceService).findAll();
    }

    @Test
    void listAllSpaces_whenEmpty_returnsOkWithEmptyList() {
        mockTimeKeeper();
        when(spaceService.findAll()).thenReturn(Flux.empty());

        Mono<ResponseEntity<List<Space>>> result = controller.listAllSpaces();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertTrue(response.getBody().isEmpty());
                })
                .verifyComplete();

        verify(spaceService).findAll();
    }

    @Test
    void listAllSpaces_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.listAllSpaces());

        verifyNoInteractions(spaceService);
    }

    @Test
    void spacesCount_whenDataAvailable_returnsOkWithCount() {
        Space space1 = Space.builder().spaceId("s1").spaceName("dev").build();
        Space space2 = Space.builder().spaceId("s2").spaceName("prod").build();

        mockTimeKeeper();
        when(spaceService.findAll()).thenReturn(Flux.just(space1, space2));

        Mono<ResponseEntity<Long>> result = controller.spacesCount();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(2L, response.getBody());
                })
                .verifyComplete();

        verify(spaceService).findAll();
    }

    @Test
    void spacesCount_whenEmpty_returnsOkWithZero() {
        mockTimeKeeper();
        when(spaceService.findAll()).thenReturn(Flux.empty());

        Mono<ResponseEntity<Long>> result = controller.spacesCount();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(0L, response.getBody());
                })
                .verifyComplete();

        verify(spaceService).findAll();
    }

    @Test
    void spacesCount_whenTimeKeeperEmpty_returnsOkWithZero() {
        mockTimeKeeperEmpty();

        Mono<ResponseEntity<Long>> result = controller.spacesCount();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(0L, response.getBody());
                })
                .verifyComplete();

        verifyNoInteractions(spaceService);
    }
}
