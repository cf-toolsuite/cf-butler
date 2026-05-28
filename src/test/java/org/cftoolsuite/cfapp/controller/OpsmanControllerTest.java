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
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OpsmanControllerTest {

    private OpsmanClient client;
    private OpsmanController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        Mono<ResponseEntity<List<DeployedProduct>>> result = controller.getDeployedProducts();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(client).getDeployedProducts();
    }

    @Test
    void getOmInfo_whenDataAvailable_returnsOk() {
        OmInfo omInfo = OmInfo.builder().build();

        when(client.getOmInfo()).thenReturn(Mono.just(omInfo));

        Mono<ResponseEntity<OmInfo>> result = controller.getOmInfo();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(omInfo, response.getBody());
                })
                .verifyComplete();

        verify(client).getOmInfo();
    }

    @Test
    void getOmInfo_whenEmpty_returnsNotFound() {
        when(client.getOmInfo()).thenReturn(Mono.empty());

        Mono<ResponseEntity<OmInfo>> result = controller.getOmInfo();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(client).getOmInfo();
    }

    @Test
    void getStemcellAssignments_whenDataAvailable_returnsOk() {
        StemcellAssignments assignments = StemcellAssignments.builder().build();

        when(client.getStemcellAssignments()).thenReturn(Mono.just(assignments));

        Mono<ResponseEntity<StemcellAssignments>> result = controller.getStemcellAssignments();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(assignments, response.getBody());
                })
                .verifyComplete();

        verify(client).getStemcellAssignments();
    }

    @Test
    void getStemcellAssignments_whenEmpty_returnsNotFound() {
        when(client.getStemcellAssignments()).thenReturn(Mono.empty());

        Mono<ResponseEntity<StemcellAssignments>> result = controller.getStemcellAssignments();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(client).getStemcellAssignments();
    }

    @Test
    void getStemcellAssociations_whenDataAvailable_returnsOk() {
        StemcellAssociations associations = StemcellAssociations.builder().build();

        when(client.getStemcellAssociations()).thenReturn(Mono.just(associations));

        Mono<ResponseEntity<StemcellAssociations>> result = controller.getStemcellAssociations();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(associations, response.getBody());
                })
                .verifyComplete();

        verify(client).getStemcellAssociations();
    }

    @Test
    void getStemcellAssociations_whenEmpty_returnsNotFound() {
        when(client.getStemcellAssociations()).thenReturn(Mono.empty());

        Mono<ResponseEntity<StemcellAssociations>> result = controller.getStemcellAssociations();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(client).getStemcellAssociations();
    }
}
