package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.cftoolsuite.cfapp.domain.Metadata;
import org.cftoolsuite.cfapp.domain.Resource;
import org.cftoolsuite.cfapp.domain.Resources;
import org.cftoolsuite.cfapp.service.ResourceMetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

class OnDemandResourceMetadataControllerTest extends ControllerTestBase {

    private ResourceMetadataService service;
    private OnDemandResourceMetadataController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        service = mock(ResourceMetadataService.class);
        controller = new OnDemandResourceMetadataController(service);
    }

    @Test
    void getResourcesMetadata_whenDataAvailable_returnsOk() {
        Resources resources = Resources.builder().build();

        when(service.getResources("applications")).thenReturn(Mono.just(resources));

        assertOkBody(controller.getResourcesMetadata("applications", null, null, null), resources);

        verify(service).getResources("applications");
    }

    @Test
    void getResourcesMetadata_whenEmpty_returnsNotFound() {
        when(service.getResources("applications")).thenReturn(Mono.empty());

        assertNotFound(controller.getResourcesMetadata("applications", null, null, null));

        verify(service).getResources("applications");
    }

    @Test
    void getResourcesMetadata_withLabelSelector_returnsOk() {
        Resources resources = Resources.builder().build();

        when(service.getResources("applications", "env=prod", 1, 50)).thenReturn(Mono.just(resources));

        assertOkBody(controller.getResourcesMetadata("applications", "env=prod", 1, 50), resources);

        verify(service).getResources("applications", "env=prod", 1, 50);
    }

    @Test
    void getResourcesMetadata_withLabelSelector_empty_returnsNotFound() {
        when(service.getResources("applications", "env=prod", 1, 50)).thenReturn(Mono.empty());

        assertNotFound(controller.getResourcesMetadata("applications", "env=prod", 1, 50));

        verify(service).getResources("applications", "env=prod", 1, 50);
    }

    @Test
    void getResourceMetadata_whenDataAvailable_returnsOk() {
        Resource resource = Resource.builder().build();

        when(service.getResource("applications", "app-1")).thenReturn(Mono.just(resource));

        assertOkBody(controller.getResourceMetadata("applications", "app-1"), resource);

        verify(service).getResource("applications", "app-1");
    }

    @Test
    void getResourceMetadata_whenEmpty_returnsNotFound() {
        when(service.getResource("applications", "app-1")).thenReturn(Mono.empty());

        assertNotFound(controller.getResourceMetadata("applications", "app-1"));

        verify(service).getResource("applications", "app-1");
    }

    @Test
    void updateResourceMetadata_whenDataAvailable_returnsOk() {
        Metadata metadata = Metadata.builder().build();

        when(service.updateResource("applications", "app-1", metadata)).thenReturn(Mono.just(metadata));

        assertOkBody(controller.updateResourceMetadata("applications", "app-1", metadata), metadata);

        verify(service).updateResource("applications", "app-1", metadata);
    }

    @Test
    void updateResourceMetadata_whenEmpty_returnsNotFound() {
        Metadata metadata = Metadata.builder().build();

        when(service.updateResource("applications", "app-1", metadata)).thenReturn(Mono.empty());

        assertNotFound(controller.updateResourceMetadata("applications", "app-1", metadata));

        verify(service).updateResource("applications", "app-1", metadata);
    }
}
