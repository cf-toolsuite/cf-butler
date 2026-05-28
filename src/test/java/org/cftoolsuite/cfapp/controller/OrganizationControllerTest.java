package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.cftoolsuite.cfapp.domain.Organization;
import org.cftoolsuite.cfapp.service.OrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OrganizationControllerTest extends ControllerTestBase {

    private OrganizationService organizationService;
    private OrganizationController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        organizationService = mock(OrganizationService.class);
        controller = new OrganizationController(organizationService, tkService);
    }

    @Test
    void listAllOrganizations_whenDataAvailable_returnsOkWithList() {
        Organization org = new Organization("org1", "my-org");

        mockTimeKeeper();
        when(organizationService.findAll()).thenReturn(Flux.just(org));

        Mono<ResponseEntity<List<Organization>>> result = controller.listAllOrganizations();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().size());
                    assertEquals("my-org", response.getBody().get(0).getName());
                })
                .verifyComplete();

        verify(organizationService).findAll();
    }

    @Test
    void listAllOrganizations_whenEmpty_returnsOkWithEmptyList() {
        mockTimeKeeper();
        when(organizationService.findAll()).thenReturn(Flux.empty());

        Mono<ResponseEntity<List<Organization>>> result = controller.listAllOrganizations();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertTrue(response.getBody().isEmpty());
                })
                .verifyComplete();

        verify(organizationService).findAll();
    }

    @Test
    void listAllOrganizations_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.listAllOrganizations());

        verifyNoInteractions(organizationService);
    }

    @Test
    void organizationsCount_whenDataAvailable_returnsOkWithCount() {
        Organization org1 = new Organization("org1", "org1");
        Organization org2 = new Organization("org2", "org2");

        mockTimeKeeper();
        when(organizationService.findAll()).thenReturn(Flux.just(org1, org2));

        Mono<ResponseEntity<Long>> result = controller.organizationsCount();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(2L, response.getBody());
                })
                .verifyComplete();

        verify(organizationService).findAll();
    }

    @Test
    void organizationsCount_whenEmpty_returnsOkWithZero() {
        mockTimeKeeper();
        when(organizationService.findAll()).thenReturn(Flux.empty());

        Mono<ResponseEntity<Long>> result = controller.organizationsCount();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(0L, response.getBody());
                })
                .verifyComplete();

        verify(organizationService).findAll();
    }

    @Test
    void organizationsCount_whenTimeKeeperEmpty_returnsOkWithZero() {
        mockTimeKeeperEmpty();

        Mono<ResponseEntity<Long>> result = controller.organizationsCount();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(0L, response.getBody());
                })
                .verifyComplete();

        verifyNoInteractions(organizationService);
    }
}
