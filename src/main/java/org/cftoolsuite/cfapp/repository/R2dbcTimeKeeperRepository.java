package org.cftoolsuite.cfapp.repository;

import java.time.LocalDateTime;

import org.cftoolsuite.cfapp.domain.TimeKeeper;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public class R2dbcTimeKeeperRepository {

    private final R2dbcEntityOperations client;

    public R2dbcTimeKeeperRepository(R2dbcEntityOperations client) {
        this.client = client;
    }

    public Mono<Void> deleteOne() {
        return
                client
                .delete(TimeKeeper.class)
                .all()
                .then();
    }

    public Mono<LocalDateTime> findOne() {
        return
                client
                .select(TimeKeeper.class)
                .first()
                .map(TimeKeeper::getCollectionTime);
    }

    public Mono<TimeKeeper> save(LocalDateTime collectionTime) {
        return
                client
                .insert(new TimeKeeper(collectionTime));
    }

}
