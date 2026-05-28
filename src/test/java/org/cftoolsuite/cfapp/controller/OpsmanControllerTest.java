package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.cftoolsuite.cfapp.client.OpsmanClient;
import org.cftoolsuite.cfapp.domain.product.DeployedProduct;
import org.cftoolsuite.cfapp.domain.product.OmInfo;
import org.cftoolsuite.cfapp.domain.product.StemcellAssignments;
import org.cftoolsuite.cfapp.domain.product.StemcellAssociations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import org.springframework.http.HttpStatus;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OpsmanControllerTest extends ControllerTestBase {

    private OpsmanClient client;
    private OpsmanController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        client = mock(OpsmanClient.class);
        controller = new OpsmanController(client);
    }

    @Test
    void getDeployedProducts_whenDataAvailable_returnsOk() {
        DeployedProduct product = DeployedProduct.builder().build();

        when(client.getDeployedProducts()).thenReturn(Mono.just(Arrays.asList(product)));

        Mono<ResponseEntity<List<DeployedProduct>>> result = controller.getDeployedProducts();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().size());
                })
                .verifyComplete();

        verify(client).getDeployedProducts();
    }

    @Test
    void getDeployedProducts_whenEmpty_returnsNotFound() {
        when(client.getDeployedProducts()).thenReturn(Mono.empty());

        assertNotFound(controller.getDeployedProducts());

        verify(client).getDeployedProducts();
    }

    @Test
    void getOmInfo_whenDataAvailable_returnsOk() {
        OmInfo omInfo = OmInfo.builder().build();

        when(client.getOmInfo()).thenReturn(Mono.just(omInfo));

        assertOkBody(controller.getOmInfo(), omInfo);

        verify(client).getOmInfo();
    }

    @Test
    void getOmInfo_whenEmpty_returnsNotFound() {
        when(client.getOmInfo()).thenReturn(Mono.empty());

        assertNotFound(controller.getOmInfo());

        verify(client).getOmInfo();
    }

    @Test
    void getStemcellAssignments_whenDataAvailable_returnsOk() {
        StemcellAssignments assignments = StemcellAssignments.builder().build();

        when(client.getStemcellAssignments()).thenReturn(Mono.just(assignments));

        assertOkBody(controller.getStemcellAssignments(), assignments);

        verify(client).getStemcellAssignments();
    }

    @Test
    void getStemcellAssignments_whenEmpty_returnsNotFound() {
        when(client.getStemcellAssignments()).thenReturn(Mono.empty());

        assertNotFound(controller.getStemcellAssignments());

        verify(client).getStemcellAssignments();
    }

    @Test
    void getStemcellAssociations_whenDataAvailable_returnsOk() {
        StemcellAssociations associations = StemcellAssociations.builder().build();

        when(client.getStemcellAssociations()).thenReturn(Mono.just(associations));

        assertOkBody(controller.getStemcellAssociations(), associations);

        verify(client).getStemcellAssociations();
    }

    @Test
    void getStemcellAssociations_whenEmpty_returnsNotFound() {
        when(client.getStemcellAssociations()).thenReturn(Mono.empty());

        assertNotFound(controller.getStemcellAssociations());

        verify(client).getStemcellAssociations();
    }
}
