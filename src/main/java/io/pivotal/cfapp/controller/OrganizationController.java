package io.pivotal.cfapp.controller;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.Organization;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
public class OrganizationController {

    private DefaultCloudFoundryOperations opsClient;

    public OrganizationController(DefaultCloudFoundryOperations opsClient) {
        this.opsClient = opsClient;
    }

    @GetMapping("/organizations")
    public Flux<Organization> listAllOrganizations() {
        return getOrganizations();
    }

    @GetMapping("/organizations/count")
    public Mono<Long> organizationsCount() {
        return getOrganizations().count();
    }

    private Flux<Organization> getOrganizations() {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .build()
                .organizations()
                    .list()
                    .map(os -> new Organization(os.getId(), os.getName()));
    }

}