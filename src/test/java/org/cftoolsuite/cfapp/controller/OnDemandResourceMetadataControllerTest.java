package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.cftoolsuite.cfapp.domain.Metadata;
import org.cftoolsuite.cfapp.domain.Resource;
import org.cftoolsuite.cfapp.domain.Resources;
import org.cftoolsuite.cfapp.service.ResourceMetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OnDemandResourceMetadataControllerTest {

    private ResourceMetadataService service;
    private OnDemandResourceMetadataController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = mock(ResourceMetadataService.class);
        controller = new OnDemandResourceMetadataController(service);
    }

    @Test
    void getResourcesMetadata_whenDataAvailable_returnsOk() {
        Resources resources = Resources.builder().build();

        when(service.getResources("applications")).thenReturn(Mono.just(resources));

        Mono<ResponseEntity<Resources>> result = controller.getResourcesMetadata("applications", null, null, null);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(resources, response.getBody());
                })
                .verifyComplete();

        verify(service).getResources("applications");
    }

    @Test
    void getResourcesMetadata_whenEmpty_returnsNotFound() {
        when(service.getResources("applications")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Resources>> result = controller.getResourcesMetadata("applications", null, null, null);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(service).getResources("applications");
    }

    @Test
    void getResourcesMetadata_withLabelSelector_returnsOk() {
        Resources resources = Resources.builder().build();

        when(service.getResources("applications", "env=prod", 1, 50)).thenReturn(Mono.just(resources));

        Mono<ResponseEntity<Resources>> result = controller.getResourcesMetadata("applications", "env=prod", 1, 50);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(resources, response.getBody());
                })
                .verifyComplete();

        verify(service).getResources("applications", "env=prod", 1, 50);
    }

    @Test
    void getResourcesMetadata_withLabelSelector_empty_returnsNotFound() {
        when(service.getResources("applications", "env=prod", 1, 50)).thenReturn(Mono.empty());

        Mono<ResponseEntity<Resources>> result = controller.getResourcesMetadata("applications", "env=prod", 1, 50);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(service).getResources("applications", "env=prod", 1, 50);
    }

    @Test
    void getResourceMetadata_whenDataAvailable_returnsOk() {
        Resource resource = Resource.builder().build();

        when(service.getResource("applications", "app-1")).thenReturn(Mono.just(resource));

        Mono<ResponseEntity<Resource>> result = controller.getResourceMetadata("applications", "app-1");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(resource, response.getBody());
                })
                .verifyComplete();

        verify(service).getResource("applications", "app-1");
    }

    @Test
    void getResourceMetadata_whenEmpty_returnsNotFound() {
        when(service.getResource("applications", "app-1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Resource>> result = controller.getResourceMetadata("applications", "app-1");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(service).getResource("applications", "app-1");
    }

    @Test
    void updateResourceMetadata_whenDataAvailable_returnsOk() {
        Metadata metadata = Metadata.builder().build();

        when(service.updateResource("applications", "app-1", metadata)).thenReturn(Mono.just(metadata));

        Mono<ResponseEntity<Metadata>> result = controller.updateResourceMetadata("applications", "app-1", metadata);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(metadata, response.getBody());
                })
                .verifyComplete();

        verify(service).updateResource("applications", "app-1", metadata);
    }

    @Test
    void updateResourceMetadata_whenEmpty_returnsNotFound() {
        Metadata metadata = Metadata.builder().build();

        when(service.updateResource("applications", "app-1", metadata)).thenReturn(Mono.empty());

        Mono<ResponseEntity<Metadata>> result = controller.updateResourceMetadata("applications", "app-1", metadata);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(service).updateResource("applications", "app-1", metadata);
    }
}
