package io.pivotal.cfapp.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.repository.R2dbcTkRepository;
import reactor.core.publisher.Mono;

@Service
public class TkService {

    private final R2dbcTkRepository repo;

    @Autowired
    public TkService(R2dbcTkRepository repo) {
        this.repo = repo;
    }

    public Mono<Integer> save() {
        return repo.save();
    }

    public Mono<Void> deleteOne() {
		    return repo.deleteOne();
    }

    public Mono<LocalDateTime> findOne() {
		    return repo.findOne();
    }
}
