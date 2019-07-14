package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.Query;
import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;

public interface QueryService {

    Flux<Row> executeQuery(Query query);
}