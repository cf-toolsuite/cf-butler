package io.pivotal.cfapp.repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.DatabaseClient.GenericInsertSpec;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.config.DbmsSettings;
import io.pivotal.cfapp.domain.Defaults;
import io.pivotal.cfapp.domain.SpaceUsers;
import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcSpaceUsersRepository {

	private final DatabaseClient client;
	private final ObjectMapper mapper;
	private final DbmsSettings settings;

	@Autowired
	public R2dbcSpaceUsersRepository(DatabaseClient client, ObjectMapper mapper, DbmsSettings settings) {
		this.client = client;
		this.mapper = mapper;
		this.settings = settings;
	}

	public Mono<Void> deleteAll() {
		String deleteAll = "delete from space_users";
		return client.execute(deleteAll).fetch().rowsUpdated().then();
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
		if (entity.getManagers() != null) {
			spec = spec.value("developers", toJson(entity.getDevelopers()));
		} else {
			spec = spec.nullValue("developers");
		}
		return spec.fetch().rowsUpdated().then(Mono.just(entity));
	}

	public Mono<SpaceUsers> findByOrganizationAndSpace(String organization, String space) {
		String selectOne = "select pk, organization, space, auditors, managers, developers from space_users where organization = "
				+ settings.getBindPrefix() + 1 + " and space = " + settings.getBindPrefix() + 2;
		return client.execute(selectOne).bind(settings.getBindPrefix() + 1, organization)
				.bind(settings.getBindPrefix() + 2, space).as(SpaceUsers.class).fetch().one();
	}

	public Flux<SpaceUsers> findAll() {
		String selectAll = "select pk, organization, space, auditors, managers, developers from space_users order by organization, space";
		return client.execute(selectAll).map((row, metadata) -> fromRow(row)).all();
	}

	private SpaceUsers fromRow(Row row) {
		return SpaceUsers.builder()
				.pk(row.get("pk", Long.class))
				.organization(Defaults.getValueOrDefault(row.get("organization", String.class), ""))
				.space(Defaults.getValueOrDefault(row.get("space", String.class), ""))
				.auditors(toList(Defaults.getValueOrDefault(row.get("auditors", String.class), "")))
				.developers(toList(Defaults.getValueOrDefault(row.get("developers", String.class), "")))
				.managers(toList(Defaults.getValueOrDefault(row.get("managers", String.class), "")))
				.build();
	}

	private String toJson(List<String> list) {
		try {
			return mapper.writeValueAsString(list);
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
