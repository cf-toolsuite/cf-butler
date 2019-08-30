package io.pivotal.cfapp.repository;

import java.time.LocalDateTime;

import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public class R2dbcTkRepository {

    private final DatabaseClient client;

    public R2dbcTkRepository(DatabaseClient client) {
        this.client = client;
    }

    public Mono<Integer> save(LocalDateTime collectionTime) {
        return client
                .insert().into("time_keeper")
                .value("collection_time", collectionTime)
                .fetch().rowsUpdated();
    }

    public Mono<Void> deleteOne() {
        return client
                .delete()
                .from("time_keeper")
                .fetch()
                .rowsUpdated()
                .then();
    }

    public Mono<LocalDateTime> findOne() {
        return client
                .select()
                .from("time_keeper")
                .project("collection_time")
                .map((row, metadata) -> row.get("collection_time", LocalDateTime.class))
                .one();
    }

}