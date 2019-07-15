package io.pivotal.cfapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.Query;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Repository
public class R2dbcQueryRepository {

    private final DatabaseClient client;

    @Autowired
    public R2dbcQueryRepository(DatabaseClient client) {
        this.client = client;
    }

    public Flux<Tuple2<Row, RowMetadata>> executeQuery(Query query) {
        return client
                .execute(query.getSql())
                .map((row, metadata) -> Tuples.of(row, metadata))
                .all();
    }
}