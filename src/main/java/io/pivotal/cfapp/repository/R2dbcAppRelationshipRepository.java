package io.pivotal.cfapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.query.Criteria;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.AppRelationship;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcAppRelationshipRepository {

	private final DatabaseClient client;

	@Autowired
	public R2dbcAppRelationshipRepository(DatabaseClient client) {
		this.client = client;
	}

	public Mono<AppRelationship> save(AppRelationship entity) {
		return
			client
				.insert()
				.into(AppRelationship.class)
				.table(AppRelationship.tableName())
				.using(entity)
				.fetch()
				.rowsUpdated()
				.then(Mono.just(entity));
	}

	public Flux<AppRelationship> findAll() {
		return client
				.select()
				.from(AppRelationship.tableName())
				.project(AppRelationship.columnNames())
				.orderBy(Order.asc("organization"), Order.asc("space"), Order.asc("app_name"))
				.as(AppRelationship.class)
				.fetch()
				.all();
	}

	public Flux<AppRelationship> findByApplicationId(String applicationId) {
		return client
				.select()
					.from(AppRelationship.tableName())
					.project(AppRelationship.columnNames())
					.matching(Criteria.where("app_id").is(applicationId))
				.orderBy((Order.asc("organization")), Order.asc("space"), Order.asc("service_name"))
				.as(AppRelationship.class)
				.fetch()
				.all();
	}

	public Flux<AppRelationship> findByServiceInstanceId(String serviceInstanceId) {
		return client
				.select()
					.from(AppRelationship.tableName())
					.project(AppRelationship.columnNames())
					.matching(Criteria.where("service_instance_id").is(serviceInstanceId))
				.orderBy((Order.asc("organization")), Order.asc("space"), Order.asc("service_name"))
				.as(AppRelationship.class)
				.fetch()
				.all();
	}

	public Mono<Void> deleteAll() {
		return client
				.delete()
				.from(AppRelationship.tableName())
				.fetch()
				.rowsUpdated()
				.then();
	}

}
