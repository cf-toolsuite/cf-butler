package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.Organization;
import io.pivotal.cfapp.service.OrganizationService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
public class OrganizationController {

    private final OrganizationService service;

    @Autowired
    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    @GetMapping("/organizations")
    public Flux<Organization> listAllOrganizations() {
        return service.getOrganizations();
    }

    @GetMapping("/organizations/count")
    public Mono<Long> organizationsCount() {
        return service.getOrganizations().count();
    }

}