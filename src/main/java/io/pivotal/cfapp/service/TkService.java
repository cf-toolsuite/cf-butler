package io.pivotal.cfapp.service;

import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.repository.R2dbcTkRepository;
import io.r2dbc.spi.R2dbcException;
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

    public Mono<Integer> save() {
        LocalDateTime collectionTime = LocalDateTime.now();
        return repo
                .save(collectionTime)
                .onErrorContinue(R2dbcException.class,
					(ex, data) -> log.error("Problem saving collectime time {}.", collectionTime, ex))
				.onErrorContinue(SQLException.class,
					(ex, data) -> log.error("Problem saving space user {}.", collectionTime, ex));

    }

    public Mono<Void> deleteOne() {
		    return repo.deleteOne();
    }

    public Mono<LocalDateTime> findOne() {
		    return repo.findOne();
    }
}
