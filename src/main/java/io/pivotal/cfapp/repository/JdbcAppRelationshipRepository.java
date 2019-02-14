package io.pivotal.cfapp.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.AppRelationship;
import io.reactivex.Flowable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Repository
public class JdbcAppRelationshipRepository {

	private Database database;

	@Autowired
	public JdbcAppRelationshipRepository(Database database) {
		this.database = database;
	}

	public Mono<AppRelationship> save(AppRelationship entity) {
		Flowable<Long> insert = database
									.update("insert into application_relationship (organization, space, app_id, app_name, service_id, service_name, service_plan, service_type) values (?, ?, ?, ?, ?, ?, ?, ?)")
									.parameters(
										entity.getOrganization(),
										entity.getSpace(),
										entity.getAppId(),
										entity.getAppName(),
										entity.getServiceId(),
										entity.getServiceName(),
										entity.getServicePlan(),
										entity.getServiceType()
									)
									.returnGeneratedKeys()
									.getAs(Long.class);
		return Mono.from(database
					.select("select id, organization, space, app_id, app_name, service_id, service_name, service_plan, service_type from application_relationship where id = ?")
					.parameterStream(insert)
					.get(rs -> fromResultSet(rs)));
	}

	public Flux<AppRelationship> findAll() {
		String selectAll = "select id, organization, space, app_id, app_name, service_id, service_name, service_plan, service_type from application_relationship order by organization, space, app_name";
		return Flux.from(database
							.select(selectAll)
							.get(rs -> fromResultSet(rs)));
	}
	
	public Flux<AppRelationship> findByApplicationId(String applicationId) {
		String select = "select id, organization, space, app_id, app_name, service_id, service_name, service_plan, service_type from application_relationship where app_id = ? order by organization, space, service_name";
		return Flux.from(database
							.select(select)
							.parameter(applicationId)
							.get(rs -> fromResultSet(rs)));
	}

	public Mono<Void> deleteAll() {
		String deleteAll = "delete from application_relationship";
		return Flux.from(database
							.update(deleteAll)
							.counts())
							.then();
	}
	
	private AppRelationship fromResultSet(ResultSet rs) throws SQLException {
		return AppRelationship
				.builder()
					.id(rs.getLong(1))
					.organization(rs.getString(2))
					.space(rs.getString(3))
					.appId(rs.getString(4))
					.appName(rs.getString(5))
					.serviceId(rs.getString(6))
					.serviceName(rs.getString(7))
					.servicePlan(rs.getString(8))
					.serviceType(rs.getString(9))
					.build();
	}
}
