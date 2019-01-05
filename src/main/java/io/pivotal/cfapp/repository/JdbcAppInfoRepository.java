package io.pivotal.cfapp.repository;

import java.sql.Timestamp;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.AppDetail;
import io.reactivex.Flowable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Repository
public class JdbcAppInfoRepository {

	private Database database;

	@Autowired
	public JdbcAppInfoRepository(Database database) {
		this.database = database;
	}

	public Mono<AppDetail> save(AppDetail entity) {
		String createOne = "insert into app_detail (organization, space, app_name, buildpack, image, stack, running_instances, total_instances, urls, last_pushed, last_event, last_event_actor, requested_state) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Flowable<Integer> insert = database
			.update(createOne)
			.parameters(
				entity.getOrganization(),
				entity.getSpace(),
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
				entity.getRequestedState()
			)
			.returnGeneratedKeys()
			.getAs(Integer.class);

		String selectOne = "select id, organization, space, app_name, buildpack, image, stack, running_instances, total_instances, urls, last_pushed, last_event, last_event_actor, requested_state from app_detail where id = ?";
		Flowable<AppDetail> result = database
			.select(selectOne)
			.parameterStream(insert)
			.get(rs -> AppDetail
						.builder()
						.id(String.valueOf(rs.getInt(1)))
						.organization(rs.getString(2))
						.space(rs.getString(3))
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
						.requestedState(rs.getString(14))
						.build());
		return Mono.from(result);
	}

	public Flux<AppDetail> findAll() {
		String selectAll = "select id, organization, space, app_name, buildpack, image, stack, running_instances, total_instances, urls, last_pushed, last_event, last_event_actor, requested_state from app_detail order by organization, space, app_name";
		Flowable<AppDetail> result = database
			.select(selectAll)
			.get(rs -> AppDetail
						.builder()
						.id(String.valueOf(rs.getInt(1)))
						.organization(rs.getString(2))
						.space(rs.getString(3))
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
						.requestedState(rs.getString(14))
						.build());
		return Flux.from(result);
	}

	public Mono<Void> deleteAll() {
		String deleteAll = "delete from app_detail";
		Flowable<Integer> result = database
			.update(deleteAll)
			.counts();
		return Flux.from(result).then();
	}
}
