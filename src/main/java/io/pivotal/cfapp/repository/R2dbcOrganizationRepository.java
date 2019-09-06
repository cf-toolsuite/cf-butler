package io.pivotal.cfapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.Defaults;
import io.pivotal.cfapp.domain.Organization;
import io.pivotal.cfapp.domain.OrganizationShim;
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
		return client
				.delete()
				.from(Organization.tableName())
				.fetch()
				.rowsUpdated()
				.then();
	}

	public Mono<Organization> save(Organization entity) {
		OrganizationShim shim =
			OrganizationShim
				.builder()
					.id(entity.getId())
					.orgName(entity.getName())
					.build();
		return client
				.insert()
				.into(OrganizationShim.class)
				.table(Organization.tableName())
				.using(shim)
				.fetch()
				.rowsUpdated()
				.then(Mono.just(entity));
	}

	public Flux<Organization> findAll() {
		String selectAll = "select id, org_name from organizations order by org_name";
		return client.execute(selectAll).map((row, metadata) -> fromRow(row)).all();
	}

	private Organization fromRow(Row row) {
		return new Organization(row.get("id", String.class), Defaults.getColumnValueOrDefault(row, "org_name", String.class, ""));
	}

}
