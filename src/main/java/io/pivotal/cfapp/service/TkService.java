package io.pivotal.cfapp.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pivotal.cfapp.repository.R2dbcTkRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class TkService {

    private final R2dbcTkRepository repo;

    @Autowired
    public TkService(R2dbcTkRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Mono<Integer> save() {
        LocalDateTime collectionTime = LocalDateTime.now();
        return repo
                .save(collectionTime)
                .onErrorContinue(
					(ex, data) -> log.error(String.format("Problem saving collectime time %s.", collectionTime), ex));
    }

    @Transactional
    public Mono<Void> deleteOne() {
		    return repo.deleteOne();
    }

    public Mono<LocalDateTime> findOne() {
		    return repo.findOne();
    }
}
