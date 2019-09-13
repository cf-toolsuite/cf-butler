package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.Metadata;
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

    @GetMapping("/metadata/{type}/{id}")
    public Mono<ResponseEntity<Resource>> getResourceMetadata(
    @PathVariable("type") String type,
    @PathVariable("id") String id
    ) {
        return service.getResource(type, id)
                        .map(r -> ResponseEntity.ok(r))
                        .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/metadata/{type}/{id}")
    public Mono<ResponseEntity<Metadata>> updateResourceMetadata(
        @PathVariable("type") String type,
        @PathVariable("id") String id,
        @RequestBody Metadata metadata
    ) {
        return service.updateResource(type, id, metadata)
                        .map(r -> ResponseEntity.ok(r))
                        .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}