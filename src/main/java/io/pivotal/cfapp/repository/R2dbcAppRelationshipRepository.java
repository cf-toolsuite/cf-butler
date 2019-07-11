package io.pivotal.cfapp.repository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.DatabaseClient.GenericInsertSpec;
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
		GenericInsertSpec<Map<String, Object>> spec =
			client.insert().into(AppRelationship.tableName())
				.value("organization", entity.getOrganization());
		spec = spec.value("space", entity.getSpace());
		spec = spec.value("app_id", entity.getAppId());
		if (entity.getAppName() != null) {
			spec = spec.value("app_name", entity.getAppName());
		} else {
			spec = spec.nullValue("app_name");
		}
		spec = spec.value("service_instance_id", entity.getServiceInstanceId());
		if (entity.getServiceName() != null) {
			spec = spec.value("service_name", entity.getServiceName());
		} else {
			spec = spec.nullValue("service_name");
		}
		if (entity.getServicePlan() != null) {
			spec = spec.value("service_plan", entity.getServicePlan());
		} else {
			spec = spec.nullValue("service_plan");
		}
		if (entity.getServiceType() != null) {
			spec = spec.value("service_type", entity.getServiceType());
		} else {
			spec = spec.nullValue("service_type");
		}
		return spec.fetch().rowsUpdated().then(Mono.just(entity));
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

	public Mono<Void> deleteAll() {
		return client
				.delete()
				.from(AppRelationship.tableName())
				.fetch()
				.rowsUpdated()
				.then();
	}

}
