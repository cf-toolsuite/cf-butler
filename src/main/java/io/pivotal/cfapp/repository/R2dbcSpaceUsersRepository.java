package io.pivotal.cfapp.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.cfapp.domain.Defaults;
import io.pivotal.cfapp.domain.SpaceUsers;
import io.r2dbc.spi.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.DatabaseClient.GenericInsertSpec;
import org.springframework.data.r2dbc.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class R2dbcSpaceUsersRepository {

	private final DatabaseClient client;
	private final ObjectMapper mapper;

	@Autowired
	public R2dbcSpaceUsersRepository(DatabaseClient client, ObjectMapper mapper) {
		this.client = client;
		this.mapper = mapper;
	}

	public Mono<Void> deleteAll() {
		return client
				.delete()
				.from("space_users")
				.fetch()
				.rowsUpdated()
				.then();
	}

	public Mono<SpaceUsers> save(SpaceUsers entity) {
		GenericInsertSpec<Map<String, Object>> spec = client.insert().into("space_users").value("organization",
				entity.getOrganization());
		spec = spec.value("space", entity.getSpace());
		if (entity.getManagers() != null) {
			spec = spec.value("managers", toJson(entity.getManagers()));
		} else {
			spec = spec.nullValue("managers");
		}
		if (entity.getAuditors() != null) {
			spec = spec.value("auditors", toJson(entity.getAuditors()));
		} else {
			spec = spec.nullValue("auditors");
		}
		if (entity.getDevelopers() != null) {
			spec = spec.value("developers", toJson(entity.getDevelopers()));
		} else {
			spec = spec.nullValue("developers");
		}
		return spec.fetch().rowsUpdated().then(Mono.just(entity));
	}

	public Mono<SpaceUsers> findByOrganizationAndSpace(String organization, String space) {
		return client
				.select()
					.from("space_users")
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
				.organization(Defaults.getColumnValueOrDefault(row, "organization", String.class, ""))
				.space(Defaults.getColumnValueOrDefault(row, "space", String.class, ""))
				.auditors(toList(Defaults.getColumnValueOrDefault(row, "auditors", String.class, "")))
				.developers(toList(Defaults.getColumnValueOrDefault(row, "developers", String.class, "")))
				.managers(toList(Defaults.getColumnValueOrDefault(row, "managers", String.class, "")))
				.build();
	}

	private String toJson(List<String> list) {
		List<String> destination = new ArrayList<>(list);
		destination.replaceAll(String::toLowerCase);
		try {
			return mapper.writeValueAsString(destination);
		} catch (JsonProcessingException jpe) {
			throw new RuntimeException(jpe);
		}
	}

	private List<String> toList(String json) {
		try {
			return mapper.readValue(
					json, new TypeReference<List<String>>() {});
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
}