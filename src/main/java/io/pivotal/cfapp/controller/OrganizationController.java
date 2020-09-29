package io.pivotal.cfapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.Organization;
import io.pivotal.cfapp.service.OrganizationService;
import io.pivotal.cfapp.service.TimeKeeperService;
import io.pivotal.cfapp.service.TkServiceUtil;
import reactor.core.publisher.Mono;


@RestController
public class OrganizationController {

    private final OrganizationService organizationService;
    private final TkServiceUtil util;

    @Autowired
    public OrganizationController(
            OrganizationService organizationService,
            TimeKeeperService tkService) {
        this.organizationService = organizationService;
        this.util = new TkServiceUtil(tkService);
    }

    @GetMapping("/snapshot/organizations")
    public Mono<ResponseEntity<List<Organization>>> listAllOrganizations() {
        return util.getHeaders()
                .flatMap(h -> organizationService
                        .findAll()
                        .collectList()
                        .map(orgs -> new ResponseEntity<>(orgs, h, HttpStatus.OK)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/snapshot/organizations/count")
    public Mono<ResponseEntity<Long>> organizationsCount() {
        return util.getHeaders()
                .flatMap(h -> organizationService
                        .findAll()
                        .count()
                        .map(count -> new ResponseEntity<>(count, h, HttpStatus.OK)))
                .defaultIfEmpty(ResponseEntity.ok(0L));
    }


}
