package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.Query;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

public interface QueryService {

    Flux<Tuple2<Row, RowMetadata>> executeQuery(Query query);
}