package io.pivotal.cfapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.domain.SpaceShim;
import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcSpaceRepository {

	private final DatabaseClient client;

	@Autowired
	public R2dbcSpaceRepository(R2dbcEntityOperations ops) {
		this.client = DatabaseClient.create(ops.getDatabaseClient().getConnectionFactory());
	}

	public Mono<Void> deleteAll() {
		return client
				.delete()
				.from(Space.tableName())
				.fetch()
				.rowsUpdated()
				.then();
	}

	public Mono<Space> save(Space entity) {
		SpaceShim shim =
			SpaceShim
				.builder()
					.orgId(entity.getOrganizationId())
					.orgName(entity.getOrganizationName())
					.spaceId(entity.getSpaceId())
					.spaceName(entity.getSpaceName())
				.build();
		return client
				.insert()
				.into(SpaceShim.class)
				.table(Space.tableName())
				.using(shim)
				.fetch()
				.rowsUpdated()
				.then(Mono.just(entity));
	}

	public Flux<Space> findAll() {
		return client
				.select()
				.from(Space.tableName())
				.project(Space.columnNames())
				.orderBy(Order.asc("org_name"), Order.asc("space_name"))
				.map((row, metadata) -> fromRow(row))
				.all();
	}

	private Space fromRow(Row row) {
		return
			Space
				.builder()
					.organizationId(row.get("org_id", String.class))
					.organizationName(row.get("org_name", String.class))
					.spaceId(row.get("space_id", String.class))
					.spaceName(row.get("space_name", String.class))
				.build();
	}
}