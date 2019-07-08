package io.pivotal.cfapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.Defaults;
import io.pivotal.cfapp.domain.Space;
import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcSpaceRepository {

	private final DatabaseClient client;

	@Autowired
	public R2dbcSpaceRepository(DatabaseClient client) {
		this.client = client;
	}

	public Mono<Void> deleteAll() {
		String deleteAll = "delete from spaces";
		return client.execute(deleteAll).fetch().rowsUpdated().then();
	}

	public Mono<Space> save(Space entity) {
		return client
				.insert()
					.into("spaces")
					.value("org_name", entity.getOrganization())
					.value("space_name", entity.getSpace())
				.fetch()
				.rowsUpdated()
				.then(Mono.just(entity));
	}

	public Flux<Space> findAll() {
		String selectAll = "select org_name, space_name from spaces order by org_name, space_name";
		return client.execute(selectAll).map((row, metadata) -> fromRow(row)).all();
	}

	private Space fromRow(Row row) {
		return new Space(row.get("org_name", String.class), Defaults.getValueOrDefault(row.get("space_name", String.class), ""));
	}
}