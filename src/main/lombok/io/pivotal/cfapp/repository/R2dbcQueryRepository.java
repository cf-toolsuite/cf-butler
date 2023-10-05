package io.pivotal.cfapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.Query;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Repository
public class R2dbcQueryRepository {

    private final R2dbcEntityOperations client;

    @Autowired
    public R2dbcQueryRepository(R2dbcEntityOperations client) {
        this.client = client;
    }

    public Flux<Tuple2<Row, RowMetadata>> executeQuery(Query query) {
        return
                client
                .getDatabaseClient()
                .sql(query.getSql())
                .map(Tuples::of)
                .all();
    }
}
