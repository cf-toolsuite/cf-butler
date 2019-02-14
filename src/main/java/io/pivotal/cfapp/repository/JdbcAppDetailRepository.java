package io.pivotal.cfapp.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.reactivex.Flowable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Profile("jdbc")
@Repository
public class JdbcAppDetailRepository {

	private Database database;

	@Autowired
	public JdbcAppDetailRepository(Database database) {
		this.database = database;
	}

	public Mono<AppDetail> save(AppDetail entity) {
		String createOne = "insert into application_detail (organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		return Mono
				.from(database
						.update(createOne)
						.parameters(
							entity.getOrganization(),
							entity.getSpace(),
							entity.getAppId(),
							entity.getAppName(),
							entity.getBuildpack(),
							entity.getImage(),
							entity.getStack(),
							entity.getRunningInstances(),
							entity.getTotalInstances(),
							entity.getUrls(),
							entity.getLastPushed() != null ? Timestamp.valueOf(entity.getLastPushed()): null,
							entity.getLastEvent(),
							entity.getLastEventActor(),
							entity.getLastEventTime() != null ? Timestamp.valueOf(entity.getLastEventTime()): null,
							entity.getRequestedState()
						)
						.counts())
						.then(Mono.just(entity));
			
	}

	public Flux<AppDetail> findAll() {
		String selectAll = "select organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state from application_detail order by organization, space, app_name";
		Flowable<AppDetail> result = database
			.select(selectAll)
			.get(rs -> fromResultSet(rs));
		return Flux.from(result);
	}

	public Mono<Void> deleteAll() {
		String deleteAll = "delete from application_detail";
		Flowable<Integer> result = database
			.update(deleteAll)
			.counts();
		return Flux.from(result).then();
	}
	
	public Mono<AppDetail> findByAppId(String appId) {
		String selectOne = "select organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state from application_detail where app_id = ?";
		return Mono.from(database
			.select(selectOne)
			.parameter(appId)
			.get(rs -> fromResultSet(rs)));
	}
	
	public Flux<Tuple2<AppDetail, ApplicationPolicy>> findByApplicationPolicy(ApplicationPolicy policy, boolean mayHaveServiceBindings) {
		return mayHaveServiceBindings == true 
				? findAppicationsThatMayHaveServiceBindings(policy)
						: findAppicationsThatDoNotHaveServiceBindings(policy);
	}
	
	private Flux<Tuple2<AppDetail, ApplicationPolicy>> findAppicationsThatMayHaveServiceBindings(ApplicationPolicy policy) {
		String select = "select organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state";
		String from = "from application_detail";
		StringBuilder where = new StringBuilder();
		List<Object> paramValues = new ArrayList<>();
		where.append("where requested_state = ? ");
		paramValues.add(policy.getState());
		if (policy.getFromDateTime() != null) {
			where.append("and last_event_time <= ? ");
			paramValues.add(Timestamp.valueOf(policy.getFromDateTime()));
		}
		if (policy.getFromDuration() != null) {
			where.append("and last_event_time <= ?");
			LocalDateTime eventTime = LocalDateTime.now().minus(policy.getFromDuration());
			paramValues.add(Timestamp.valueOf(eventTime));
		}
		String orderBy = "order by organization, space, app_name";
		String sql = String.join(" ", select, from, where, orderBy);
		Flowable<Tuple2<AppDetail, ApplicationPolicy>> result = 
			database
				.select(sql)
				.parameters(paramValues)
				.get(rs -> toTuple(fromResultSet(rs), policy));
		return Flux.from(result);
	}
	
	private Flux<Tuple2<AppDetail, ApplicationPolicy>> findAppicationsThatDoNotHaveServiceBindings(ApplicationPolicy policy) {
		String select = 
				"select ad.organization, ad.space, ad.app_id, ad.app_name, ad.buildpack, ad.image, " + 
				"ad.stack, ad.running_instances, ad.total_instances, ad.urls, ad.last_pushed, ad.last_event, " + 
				"ad.last_event_actor, ad.last_event_time, ad.requested_state";
		String from = "from application_detail ad";
		String leftJoin = "left join application_relationship ar on ad.app_id = ar.app_id";
		StringBuilder where = new StringBuilder();
		List<Object> paramValues = new ArrayList<>();
		where.append("where ar.service_id is null and ad.requested_state = ? ");
		paramValues.add(policy.getState());
		if (policy.getFromDateTime() != null) {
			where.append("and ad.last_event_time <= ?");
			paramValues.add(Timestamp.valueOf(policy.getFromDateTime()));
		}
		if (policy.getFromDuration() != null) {
			where.append("and ad.last_event_time <= ?");
			LocalDateTime eventTime = LocalDateTime.now().minus(policy.getFromDuration());
			paramValues.add(Timestamp.valueOf(eventTime));
		}
		String orderBy = "order by ad.organization, ad.space, ad.app_name";
		String sql = String.join(" ", select, from, leftJoin, where, orderBy);
		Flowable<Tuple2<AppDetail, ApplicationPolicy>> result = 
			database
				.select(sql)
				.parameters(paramValues)
				.get(rs -> toTuple(fromResultSet(rs), policy));
		return Flux.from(result);
	}
	
	private AppDetail fromResultSet(ResultSet rs) throws SQLException {
		return AppDetail
				.builder()
					.organization(rs.getString(1))
					.space(rs.getString(2))
					.appId(rs.getString(3))
					.appName(rs.getString(4))
					.buildpack(rs.getString(5))
					.image(rs.getString(6))
					.stack(rs.getString(7))
					.runningInstances(rs.getInt(8))
					.totalInstances(rs.getInt(9))
					.urls(rs.getString(10))
					.lastPushed(rs.getTimestamp(11) != null ? rs.getTimestamp(11).toLocalDateTime(): null)
					.lastEvent(rs.getString(12))
					.lastEventActor(rs.getString(13))
					.lastEventTime(rs.getTimestamp(14) != null ? rs.getTimestamp(14).toLocalDateTime(): null)
					.requestedState(rs.getString(15))
					.build();
	}
	
	private Tuple2<AppDetail, ApplicationPolicy> toTuple(AppDetail detail, ApplicationPolicy policy) {
		return Tuples.of(detail, policy);
	}

}
