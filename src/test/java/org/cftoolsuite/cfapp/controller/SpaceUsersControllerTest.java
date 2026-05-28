package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.cftoolsuite.cfapp.domain.SpaceUsers;
import org.cftoolsuite.cfapp.service.SpaceUsersService;
import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SpaceUsersControllerTest {

    private SpaceUsersService service;
    private TimeKeeperService tkService;
    private SpaceUsersController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = mock(SpaceUsersService.class);
        tkService = mock(TimeKeeperService.class);
        controller = new SpaceUsersController(service, tkService);
    }

    @Test
    void allAccountNames_whenDataAvailable_returnsOkWithNames() {
        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(service.obtainAccountNames()).thenReturn(Flux.just("user1", "user2"));

        Mono<ResponseEntity<List<String>>> result = controller.allAccountNames();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(2, response.getBody().size());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(service).obtainAccountNames();
    }

    @Test
    void allAccountNames_whenEmpty_returnsOkWithEmptyList() {
        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(service.obtainAccountNames()).thenReturn(Flux.empty());

        Mono<ResponseEntity<List<String>>> result = controller.allAccountNames();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertTrue(response.getBody().isEmpty());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(service).obtainAccountNames();
    }

    @Test
    void allAccountNames_whenTimeKeeperEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<List<String>>> result = controller.allAccountNames();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(service);
    }

    @Test
    void getAllSpaceUsers_whenDataAvailable_returnsOkWithUsers() {
        SpaceUsers su = SpaceUsers.builder().organization("org1").space("dev").build();

        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(service.findAll()).thenReturn(Flux.just(su));

        Mono<ResponseEntity<List<SpaceUsers>>> result = controller.getAllSpaceUsers();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().size());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(service).findAll();
    }

    @Test
    void getAllSpaceUsers_whenEmpty_returnsOkWithEmptyList() {
        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(service.findAll()).thenReturn(Flux.empty());

        Mono<ResponseEntity<List<SpaceUsers>>> result = controller.getAllSpaceUsers();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertTrue(response.getBody().isEmpty());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(service).findAll();
    }

    @Test
    void getUsersInOrganizationAndSpace_whenFound_returnsOk() {
        SpaceUsers su = SpaceUsers.builder().organization("myorg").space("dev").build();

        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(service.findByOrganizationAndSpace("myorg", "dev")).thenReturn(Mono.just(su));

        Mono<ResponseEntity<SpaceUsers>> result = controller.getUsersInOrganizationAndSpace("myorg", "dev");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("myorg", response.getBody().getOrganization());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(service).findByOrganizationAndSpace("myorg", "dev");
    }

    @Test
    void getUsersInOrganizationAndSpace_whenNotFound_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(service.findByOrganizationAndSpace("myorg", "dev")).thenReturn(Mono.empty());

        Mono<ResponseEntity<SpaceUsers>> result = controller.getUsersInOrganizationAndSpace("myorg", "dev");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(service).findByOrganizationAndSpace("myorg", "dev");
    }

    @Test
    void totalAccounts_whenDataAvailable_returnsOkWithCount() {
        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(service.obtainAccountNames()).thenReturn(Flux.just("user1", "user2", "user3"));

        Mono<ResponseEntity<Long>> result = controller.totalAccounts();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(3L, response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(service).obtainAccountNames();
    }

    @Test
    void totalAccounts_whenEmpty_returnsOkWithZero() {
        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(service.obtainAccountNames()).thenReturn(Flux.empty());

        Mono<ResponseEntity<Long>> result = controller.totalAccounts();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(0L, response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(service).obtainAccountNames();
    }

    @Test
    void totalAccounts_whenTimeKeeperEmpty_returnsOkWithZero() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<Long>> result = controller.totalAccounts();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(0L, response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(service);
    }
}
