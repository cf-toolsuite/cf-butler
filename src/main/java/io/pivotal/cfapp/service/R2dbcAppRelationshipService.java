package io.pivotal.cfapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pivotal.cfapp.domain.AppRelationship;
import io.pivotal.cfapp.repository.R2dbcAppRelationshipRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class R2dbcAppRelationshipService implements AppRelationshipService {

    private R2dbcAppRelationshipRepository repo;

    @Autowired
    public R2dbcAppRelationshipService(R2dbcAppRelationshipRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public Mono<Void> deleteAll() {
        return repo.deleteAll();
    }

    @Override
    public Flux<AppRelationship> findAll() {
        return repo.findAll();
    }

    @Override
    public Flux<AppRelationship> findByApplicationId(String applicationId) {
        return repo.findByApplicationId(applicationId);
    }

    @Override
    public Flux<AppRelationship> findByServiceInstanceId(String serviceInstanceId) {
        return repo.findByServiceInstanceId(serviceInstanceId);
    }

    @Override
    @Transactional
    public Mono<AppRelationship> save(AppRelationship entity) {
        return repo
                .save(entity)
                .onErrorContinue(
                        (ex, data) -> log.error(String.format("Problem saving application relationship %s.", entity), ex));
    }

}
