package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.cftoolsuite.cfapp.domain.UserSpaces;
import org.cftoolsuite.cfapp.service.UserSpacesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class UserSpacesControllerTest extends ControllerTestBase {

    private UserSpacesService service;
    private UserSpacesController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        service = mock(UserSpacesService.class);
        controller = new UserSpacesController(service, tkService);
    }

    @Test
    void getSpacesForAccountName_whenFound_returnsOk() {
        UserSpaces userSpaces = UserSpaces.builder().accountName("testuser").build();

        mockTimeKeeper();
        when(service.getUserSpaces("testuser")).thenReturn(Mono.just(userSpaces));

        Mono<ResponseEntity<UserSpaces>> result = controller.getSpacesForAccountName("testuser");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("testuser", response.getBody().getAccountName());
                })
                .verifyComplete();
    }

    @Test
    void getSpacesForAccountName_whenNotFound_returnsNotFound() {
        mockTimeKeeper();
        when(service.getUserSpaces("testuser")).thenReturn(Mono.empty());

        assertNotFound(controller.getSpacesForAccountName("testuser"));
    }

    @Test
    void getSpacesForAccountName_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.getSpacesForAccountName("testuser"));
    }
}
