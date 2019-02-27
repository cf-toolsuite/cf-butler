package io.pivotal.cfapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.AppRelationship;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcAppRelationshipRepository {

	private DatabaseClient client;

	@Autowired
	public R2dbcAppRelationshipRepository(DatabaseClient client) {
		this.client = client;
	}

	public Mono<AppRelationship> save(AppRelationship entity) {
		return client.insert().into("application_relationship")
								.value("organization", entity.getOrganization())
								.value("space", entity.getSpace())
								.value("app_id", entity.getAppId())
								.value("app_name", entity.getAppName())
								.value("service_id", entity.getServiceId())
								.value("service_name", entity.getServiceName())
								.value("service_plan", entity.getServicePlan())
								.value("service_type", entity.getServiceType())
								.fetch()
								.rowsUpdated()
								.then(client.execute().sql("select id, organization, space, app_id, app_name, service_id, service_name, service_plan, service_type from application_relationship where app_id = ? and service_id = ?")
												.bind("app_id", entity.getAppId())
												.bind("service_id", entity.getServiceId())
												.as(AppRelationship.class)
												.fetch()
												.one());
	}

	public Flux<AppRelationship> findAll() {
		return client.select().from("application_relationship")
						.orderBy(Sort.by("organization", "space", "app_name"))
						.as(AppRelationship.class)
						.fetch()
						.all();
	}

	public Flux<AppRelationship> findByApplicationId(String applicationId) {
		String selectOne = "select id, organization, space, app_id, app_name, service_id, service_name, service_plan, service_type from application_relationship where app_id = ? order by organization, space, service_name";
		return client.execute().sql(selectOne)
						.bind("app_id", applicationId)
						.as(AppRelationship.class)
						.fetch()
						.all();
	}

	public Mono<Void> deleteAll() {
		return client.execute().sql("delete from application_relationship")
						.fetch()
						.rowsUpdated()
						.then();
	}

}
