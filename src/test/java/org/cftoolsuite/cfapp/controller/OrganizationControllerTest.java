package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.cftoolsuite.cfapp.domain.Organization;
import org.cftoolsuite.cfapp.service.OrganizationService;
import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OrganizationControllerTest {

    private OrganizationService organizationService;
    private TimeKeeperService tkService;
    private OrganizationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        organizationService = mock(OrganizationService.class);
        tkService = mock(TimeKeeperService.class);
        controller = new OrganizationController(organizationService, tkService);
    }

    @Test
    void listAllOrganizations_whenDataAvailable_returnsOkWithList() {
        Organization org = new Organization("org1", "my-org");

        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(organizationService.findAll()).thenReturn(Flux.just(org));

        Mono<ResponseEntity<List<Organization>>> result = controller.listAllOrganizations();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().size());
                    assertEquals("my-org", response.getBody().get(0).getName());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(organizationService).findAll();
    }

    @Test
    void listAllOrganizations_whenEmpty_returnsOkWithEmptyList() {
        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(organizationService.findAll()).thenReturn(Flux.empty());

        Mono<ResponseEntity<List<Organization>>> result = controller.listAllOrganizations();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertTrue(response.getBody().isEmpty());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(organizationService).findAll();
    }

    @Test
    void listAllOrganizations_whenTimeKeeperEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<List<Organization>>> result = controller.listAllOrganizations();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(organizationService);
    }

    @Test
    void organizationsCount_whenDataAvailable_returnsOkWithCount() {
        Organization org1 = new Organization("org1", "org1");
        Organization org2 = new Organization("org2", "org2");

        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(organizationService.findAll()).thenReturn(Flux.just(org1, org2));

        Mono<ResponseEntity<Long>> result = controller.organizationsCount();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(2L, response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(organizationService).findAll();
    }

    @Test
    void organizationsCount_whenEmpty_returnsOkWithZero() {
        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(organizationService.findAll()).thenReturn(Flux.empty());

        Mono<ResponseEntity<Long>> result = controller.organizationsCount();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(0L, response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(organizationService).findAll();
    }

    @Test
    void organizationsCount_whenTimeKeeperEmpty_returnsOkWithZero() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<Long>> result = controller.organizationsCount();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(0L, response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(organizationService);
    }
}
