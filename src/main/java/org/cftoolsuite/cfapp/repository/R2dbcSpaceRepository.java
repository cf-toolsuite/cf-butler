package org.cftoolsuite.cfapp.repository;

import org.cftoolsuite.cfapp.domain.Space;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcSpaceRepository {

    private final R2dbcEntityOperations client;

    @Autowired
    public R2dbcSpaceRepository(R2dbcEntityOperations client) {
        this.client = client;
    }

    public Mono<Void> deleteAll() {
        return
                client
                .delete(Space.class)
                .all()
                .then();
    }

    public Flux<Space> findAll() {
        return
                client
                .select(Space.class)
                .matching(Query.empty().sort(Sort.by(Order.asc("org_name"), Order.asc("space_name"))))
                .all();
    }

    public Mono<Space> save(Space entity) {
        return
                client
                .insert(entity);
    }
}
