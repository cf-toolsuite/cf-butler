package io.pivotal.cfapp.repository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.function.DatabaseClient.GenericInsertSpec;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.config.DbmsSettings;
import io.pivotal.cfapp.domain.AppRelationship;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcAppRelationshipRepository {

	private DatabaseClient client;
	private DbmsSettings settings;

	@Autowired
	public R2dbcAppRelationshipRepository(
		DatabaseClient client,
		DbmsSettings settings) {
		this.client = client;
		this.settings = settings;
	}

	public Mono<AppRelationship> save(AppRelationship entity) {
		GenericInsertSpec<Map<String, Object>> spec =
			client.insert().into("application_relationship")
				.value("organization", entity.getOrganization());
		spec = spec.value("space", entity.getSpace());
		spec = spec.value("app_id", entity.getAppId());
		if (entity.getAppName() != null) {
			spec = spec.value("app_name", entity.getAppName());
		} else {
			spec = spec.nullValue("app_name", String.class);
		}
		spec = spec.value("service_id", entity.getServiceId());
		if (entity.getServiceName() != null) {
			spec = spec.value("service_name", entity.getServiceName());
		} else {
			spec = spec.nullValue("service_name", String.class);
		}
		if (entity.getServicePlan() != null) {
			spec = spec.value("service_plan", entity.getServicePlan());
		} else {
			spec = spec.nullValue("service_plan", String.class);
		}
		if (entity.getServiceType() != null) {
			spec = spec.value("service_type", entity.getServiceType());
		} else {
			spec = spec.nullValue("service_type", String.class);
		}
		return spec.fetch().rowsUpdated().then(Mono.just(entity));
	}

	public Flux<AppRelationship> findAll() {
		return client.select().from("application_relationship")
						.orderBy(Sort.by("organization", "space", "app_name"))
						.as(AppRelationship.class)
						.fetch()
						.all();
	}

	public Flux<AppRelationship> findByApplicationId(String applicationId) {
		String index = settings.getBindPrefix() + 1;
		String selectOne = "select pk, organization, space, app_id, app_name, service_id, service_name, service_plan, service_type from application_relationship where app_id = " + index + " order by organization, space, service_name";
		return client.execute().sql(selectOne)
						.bind(index, applicationId)
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
