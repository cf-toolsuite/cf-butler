package org.cftoolsuite.cfapp.controller;

import java.util.List;

import org.cftoolsuite.cfapp.client.OpsmanClient;
import org.cftoolsuite.cfapp.domain.product.DeployedProduct;
import org.cftoolsuite.cfapp.domain.product.OmInfo;
import org.cftoolsuite.cfapp.domain.product.StemcellAssignments;
import org.cftoolsuite.cfapp.domain.product.StemcellAssociations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/products/om/info")
    public Mono<ResponseEntity<OmInfo>> getOmInfo() {
        return client
                .getOmInfo()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/products/stemcell/assignments")
    public Mono<ResponseEntity<StemcellAssignments>> getStemcellAssignments() {
        return client
                .getStemcellAssignments()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/products/stemcell/associations")
    public Mono<ResponseEntity<StemcellAssociations>> getStemcellAssociations() {
        return client
                .getStemcellAssociations()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
