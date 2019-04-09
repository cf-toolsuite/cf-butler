package io.pivotal.cfapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.repository.R2dbcSpaceRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        return repo.save(entity);
    }

    @Override
    public Flux<Space> findAll() {
        return repo.findAll();
    }
}