package io.pivotal.cfapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.Demographics;
import reactor.core.publisher.Mono;

@Service
public class DemographicsService {

    private final SpaceService spaceService;
    private final SpaceUsersService spaceUsersService;
    private final OrganizationService orgService;

    @Autowired
    public DemographicsService(
            SpaceService spaceService,
            SpaceUsersService spaceUsersService,
            OrganizationService orgService
            ) {
        this.spaceService = spaceService;
        this.spaceUsersService = spaceUsersService;
        this.orgService = orgService;
    }

    public Mono<Demographics> getDemographics() {
        return totalUserAccounts()
                .map(ua -> Demographics.builder().userAccounts(ua))
                .flatMap(b -> totalServiceAccounts()
                        .map(sa -> b.serviceAccounts(sa)))
                .flatMap(b -> totalSpaces()
                        .map(s -> b.spaces(s)))
                .flatMap(b -> totalOrganizations()
                        .map(o -> b.organizations(o).build()));
    }

    private Mono<Long> totalOrganizations() {
        return orgService.findAll().count();
    }

    private Mono<Long> totalServiceAccounts() {
        return spaceUsersService.totalServiceAccounts();
    }

    private Mono<Long> totalSpaces() {
        return spaceService.findAll().count();
    }

    private Mono<Long> totalUserAccounts() {
        return spaceUsersService.totalUserAccounts();
    }
}
