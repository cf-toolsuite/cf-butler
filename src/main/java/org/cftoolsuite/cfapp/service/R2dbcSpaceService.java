package org.cftoolsuite.cfapp.service;

import org.cftoolsuite.cfapp.domain.Space;
import org.cftoolsuite.cfapp.repository.R2dbcSpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public Mono<Void> deleteAll() {
        return repo.deleteAll();
    }

    @Override
    public Flux<Space> findAll() {
        return repo.findAll();
    }

    @Override
    @Transactional
    public Mono<Space> save(Space entity) {
        return repo
                .save(entity)
                .onErrorContinue(
                        (ex, data) -> log.error(String.format("Problem saving space %s.", entity), ex));
    }
}
