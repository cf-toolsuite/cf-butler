package io.pivotal.cfapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.client.OpsmanClient;
import io.pivotal.cfapp.domain.product.DeployedProduct;
import io.pivotal.cfapp.domain.product.OmInfo;
import io.pivotal.cfapp.domain.product.StemcellAssignments;
import io.pivotal.cfapp.domain.product.StemcellAssociations;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "om.enabled", havingValue = "true")
public class OpsmanController {

    private final OpsmanClient client;

    @Autowired
    public OpsmanController(
        OpsmanClient client
    ) {
        this.client = client;
    }

    @GetMapping("/products/deployed")
    public Mono<ResponseEntity<List<DeployedProduct>>> getDeployedProducts() {
        return client
                .getDeployedProducts()
                .map(products -> ResponseEntity.ok(products))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/products/stemcell/assignments")
    public Mono<ResponseEntity<StemcellAssignments>> getStemcellAssignments() {
        return client
                .getStemcellAssignments()
                .map(assignments -> ResponseEntity.ok(assignments))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/products/stemcell/associations")
    public Mono<ResponseEntity<StemcellAssociations>> getStemcellAssociations() {
        return client
                .getStemcellAssociations()
                .map(associations -> ResponseEntity.ok(associations))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/products/om/info")
    public Mono<ResponseEntity<OmInfo>> getOmInfo() {
        return client
                .getOmInfo()
                .map(info -> ResponseEntity.ok(info))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}