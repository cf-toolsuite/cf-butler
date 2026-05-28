package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.cftoolsuite.cfapp.domain.SpaceUsers;
import org.cftoolsuite.cfapp.service.SpaceUsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import org.springframework.http.HttpStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SpaceUsersControllerTest extends ControllerTestBase {

    private SpaceUsersService service;
    private SpaceUsersController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        service = mock(SpaceUsersService.class);
        controller = new SpaceUsersController(service, tkService);
    }

    @Test
    void allAccountNames_whenDataAvailable_returnsOkWithNames() {
        mockTimeKeeper();
        when(service.obtainAccountNames()).thenReturn(Flux.just("user1", "user2"));

        Mono<ResponseEntity<List<String>>> result = controller.allAccountNames();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(2, response.getBody().size());
                })
                .verifyComplete();

        verify(service).obtainAccountNames();
    }

    @Test
    void allAccountNames_whenEmpty_returnsOkWithEmptyList() {
        mockTimeKeeper();
        when(service.obtainAccountNames()).thenReturn(Flux.empty());

        Mono<ResponseEntity<List<String>>> result = controller.allAccountNames();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertTrue(response.getBody().isEmpty());
                })
                .verifyComplete();

        verify(service).obtainAccountNames();
    }

    @Test
    void allAccountNames_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.allAccountNames());

        verifyNoInteractions(service);
    }

    @Test
    void getAllSpaceUsers_whenDataAvailable_returnsOkWithUsers() {
        SpaceUsers su = SpaceUsers.builder().organization("org1").space("dev").build();

        mockTimeKeeper();
        when(service.findAll()).thenReturn(Flux.just(su));

        Mono<ResponseEntity<List<SpaceUsers>>> result = controller.getAllSpaceUsers();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().size());
                })
                .verifyComplete();

        verify(service).findAll();
    }

    @Test
    void getAllSpaceUsers_whenEmpty_returnsOkWithEmptyList() {
        mockTimeKeeper();
        when(service.findAll()).thenReturn(Flux.empty());

        Mono<ResponseEntity<List<SpaceUsers>>> result = controller.getAllSpaceUsers();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertTrue(response.getBody().isEmpty());
                })
                .verifyComplete();

        verify(service).findAll();
    }

    @Test
    void getUsersInOrganizationAndSpace_whenFound_returnsOk() {
        SpaceUsers su = SpaceUsers.builder().organization("myorg").space("dev").build();

        mockTimeKeeper();
        when(service.findByOrganizationAndSpace("myorg", "dev")).thenReturn(Mono.just(su));

        Mono<ResponseEntity<SpaceUsers>> result = controller.getUsersInOrganizationAndSpace("myorg", "dev");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("myorg", response.getBody().getOrganization());
                })
                .verifyComplete();

        verify(service).findByOrganizationAndSpace("myorg", "dev");
    }

    @Test
    void getUsersInOrganizationAndSpace_whenNotFound_returnsNotFound() {
        mockTimeKeeper();
        when(service.findByOrganizationAndSpace("myorg", "dev")).thenReturn(Mono.empty());

        assertNotFound(controller.getUsersInOrganizationAndSpace("myorg", "dev"));

        verify(service).findByOrganizationAndSpace("myorg", "dev");
    }

    @Test
    void totalAccounts_whenDataAvailable_returnsOkWithCount() {
        mockTimeKeeper();
        when(service.obtainAccountNames()).thenReturn(Flux.just("user1", "user2", "user3"));

        Mono<ResponseEntity<Long>> result = controller.totalAccounts();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(3L, response.getBody());
                })
                .verifyComplete();

        verify(service).obtainAccountNames();
    }

    @Test
    void totalAccounts_whenEmpty_returnsOkWithZero() {
        mockTimeKeeper();
        when(service.obtainAccountNames()).thenReturn(Flux.empty());

        Mono<ResponseEntity<Long>> result = controller.totalAccounts();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(0L, response.getBody());
                })
                .verifyComplete();

        verify(service).obtainAccountNames();
    }

    @Test
    void totalAccounts_whenTimeKeeperEmpty_returnsOkWithZero() {
        mockTimeKeeperEmpty();

        Mono<ResponseEntity<Long>> result = controller.totalAccounts();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(0L, response.getBody());
                })
                .verifyComplete();

        verifyNoInteractions(service);
    }
}
