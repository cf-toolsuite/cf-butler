package io.pivotal.cfapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.repository.R2dbcSpaceRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class R2dbcSpaceService implements SpaceService {

    private final R2dbcSpaceRepository repo;

    @Autowired
    public R2dbcSpaceService(R2dbcSpaceRepository repo) {
        this.repo = repo;
    }

    @Override
    public Mono<Void> deleteAll() {
        return repo.deleteAll();
    }

    @Override
    public Mono<Space> save(Space entity) {
        return repo
                .save(entity)
                .onErrorContinue(
                    (ex, data) -> log.error("Problem saving space {}.", entity, ex));
    }

    @Override
    public Flux<Space> findAll() {
        return repo.findAll();
    }
}