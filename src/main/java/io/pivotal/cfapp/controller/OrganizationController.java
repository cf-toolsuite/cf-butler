package io.pivotal.cfapp.controller;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;


@RestController
public class OrganizationController {

    private DefaultCloudFoundryOperations opsClient;

    public OrganizationController(DefaultCloudFoundryOperations opsClient) {
        this.opsClient = opsClient;
    }

    @GetMapping("/organizations")
    public Flux<Tuple2<String, String>> listAllOrganizations() {
        return getOrganizations();
    }

    @GetMapping("/organizations/count")
    public Mono<Long> organizationsCount() {
        return getOrganizations().count();
    }

    private Flux<Tuple2<String, String>> getOrganizations() {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .build()
                .organizations()
                    .list()
                    .map(os -> Tuples.of(os.getId(), os.getName()));
    }

}