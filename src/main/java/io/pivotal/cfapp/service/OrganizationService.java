package io.pivotal.cfapp.service;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.Organization;
import reactor.core.publisher.Flux;

@Service
public class OrganizationService {

    private final DefaultCloudFoundryOperations opsClient;

    @Autowired
    public OrganizationService(DefaultCloudFoundryOperations opsClient) {
        this.opsClient = opsClient;
    }

    public Flux<Organization> getOrganizations() {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .build()
                .organizations()
                    .list()
                    .map(os -> new Organization(os.getId(), os.getName()));
    }
}