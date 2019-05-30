package io.pivotal.cfapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.client.OpsmanClient;
import io.pivotal.cfapp.domain.product.DeployedProduct;
import io.pivotal.cfapp.domain.product.OmInfo;
import io.pivotal.cfapp.domain.product.StemcellAssignments;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "om.enabled", havingValue = "true")
public class OpsmanController {

    private final OpsmanClient client;

    @Autowired
    public OpsmanController(OpsmanClient client) {
        this.client = client;
    }

    @GetMapping("/products/deployed")
    public Mono<ResponseEntity<List<DeployedProduct>>> getDeployedProducts(@RequestHeader(HttpHeaders.AUTHORIZATION) String uaaToken) {
        return client
                .getDeployedProducts(uaaToken)
                .map(products -> ResponseEntity.ok(products));
    }

    @GetMapping("/products/stemcell/assignments")
    public Mono<ResponseEntity<StemcellAssignments>> getStemcellAssignments(@RequestHeader(HttpHeaders.AUTHORIZATION) String uaaToken) {
        return client
                .getStemcellAssignments(uaaToken)
                .map(assignments -> ResponseEntity.ok(assignments));
    }

    @GetMapping("/products/om/info")
    public Mono<ResponseEntity<OmInfo>> getOmInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String uaaToken) {
        return client
                .getOmInfo(uaaToken)
                .map(info -> ResponseEntity.ok(info));
    }

}