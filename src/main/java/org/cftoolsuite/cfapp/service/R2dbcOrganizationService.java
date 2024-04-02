package org.cftoolsuite.cfapp.service;

import org.cftoolsuite.cfapp.domain.Organization;
import org.cftoolsuite.cfapp.repository.R2dbcOrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class R2dbcOrganizationService implements OrganizationService {

    private final R2dbcOrganizationRepository repo;

    @Autowired
    public R2dbcOrganizationService(R2dbcOrganizationRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public Mono<Void> deleteAll() {
        return repo.deleteAll();
    }

    @Override
    public Flux<Organization> findAll() {
        return repo.findAll();
    }

    @Override
    @Transactional
    public Mono<Organization> save(Organization entity) {
        return repo
                .save(entity)
                .onErrorContinue(
                        (ex, data) -> log.error(String.format("Problem saving organization %s.", entity), ex));
    }
}
