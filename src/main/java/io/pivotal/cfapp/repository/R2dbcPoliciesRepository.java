package io.pivotal.cfapp.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.DatabaseClient.GenericInsertSpec;
import org.springframework.data.r2dbc.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import io.pivotal.cfapp.domain.ApplicationOperation;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.Defaults;
import io.pivotal.cfapp.domain.EmailNotificationTemplate;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.domain.Query;
import io.pivotal.cfapp.domain.QueryPolicy;
import io.pivotal.cfapp.domain.ServiceInstanceOperation;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcPoliciesRepository {

	private final DatabaseClient dbClient;
	private final PolicyIdProvider idProvider;
	private final ObjectMapper mapper;

	@Autowired
	public R2dbcPoliciesRepository(
		DatabaseClient dbClient,
		PolicyIdProvider idProvider,
		ObjectMapper mapper) {
		this.dbClient = dbClient;
		this.idProvider = idProvider;
		this.mapper = mapper;
	}

	public Mono<Policies> save(Policies entity) {
		List<ApplicationPolicy> applicationPolicies =
			entity.getApplicationPolicies().stream()
				.map(p -> idProvider.seedApplicationPolicy(p)).collect(Collectors.toList());

		List<ServiceInstancePolicy> serviceInstancePolicies =
			entity.getServiceInstancePolicies().stream()
				.map(p -> idProvider.seedServiceInstancePolicy(p)).collect(Collectors.toList());

		List<QueryPolicy> queryPolicies =
			entity.getQueryPolicies().stream()
				.map(p -> idProvider.seedQueryPolicy(p)).collect(Collectors.toList());

		return Flux.fromIterable(applicationPolicies)
					.concatMap(ap -> saveApplicationPolicy(ap))
					.thenMany(Flux.fromIterable(serviceInstancePolicies)
					.concatMap(sip -> saveServiceInstancePolicy(sip)))
					.thenMany(Flux.fromIterable(queryPolicies)
					.concatMap(qp -> saveQueryPolicy(qp)))
					.then(Mono.just(new Policies(applicationPolicies, serviceInstancePolicies, queryPolicies)));
	}

	public Mono<Policies> findServiceInstancePolicyById(String id) {
		List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
		return
			Flux
				.from(dbClient
						.select()
							.from(ServiceInstancePolicy.tableName())
							.project(ServiceInstancePolicy.columnNames())
							.matching(Criteria.where("id").is(id))
						.map((row, metadata) ->
							ServiceInstancePolicy
								.builder()
									.pk(row.get("pk", Long.class))
									.id(row.get("id", String.class))
									.operation(row.get("operation", String.class))
									.description(Defaults.getValueOrDefault(row.get("description", String.class), ""))
									.options(readOptions(row.get("options", String.class) == null ? "{}" : row.get("options", String.class)))
									.organizationWhiteList(row.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(row.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
									.build())
						.all())
				.map(sp -> serviceInstancePolicies.add(sp))
				.then(Mono.just(new Policies(Collections.emptyList(), serviceInstancePolicies, Collections.emptyList())))
				.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}

	public Mono<Policies> findApplicationPolicyById(String id) {
		List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
		return
			Flux
				.from(dbClient
						.select()
							.from(ApplicationPolicy.tableName())
							.project(ApplicationPolicy.columnNames())
							.matching(Criteria.where("id").is(id))
						.map((row, metadata) ->
							ApplicationPolicy
								.builder()
									.pk(row.get("pk", Long.class))
									.id(row.get("id", String.class))
									.operation(row.get("operation", String.class))
									.description(Defaults.getValueOrDefault(row.get("description", String.class), ""))
									.options(readOptions(row.get("options", String.class) == null ? "{}" : row.get("options", String.class)))
									.organizationWhiteList(row.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(row.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
									.state(row.get("state", String.class))
									.build())
						.all())
				.map(ap -> applicationPolicies.add(ap))
				.then(Mono.just(new Policies(applicationPolicies, Collections.emptyList(), Collections.emptyList())))
				.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}

	public Mono<Policies> findQueryPolicyById(String id) {
		List<QueryPolicy> queryPolicies = new ArrayList<>();
		return
			Flux
				.from(dbClient
						.select()
							.from(QueryPolicy.tableName())
							.project(QueryPolicy.columnNames())
							.matching(Criteria.where("id").is(id))
						.map((row, metadata) ->
							QueryPolicy
								.builder()
									.pk(row.get("pk", Long.class))
									.id(row.get("id", String.class))
									.description(Defaults.getValueOrDefault(row.get("description", String.class), ""))
									.queries(readQueries(row.get("queries", String.class) == null ? "{}" : row.get("queries", String.class)))
									.emailNotificationTemplate(readEmailNotificationTemplate(row.get("email_notification_template", String.class) == null ? "{}": row.get("email_notification_template", String.class)))
									.build())
						.all())
				.map(qp -> queryPolicies.add(qp))
				.then(Mono.just(new Policies(Collections.emptyList(), Collections.emptyList(), queryPolicies)))
				.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}

	public Mono<Policies> findAll() {
		String selectAllApplicationPolicies = "select pk, id, operation, description, state, options, organization_whitelist from application_policy";
		String selectAllServiceInstancePolicies = "select pk, id, operation, description, options, organization_whitelist from service_instance_policy";
		String selectAllQueryPolicies = "select pk, id, description, queries, email_notification_template from query_policy";
		List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
		List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
		List<QueryPolicy> queryPolicies = new ArrayList<>();

		return
				Flux
					.from(dbClient.execute(selectAllApplicationPolicies)
							.map((row, metadata) ->
								ApplicationPolicy
									.builder()
										.pk(row.get("pk", Long.class))
										.id(row.get("id", String.class))
										.operation(row.get("operation", String.class))
										.description(row.get("description", String.class))
										.options(readOptions(row.get("options", String.class) == null ? "{}" : row.get("options", String.class)))
										.organizationWhiteList(row.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(row.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
										.state(row.get("state", String.class))
										.build())
							.all())
					.map(ap -> applicationPolicies.add(ap))
					.thenMany(
						Flux
							.from(dbClient.execute(selectAllServiceInstancePolicies)
									.map((row, metadata) ->
										ServiceInstancePolicy
											.builder()
												.pk(row.get("pk", Long.class))
												.id(row.get("id", String.class))
												.operation(row.get("operation", String.class))
												.description(Defaults.getValueOrDefault(row.get("description", String.class), ""))
												.options(readOptions(row.get("options", String.class) == null ? "{}" : row.get("options", String.class)))
												.organizationWhiteList(row.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(row.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
												.build())
									.all())
							.map(sp -> serviceInstancePolicies.add(sp)))
					.thenMany(
						Flux
							.from(dbClient.execute(selectAllQueryPolicies)
									.map((row, metadata) ->
										QueryPolicy
											.builder()
												.pk(row.get("pk", Long.class))
												.id(row.get("id", String.class))
												.description(Defaults.getValueOrDefault(row.get("description", String.class), ""))
												.queries(readQueries(row.get("queries", String.class) == null ? "[]" : row.get("queries", String.class)))
												.emailNotificationTemplate(readEmailNotificationTemplate(row.get("email_notification_template", String.class) == null ? "{}": row.get("email_notification_template", String.class)))
												.build())
									.all())
							.map(qp -> queryPolicies.add(qp)))
					.then(Mono.just(new Policies(applicationPolicies, serviceInstancePolicies, queryPolicies)))
					.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}

	public Mono<Policies> findAllQueryPolicies() {
		return
			dbClient
				.select()
				.from(QueryPolicy.tableName())
				.project(QueryPolicy.columnNames())
				.map(
					(row, metadata) ->
						QueryPolicy
							.builder()
								.pk(row.get("pk", Long.class))
								.id(row.get("id", String.class))
								.description(Defaults.getValueOrDefault(row.get("description", String.class), ""))
								.queries(readQueries(row.get("queries", String.class) == null ? "[]" : row.get("queries", String.class)))
								.emailNotificationTemplate(
									readEmailNotificationTemplate(
										row.get("email_notification_template", String.class) == null
											? "{}"
											: row.get("email_notification_template", String.class)))
							.build())
				.all()
				.collectList()
				.map(qps -> new Policies(Collections.emptyList(), Collections.emptyList(), qps))
				.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}

	public Mono<Void> deleteApplicationPolicyById(String id) {
		return
			Flux
				.from(dbClient
						.delete()
							.from(ApplicationPolicy.tableName())
							.matching(Criteria.where("id").is(id))
						.fetch()
						.rowsUpdated())
				.then();
	}

	public Mono<Void> deleteServiceInstancePolicyById(String id) {
		return
			Flux
				.from(dbClient
						.delete()
							.from(ServiceInstancePolicy.tableName())
							.matching(Criteria.where("id").is(id))
						.fetch()
						.rowsUpdated())
				.then();
	}

	public Mono<Void> deleteQueryPolicyById(String id) {
		return
			Flux
				.from(dbClient
						.delete()
							.from(QueryPolicy.tableName())
							.matching(Criteria.where("id").is(id))
						.fetch()
						.rowsUpdated())
				.then();
	}

	public Mono<Void> deleteAll() {
		return
			Flux
				.from(
					dbClient
						.delete()
						.from(ApplicationPolicy.tableName())
						.fetch()
						.rowsUpdated()
				)
				.thenMany(
					Flux
						.from(
							dbClient
								.delete()
								.from(ServiceInstancePolicy.tableName())
								.fetch()
								.rowsUpdated())
						)
				.then();
	}

	private Mono<Integer> saveApplicationPolicy(ApplicationPolicy ap) {
		GenericInsertSpec<Map<String, Object>> spec =
			dbClient.insert().into(ApplicationPolicy.tableName())
				.value("id", ap.getId());
		if (ap.getDescription() != null) {
			spec = spec.value("description", ap.getDescription());
		} else {
			spec = spec.nullValue("description");
		}
		if (ap.getState() != null) {
			spec = spec.value("state", ap.getState());
		} else {
			spec = spec.nullValue("state");
		}
		if (ap.getOperation() != null) {
			spec = spec.value("operation", ap.getOperation());
		} else {
			spec = spec.nullValue("operation");
		}
		if (!CollectionUtils.isEmpty(ap.getOptions())) {
			spec = spec.value("options", writeOptions(ap.getOptions()));
		} else {
			spec = spec.nullValue("options");
		}
		spec = spec.value("organization_whitelist", String.join(",", ap.getOrganizationWhiteList()));
		return spec.fetch().rowsUpdated();
	}

	private Mono<Integer> saveServiceInstancePolicy(ServiceInstancePolicy sip) {
		GenericInsertSpec<Map<String, Object>> spec =
			dbClient.insert().into(ServiceInstancePolicy.tableName())
				.value("id", sip.getId());
		if (sip.getDescription() != null) {
			spec = spec.value("description", sip.getDescription());
		} else {
			spec = spec.nullValue("description");
		}
		if (sip.getOperation() != null) {
			spec = spec.value("operation", sip.getOperation());
		} else {
			spec = spec.nullValue("operation");
		}
		if (!CollectionUtils.isEmpty(sip.getOptions())) {
			spec = spec.value("options", writeOptions(sip.getOptions()));
		} else {
			spec = spec.nullValue("options");
		}
		spec = spec.value("organization_whitelist", String.join(",", sip.getOrganizationWhiteList()));
		return spec.fetch().rowsUpdated();
	}

	private Mono<Integer> saveQueryPolicy(QueryPolicy qp) {
		GenericInsertSpec<Map<String, Object>> spec =
			dbClient.insert().into(QueryPolicy.tableName())
				.value("id", qp.getId());
		if (qp.getDescription() != null) {
			spec = spec.value("description", qp.getDescription());
		} else {
			spec = spec.nullValue("description");
		}
		if (!CollectionUtils.isEmpty(qp.getQueries())) {
			spec = spec.value("queries", writeQueries(qp.getQueries()));
		} else {
			spec = spec.nullValue("queries");
		}
		if (qp.getEmailNotificationTemplate() != null) {
			spec = spec.value("email_notification_template", writeEmailNotificationTemplate(qp.getEmailNotificationTemplate()));
		} else {
			spec = spec.nullValue("email_notification_template");
		}
		return spec.fetch().rowsUpdated();
	}

	public Mono<Policies> findByApplicationOperation(ApplicationOperation operation) {
		List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
		List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
		List<QueryPolicy> queryPolicies = new ArrayList<>();
		return
				Flux
					.from(dbClient
							.select()
								.from(ApplicationPolicy.tableName())
								.project(ApplicationPolicy.columnNames())
								.matching(Criteria.where("operation").is(operation.getName()))
							.map((row, metadata) ->
								ApplicationPolicy
									.builder()
										.pk(row.get("pk", Long.class))
										.id(row.get("id", String.class))
										.operation(row.get("operation", String.class))
										.description(row.get("description", String.class))
										.options(readOptions(row.get("options", String.class) == null ? "{}" : row.get("options", String.class)))
										.organizationWhiteList(row.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(row.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
										.state(row.get("state", String.class))
										.build())
							.all())
					.map(ap -> applicationPolicies.add(ap))
					.then(Mono.just(new Policies(applicationPolicies, serviceInstancePolicies, queryPolicies)))
					.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}

	public Mono<Policies> findByServiceInstanceOperation(ServiceInstanceOperation operation) {
		List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
		List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
		List<QueryPolicy> queryPolicies = new ArrayList<>();
		return
				Flux
					.from(dbClient
							.select()
								.from(ServiceInstancePolicy.tableName())
								.project(ServiceInstancePolicy.columnNames())
								.matching(Criteria.where("operation").is(operation.getName()))
							.map((row, metadata) ->
								ServiceInstancePolicy
									.builder()
										.pk(row.get("pk", Long.class))
										.id(row.get("id", String.class))
										.operation(row.get("operation", String.class))
										.description(Defaults.getValueOrDefault(row.get("description", String.class), ""))
										.options(readOptions(row.get("options", String.class) == null ? "{}" : row.get("options", String.class)))
										.organizationWhiteList(row.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(row.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
										.build())
							.all())
					.map(sp -> serviceInstancePolicies.add(sp))
					.then(Mono.just(new Policies(applicationPolicies, serviceInstancePolicies, queryPolicies)))
					.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}

	private String writeQueries(Set<Query> value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Problem writing queries", jpe);
        }
	}

	private String writeOptions(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Problem writing options", jpe);
        }
	}

	private String writeEmailNotificationTemplate(EmailNotificationTemplate value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Problem writing email notification template", jpe);
        }
	}

	private Map<String, Object> readOptions(String value) {
        try {
            return mapper.readValue(value, new TypeReference<Map<String, Object>>() {});
        } catch (IOException ioe) {
            throw new RuntimeException("Problem reading options", ioe);
        }
	}

	private Set<Query> readQueries(String value) {
        try {
            return mapper.readValue(value, new TypeReference<Set<Query>>() {});
        } catch (IOException ioe) {
            throw new RuntimeException("Problem reading queries", ioe);
        }
	}

	private EmailNotificationTemplate readEmailNotificationTemplate(String value) {
        try {
            return mapper.readValue(value, EmailNotificationTemplate.class);
        } catch (IOException ioe) {
            throw new RuntimeException("Problem reading email notification template", ioe);
        }
	}
}
