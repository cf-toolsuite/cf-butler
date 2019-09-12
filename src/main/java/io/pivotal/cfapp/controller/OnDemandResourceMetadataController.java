package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.Resource;
import io.pivotal.cfapp.service.ResourceMetadataService;
import reactor.core.publisher.Mono;

@Profile("on-demand")
@RestController
public class OnDemandResourceMetadataController {

    private final ResourceMetadataService service;


    @Autowired
    public OnDemandResourceMetadataController(
        ResourceMetadataService service
    ) {
        this.service = service;
    }

    @GetMapping("/metadata/{id}")
    public Mono<ResponseEntity<Resource>> getResourceMetadata(
        @PathVariable("id") String id
    ) {
        return service.getResource(id)
                        .map(r -> ResponseEntity.ok(r))
                        .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/metadata")
    public Mono<ResponseEntity<Resource>> patchResourceMetadata(
        @RequestBody Resource resource
    ) {
        return service.patchResource(resource)
                        .map(r -> ResponseEntity.ok(r))
                        .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}