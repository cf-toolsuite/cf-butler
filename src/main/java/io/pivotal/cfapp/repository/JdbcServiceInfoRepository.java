package io.pivotal.cfapp.repository;

import java.sql.Timestamp;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.ServiceDetail;
import io.reactivex.Flowable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Repository
public class JdbcServiceInfoRepository {

	private Database database;

	@Autowired
	public JdbcServiceInfoRepository(Database database) {
		this.database = database;
	}

	public Mono<ServiceDetail> save(ServiceDetail entity) {
		String createOne = "insert into service_detail (organization, space, name, service, description, plan, type, bound_applications, last_operation, last_updated, dashboard_url, requested_state) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Flowable<Integer> insert = database
			.update(createOne)
			.parameters(
				entity.getOrganization(),
				entity.getSpace(),
				entity.getName(),
				entity.getService(),
				entity.getDescription(),
				entity.getPlan(),
				entity.getType(),
				entity.getApplications(),
				entity.getLastOperation(),
				entity.getLastUpdated() != null ? Timestamp.valueOf(entity.getLastUpdated()): null,
				entity.getDashboardUrl(),
				entity.getRequestedState()
			)
			.returnGeneratedKeys()
			.getAs(Integer.class);

		String selectOne = "select id, organization, space, name, service, description, plan, type, bound_applications, last_operation, last_updated, dashboard_url, requested_state from service_detail where id = ?";
		Flowable<ServiceDetail> result = database
			.select(selectOne)
			.parameterStream(insert)
			.get(rs -> ServiceDetail
						.builder()
						.id(String.valueOf(rs.getInt(1)))
						.organization(rs.getString(2))
						.space(rs.getString(3))
						.name(rs.getString(4))
						.service(rs.getString(5))
						.description(rs.getString(6))
						.plan(rs.getString(7))
						.type(rs.getString(8))
						.applications(rs.getString(9))
						.lastOperation(rs.getString(10))
						.lastUpdated(rs.getTimestamp(11) != null ? rs.getTimestamp(11).toLocalDateTime(): null)
						.dashboardUrl(rs.getString(12))
						.requestedState(rs.getString(13))
						.build());
		return Mono.from(result);
	}

	public Flux<ServiceDetail> findAll() {
		String selectAll = "select id, organization, space, name, service, description, plan, type, bound_applications, last_operation, last_updated, dashboard_url, requested_state from service_detail order by organization, space, service, name";
		Flowable<ServiceDetail> result = database
			.select(selectAll)
			.get(rs -> ServiceDetail
						.builder()
						.id(String.valueOf(rs.getInt(1)))
						.organization(rs.getString(2))
						.space(rs.getString(3))
						.name(rs.getString(4))
						.service(rs.getString(5))
						.description(rs.getString(6))
						.plan(rs.getString(7))
						.type(rs.getString(8))
						.applications(rs.getString(9))
						.lastOperation(rs.getString(10))
						.lastUpdated(rs.getTimestamp(11) != null ? rs.getTimestamp(11).toLocalDateTime(): null)
						.dashboardUrl(rs.getString(12))
						.requestedState(rs.getString(13))
						.build());
		return Flux.from(result);
	}

	public Mono<Void> deleteAll() {
		String deleteAll = "delete from service_detail";
		Flowable<Integer> result = database
			.update(deleteAll)
			.counts();
		return Flux.from(result).then();
	}
}