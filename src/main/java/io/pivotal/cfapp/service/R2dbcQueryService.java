package io.pivotal.cfapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pivotal.cfapp.domain.Query;
import io.pivotal.cfapp.repository.R2dbcQueryRepository;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

@Slf4j
@Service
public class R2dbcQueryService implements QueryService {

    private final R2dbcQueryRepository repo;

    @Autowired
    public R2dbcQueryService(R2dbcQueryRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public Flux<Tuple2<Row, RowMetadata>> executeQuery(Query query) {
        log.trace(String.format("Attempting to execute a query named [ %s ] and the statement is [ %s ]", query.getName(), query.getSql()));
        return repo
                .executeQuery(query)
                .onErrorContinue(
                        (ex, data) -> log.error(String.format("Problem executing query %s.", query.getSql()), ex));
    }
}
