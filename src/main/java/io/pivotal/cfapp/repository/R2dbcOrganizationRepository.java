package io.pivotal.cfapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.Defaults;
import io.pivotal.cfapp.domain.Organization;
import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcOrganizationRepository {

	private final DatabaseClient client;

	@Autowired
	public R2dbcOrganizationRepository(DatabaseClient client) {
		this.client = client;
	}

	public Mono<Void> deleteAll() {
		String deleteAll = "delete from organizations";
		return client.execute(deleteAll).fetch().rowsUpdated().then();
	}

	public Mono<Organization> save(Organization entity) {
		return client
				.insert()
					.into("organizations")
					.value("id", entity.getId())
					.value("org_name", entity.getName())
				.fetch()
				.rowsUpdated()
				.then(Mono.just(entity));
	}

	public Flux<Organization> findAll() {
		String selectAll = "select id, org_name from organizations order by org_name";
		return client.execute(selectAll).map((row, metadata) -> fromRow(row)).all();
	}

	private Organization fromRow(Row row) {
		return new Organization(row.get("id", String.class), Defaults.getValueOrDefault(row.get("org_name", String.class), ""));
	}

}
