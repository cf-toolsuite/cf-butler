package io.pivotal.cfapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.Defaults;
import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.domain.SpaceUsersShim;
import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcSpaceUsersRepository {

	private final DatabaseClient client;

	@Autowired
	public R2dbcSpaceUsersRepository(R2dbcEntityOperations ops) {
		this.client = DatabaseClient.create(ops.getDatabaseClient().getConnectionFactory());
	}

	public Mono<Void> deleteAll() {
		return client
				.delete()
				.from(SpaceUsers.tableName())
				.fetch()
				.rowsUpdated()
				.then();
	}

	public Mono<SpaceUsers> save(SpaceUsers entity) {
		SpaceUsersShim shim = SpaceUsersShim.from(entity);
		return
			client
				.insert()
				.into(SpaceUsersShim.class)
				.table(SpaceUsers.tableName())
				.using(shim)
				.fetch()
				.rowsUpdated()
				.then(Mono.just(entity));
	}

	public Mono<SpaceUsers> findByOrganizationAndSpace(String organization, String space) {
		return client
				.select()
					.from(SpaceUsers.tableName())
					.project("pk", "organization", "space", "auditors", "managers", "developers")
					.matching(Criteria.where("organization").is(organization).and("space").is(space))
					.as(SpaceUsers.class)
				.fetch()
				.one();
	}

	public Flux<SpaceUsers> findAll() {
		String selectAll = "select pk, organization, space, auditors, managers, developers from space_users order by organization, space";
		return client.execute(selectAll).map((row, metadata) -> fromRow(row)).all();
	}

	private SpaceUsers fromRow(Row row) {
		return SpaceUsers.builder()
				.pk(row.get("pk", Long.class))
				.organization(Defaults.getColumnValue(row, "organization", String.class))
				.space(Defaults.getColumnValue(row, "space", String.class))
				.auditors(Defaults.getColumnListOfStringValue(row, "auditors"))
				.developers(Defaults.getColumnListOfStringValue(row, "developers"))
				.managers(Defaults.getColumnListOfStringValue(row, "managers"))
				.build();
	}

}