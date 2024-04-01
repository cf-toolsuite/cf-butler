package org.cftoolsuite.cfapp.service;

import java.time.LocalDateTime;

import org.cftoolsuite.cfapp.domain.TimeKeeper;
import org.cftoolsuite.cfapp.repository.R2dbcTimeKeeperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class TimeKeeperService {

    private final R2dbcTimeKeeperRepository repo;

    @Autowired
    public TimeKeeperService(R2dbcTimeKeeperRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Mono<Void> deleteOne() {
        return repo.deleteOne();
    }

    public Mono<LocalDateTime> findOne() {
        return repo.findOne();
    }

    @Transactional
    public Mono<TimeKeeper> save() {
        LocalDateTime collectionTime = LocalDateTime.now();
        return
                repo
                .save(collectionTime)
                .onErrorContinue(
                        (ex, data) -> log.error(String.format("Problem saving collectime time %s.", collectionTime), ex));
    }
}
