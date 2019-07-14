package io.pivotal.cfapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.Query;
import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;

@Repository
public class R2dbcQueryRepository {

    private final DatabaseClient client;

    @Autowired
    public R2dbcQueryRepository(DatabaseClient client) {
        this.client = client;
    }

    public Flux<Row> executeQuery(Query query) {
        return client
                .execute(query.getSql())
                .map((row, metadata) -> row)
                .all();
    }
}