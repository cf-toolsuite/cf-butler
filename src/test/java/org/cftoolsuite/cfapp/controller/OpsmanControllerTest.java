package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import org.cftoolsuite.cfapp.client.OpsmanClient;
import org.cftoolsuite.cfapp.domain.product.DeployedProduct;
import org.cftoolsuite.cfapp.domain.product.OmInfo;
import org.cftoolsuite.cfapp.domain.product.StemcellAssignments;
import org.cftoolsuite.cfapp.domain.product.StemcellAssociations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

class OpsmanControllerTest extends ControllerTestBase {

    private OpsmanClient client;
    private OpsmanController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        client = mock(OpsmanClient.class);
        controller = new OpsmanController(client);
    }

    static Stream<Arguments> clientTypes() {
        return Stream.of(
                arguments("DeployedProducts", Arrays.asList(DeployedProduct.builder().build())),
                arguments("OmInfo", OmInfo.builder().build()),
                arguments("StemcellAssignments", StemcellAssignments.builder().build()),
                arguments("StemcellAssociations", StemcellAssociations.builder().build())
        );
    }

    @ParameterizedTest(name = "{0} - data available")
    @MethodSource("clientTypes")
    void getClientData_whenDataAvailable_returnsOk(String type, Object data) {
        stubClient(type, Mono.just(data));
        Mono<? extends ResponseEntity<?>> result = invokeController(type);
        assertOk(result);
    }

    @ParameterizedTest(name = "{0} - empty")
    @MethodSource("clientTypes")
    void getClientData_whenEmpty_returnsNotFound(String type, Object data) {
        stubClient(type, Mono.empty());
        assertNotFound(invokeController(type));
    }

    private void stubClient(String type, Mono<?> mono) {
        switch (type) {
            case "DeployedProducts" -> when(client.getDeployedProducts()).thenReturn((Mono<List<DeployedProduct>>) mono);
            case "OmInfo" -> when(client.getOmInfo()).thenReturn((Mono<OmInfo>) mono);
            case "StemcellAssignments" -> when(client.getStemcellAssignments()).thenReturn((Mono<StemcellAssignments>) mono);
            case "StemcellAssociations" -> when(client.getStemcellAssociations()).thenReturn((Mono<StemcellAssociations>) mono);
        }
    }

    private Mono<? extends ResponseEntity<?>> invokeController(String type) {
        return switch (type) {
            case "DeployedProducts" -> controller.getDeployedProducts();
            case "OmInfo" -> controller.getOmInfo();
            case "StemcellAssignments" -> controller.getStemcellAssignments();
            case "StemcellAssociations" -> controller.getStemcellAssociations();
            default -> throw new IllegalArgumentException(type);
        };
    }
}
