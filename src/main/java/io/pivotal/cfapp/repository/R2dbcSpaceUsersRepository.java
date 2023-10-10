package io.pivotal.cfapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.SpaceUsers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcSpaceUsersRepository {

    private final R2dbcEntityOperations client;

    @Autowired
    public R2dbcSpaceUsersRepository(R2dbcEntityOperations client) {
        this.client = client;
    }

    public Mono<Void> deleteAll() {
        return
                client
                .delete(SpaceUsers.class)
                .all()
                .then();
    }

    public Flux<SpaceUsers> findAll() {
        return
                client
                .select(SpaceUsers.class)
                .matching(Query.empty().sort(Sort.by(Order.asc("organization"), Order.asc("space"))))
                .all();
    }

    public Mono<SpaceUsers> findByOrganizationAndSpace(String organization, String space) {
        return
                client
                .select(SpaceUsers.class)
                .matching(Query.query(Criteria.where("organization").is(organization).and("space").is(space)))
                .one();
    }

    public Mono<SpaceUsers> save(SpaceUsers entity) {
        return
                client
                .insert(entity);
    }

}
