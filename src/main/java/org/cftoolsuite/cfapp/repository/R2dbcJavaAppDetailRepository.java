package org.cftoolsuite.cfapp.repository;

import org.cftoolsuite.cfapp.domain.JavaAppDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcJavaAppDetailRepository {

    private final R2dbcEntityOperations client;

    @Autowired
    public R2dbcJavaAppDetailRepository(R2dbcEntityOperations client) {
        this.client = client;
    }

    public Mono<Void> deleteAll() {
        return
            client
                .delete(JavaAppDetail.class)
                .all()
                .then();
    }

    public Flux<JavaAppDetail> findAll() {
        Sort order = Sort.by(Order.asc("organization"), Order.asc("space"), Order.asc("app_name"));
        return
            client
                .select(JavaAppDetail.class)
                .matching(Query.empty().sort(order))
                .all();
    }

    public Mono<JavaAppDetail> findByAppId(String appId) {
        Criteria criteria =
                Criteria
                .where("app_id").is(appId);
        return
            client
                .select(JavaAppDetail.class)
                .matching(Query.query(criteria))
                .one();
    }

    public Mono<JavaAppDetail> save(JavaAppDetail entity) {
        return
            client
                .insert(entity);
    }

}
