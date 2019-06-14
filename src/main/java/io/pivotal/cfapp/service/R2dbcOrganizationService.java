package io.pivotal.cfapp.service;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.Organization;
import io.pivotal.cfapp.repository.R2dbcOrganizationRepository;
import io.r2dbc.spi.R2dbcException;
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
    public Mono<Void> deleteAll() {
        return repo.deleteAll();
    }

    @Override
    public Mono<Organization> save(Organization entity) {
        return repo
                .save(entity)
                .onErrorContinue(R2dbcException.class,
                    (ex, data) -> log.error("Problem saving organization {}.", entity, ex))
                .onErrorContinue(SQLException.class,
                    (ex, data) -> log.error("Problem saving organization {}.", entity, ex));
    }

    @Override
    public Flux<Organization> findAll() {
        return repo.findAll();
    }
}