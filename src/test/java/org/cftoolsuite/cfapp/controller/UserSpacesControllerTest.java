package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.cftoolsuite.cfapp.domain.UserSpaces;
import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.cftoolsuite.cfapp.service.UserSpacesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class UserSpacesControllerTest {

    private UserSpacesService service;
    private TimeKeeperService tkService;
    private UserSpacesController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = mock(UserSpacesService.class);
        tkService = mock(TimeKeeperService.class);
        controller = new UserSpacesController(service, tkService);
    }

    @Test
    void getSpacesForAccountName_whenFound_returnsOk() {
        UserSpaces userSpaces = UserSpaces.builder().accountName("testuser").build();

        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(service.getUserSpaces("testuser")).thenReturn(Mono.just(userSpaces));

        Mono<ResponseEntity<UserSpaces>> result = controller.getSpacesForAccountName("testuser");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("testuser", response.getBody().getAccountName());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(service).getUserSpaces("testuser");
    }

    @Test
    void getSpacesForAccountName_whenNotFound_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(service.getUserSpaces("testuser")).thenReturn(Mono.empty());

        Mono<ResponseEntity<UserSpaces>> result = controller.getSpacesForAccountName("testuser");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(service).getUserSpaces("testuser");
    }

    @Test
    void getSpacesForAccountName_whenTimeKeeperEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<UserSpaces>> result = controller.getSpacesForAccountName("testuser");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(service);
    }
}
