package org.cftoolsuite.cfapp.controller;

import org.cftoolsuite.cfapp.domain.Metadata;
import org.cftoolsuite.cfapp.domain.Resource;
import org.cftoolsuite.cfapp.domain.Resources;
import org.cftoolsuite.cfapp.service.ResourceMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/metadata/{type}")
    public Mono<ResponseEntity<Resources>> getResourcesMetadata(
    @PathVariable("type") String type,
    @RequestParam(value = "label_selector", required = false) String labelSelector,
    @RequestParam(value = "page", required = false) Integer page,
    @RequestParam(value = "per_page", required = false) Integer perPage
    ) {
        if (labelSelector != null){
            return service.getResources(type,labelSelector,page,perPage)
            .map(r -> ResponseEntity.ok(r))
            .defaultIfEmpty(ResponseEntity.notFound().build());
        }
        return service.getResources(type)
                        .map(r -> ResponseEntity.ok(r))
                        .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/metadata/{type}/{id}")
    public Mono<ResponseEntity<Resource>> getResourceMetadata(
            @PathVariable("type") String type,
            @PathVariable("id") String id
            ) {
        return service.getResource(type, id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/metadata/{type}/{id}")
    public Mono<ResponseEntity<Metadata>> updateResourceMetadata(
            @PathVariable("type") String type,
            @PathVariable("id") String id,
            @RequestBody Metadata metadata
            ) {
        return service.updateResource(type, id, metadata)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
