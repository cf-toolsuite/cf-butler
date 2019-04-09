package io.pivotal.cfapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.Organization;
import io.pivotal.cfapp.repository.R2dbcOrganizationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class R2dbcOrganizationService implements OrganizationService {

    private final R2dbcOrganizationRepository repo;

    @Autowired
    public R2dbcOrganizationService(R2dbcOrganizationRepository repo) {
        this.repo = repo;
    }

    @Override
    public Mono<Void> deleteAll() {
        return repo.deleteAll();
    }

    @Override
    public Mono<Organization> save(Organization entity) {
        return repo.save(entity);
    }

    @Override
    public Flux<Organization> findAll() {
        return repo.findAll();
    }
}