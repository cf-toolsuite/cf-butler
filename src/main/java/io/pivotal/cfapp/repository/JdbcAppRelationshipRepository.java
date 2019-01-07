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
		String createOne = "insert into app_relationship (organization, space, app_id, app_name, service_id, service_name, service_plan, service_type) values (?, ?, ?, ?, ?, ?, ?, ?)";
		Flux.from(database
					.update(createOne)
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
					.counts())
			.subscribe();

		return Mono.just(entity);
	}

	public Flux<AppRelationship> findAll() {
		String selectAll = "select organization, space, app_id, app_name, service_id, service_name, service_plan, service_type from app_relationship order by organization, space, app_name";
		Flowable<AppRelationship> result = database
			.select(selectAll)
			.get(rs -> fromResultSet(rs));
		return Flux.from(result);
	}

	public Mono<Void> deleteAll() {
		String deleteAll = "delete from app_relationship";
		Flowable<Integer> result = database
			.update(deleteAll)
			.counts();
		return Flux.from(result).then();
	}
	
	private AppRelationship fromResultSet(ResultSet rs) throws SQLException {
		return AppRelationship
				.builder()
				.organization(rs.getString(1))
				.space(rs.getString(2))
				.appId(rs.getString(3))
				.appName(rs.getString(4))
				.serviceId(rs.getString(5))
				.serviceName(rs.getString(6))
				.servicePlan(rs.getString(7))
				.serviceType(rs.getString(8))
				.build();
	}
}
