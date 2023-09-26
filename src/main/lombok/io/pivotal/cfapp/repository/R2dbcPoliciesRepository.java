package io.pivotal.cfapp.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.ApplicationOperation;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.EndpointPolicy;
import io.pivotal.cfapp.domain.HygienePolicy;
import io.pivotal.cfapp.domain.ResourceNotificationPolicy;
import io.pivotal.cfapp.domain.LegacyPolicy;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.domain.QueryPolicy;
import io.pivotal.cfapp.domain.ServiceInstanceOperation;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcPoliciesRepository {

    private final R2dbcEntityOperations dbClient;
    private final PolicyIdProvider idProvider;

    @Autowired
    public R2dbcPoliciesRepository(
            R2dbcEntityOperations dbClient,
            PolicyIdProvider idProvider) {
        this.dbClient = dbClient;
        this.idProvider = idProvider;
    }

    public Mono<Void> deleteAll() {
        return
                dbClient
                .delete(ApplicationPolicy.class).all()
                .then(dbClient.delete(ServiceInstancePolicy.class).all())
                .then(dbClient.delete(EndpointPolicy.class).all())
                .then(dbClient.delete(QueryPolicy.class).all())
                .then(dbClient.delete(HygienePolicy.class).all())
                .then(dbClient.delete(ResourceNotificationPolicy.class).all())
                .then(dbClient.delete(LegacyPolicy.class).all())
                .then();
    }

    public Mono<Void> deleteApplicationPolicyById(String id) {
        return
                dbClient
                .delete(ApplicationPolicy.class)
                .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("id").is(id)))
                .all()
                .then();
    }

    public Mono<Void> deleteEndpointPolicyById(String id) {
        return
                dbClient
                .delete(EndpointPolicy.class)
                .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("id").is(id)))
                .all()
                .then();
    }

    public Mono<Void> deleteHygienePolicyById(String id) {
        return
                dbClient
                .delete(HygienePolicy.class)
                .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("id").is(id)))
                .all()
                .then();
    }

    public Mono<Void> deleteResourceNotificationPolicyById(String id) {
        return
                dbClient
                .delete(ResourceNotificationPolicy.class)
                .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("id").is(id)))
                .all()
                .then();
    }

    public Mono<Void> deleteLegacyPolicyById(String id) {
        return
                dbClient
                .delete(LegacyPolicy.class)
                .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("id").is(id)))
                .all()
                .then();
    }

    public Mono<Void> deleteQueryPolicyById(String id) {
        return
                dbClient
                .delete(QueryPolicy.class)
                .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("id").is(id)))
                .all()
                .then();
    }

    public Mono<Void> deleteServiceInstancePolicyById(String id) {
        return
                dbClient
                .delete(ServiceInstancePolicy.class)
                .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("id").is(id)))
                .all()
                .then();
    }

    public Mono<Policies> findAll() {
        List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
        List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
        List<EndpointPolicy> endpointPolicies = new ArrayList<>();
        List<QueryPolicy> queryPolicies = new ArrayList<>();
        List<HygienePolicy> hygienePolicies = new ArrayList<>();
        List<ResourceNotificationPolicy> resourceNotificationPolicies = new ArrayList<>();
        List<LegacyPolicy> legacyPolicies = new ArrayList<>();
        return
                Flux
                .from(dbClient.select(ApplicationPolicy.class).all())
                .map(ap -> applicationPolicies.add(ap))
                .thenMany(
                        Flux
                        .from(dbClient.select(ServiceInstancePolicy.class).all())
                        .map(sp -> serviceInstancePolicies.add(sp)))
                .thenMany(
                        Flux
                        .from(dbClient.select(EndpointPolicy.class).all())
                        .map(ep -> endpointPolicies.add(ep)))
                .thenMany(
                        Flux
                        .from(dbClient.select(QueryPolicy.class).all())
                        .map(qp -> queryPolicies.add(qp)))
                .thenMany(
                        Flux
                        .from(dbClient.select(HygienePolicy.class).all())
                        .map(hp -> hygienePolicies.add(hp)))
                .thenMany(
                        Flux
                        .from(dbClient.select(ResourceNotificationPolicy.class).all())
                        .map(rnp -> resourceNotificationPolicies.add(rnp)))
                .thenMany(
                        Flux
                        .from(dbClient.select(LegacyPolicy.class).all())
                        .map(lp -> legacyPolicies.add(lp)))
                .then(Mono.just(Policies.builder().applicationPolicies(applicationPolicies).endpointPolicies(endpointPolicies).serviceInstancePolicies(serviceInstancePolicies).queryPolicies(queryPolicies).hygienePolicies(hygienePolicies).resourceNotificationPolicies(resourceNotificationPolicies).legacyPolicies(legacyPolicies).build()))
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> findAllEndpointPolicies() {
        return
                dbClient
                .select(EndpointPolicy.class)
                .all()
                .collectList()
                .map(eps -> Policies.builder().endpointPolicies(eps).build())
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> findAllHygienePolicies() {
        return
                dbClient
                .select(HygienePolicy.class)
                .all()
                .collectList()
                .map(hps -> Policies.builder().hygienePolicies(hps).build())
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> findAllResourceNotificationPolicies() {
        return
                dbClient
                .select(ResourceNotificationPolicy.class)
                .all()
                .collectList()
                .map(rnps -> Policies.builder().resourceNotificationPolicies(rnps).build())
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> findAllLegacyPolicies() {
        return
                dbClient
                .select(LegacyPolicy.class)
                .all()
                .collectList()
                .map(lps -> Policies.builder().legacyPolicies(lps).build())
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> findAllQueryPolicies() {
        return
                dbClient
                .select(QueryPolicy.class)
                .all()
                .collectList()
                .map(qps -> Policies.builder().queryPolicies(qps).build())
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> findApplicationPolicyById(String id) {
        List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
        return
                Flux
                .from(dbClient
                        .select(ApplicationPolicy.class)
                        .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("id").is(id)))
                        .all())
                .map(ap -> applicationPolicies.add(ap))
                .then(Mono.just(Policies.builder().applicationPolicies(applicationPolicies).build()))
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> findByApplicationOperation(ApplicationOperation operation) {
        List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
        return
                Flux
                .from(dbClient
                        .select(ApplicationPolicy.class)
                        .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("operation").is(operation.getName())))
                        .all())
                .map(ap -> applicationPolicies.add(ap))
                .then(Mono.just(Policies.builder().applicationPolicies(applicationPolicies).build()))
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> findByServiceInstanceOperation(ServiceInstanceOperation operation) {
        List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
        return
                Flux
                .from(dbClient
                        .select(ServiceInstancePolicy.class)
                        .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("operation").is(operation.getName())))
                        .all())
                .map(sp -> serviceInstancePolicies.add(sp))
                .then(Mono.just(Policies.builder().serviceInstancePolicies(serviceInstancePolicies).build()))
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> findEndpointPolicyById(String id) {
        List<EndpointPolicy> endpointPolicies = new ArrayList<>();
        return
                Flux
                .from(dbClient
                        .select(EndpointPolicy.class)
                        .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("id").is(id)))
                        .all())
                .map(ep -> endpointPolicies.add(ep))
                .then(Mono.just(Policies.builder().endpointPolicies(endpointPolicies).build()))
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> findHygienePolicyById(String id) {
        List<HygienePolicy> hygienePolicies = new ArrayList<>();
        return
                Flux
                .from(dbClient
                        .select(HygienePolicy.class)
                        .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("id").is(id)))
                        .all())
                .map(hp -> hygienePolicies.add(hp))
                .then(Mono.just(Policies.builder().hygienePolicies(hygienePolicies).build()))
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> findResourceNotificationPolicyById(String id) {
        List<ResourceNotificationPolicy> resourceNotificationPolicies = new ArrayList<>();
        return
                Flux
                .from(dbClient
                        .select(ResourceNotificationPolicy.class)
                        .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("id").is(id)))
                        .all())
                .map(hp -> resourceNotificationPolicies.add(hp))
                .then(Mono.just(Policies.builder().resourceNotificationPolicies(resourceNotificationPolicies).build()))
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> findLegacyPolicyById(String id) {
        List<LegacyPolicy> legacyPolicies = new ArrayList<>();
        return
                Flux
                .from(dbClient
                        .select(LegacyPolicy.class)
                        .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("id").is(id)))
                        .all())
                .map(lp -> legacyPolicies.add(lp))
                .then(Mono.just(Policies.builder().legacyPolicies(legacyPolicies).build()))
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> findQueryPolicyById(String id) {
        List<QueryPolicy> queryPolicies = new ArrayList<>();
        return
                Flux
                .from(dbClient
                        .select(QueryPolicy.class)
                        .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("id").is(id)))
                        .all())
                .map(qp -> queryPolicies.add(qp))
                .then(Mono.just(Policies.builder().queryPolicies(queryPolicies).build()))
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> findServiceInstancePolicyById(String id) {
        List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
        return
                Flux
                .from(dbClient
                        .select(ServiceInstancePolicy.class)
                        .matching(org.springframework.data.relational.core.query.Query.query(Criteria.where("id").is(id)))
                        .all())
                .map(sp -> serviceInstancePolicies.add(sp))
                .then(Mono.just(Policies.builder().serviceInstancePolicies(serviceInstancePolicies).build()))
                .flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
    }

    public Mono<Policies> save(Policies entity) {
        List<ApplicationPolicy> applicationPolicies =
                entity.getApplicationPolicies().stream()
                .map(p -> idProvider.seedApplicationPolicy(p)).collect(Collectors.toList());

        List<ServiceInstancePolicy> serviceInstancePolicies =
                entity.getServiceInstancePolicies().stream()
                .map(p -> idProvider.seedServiceInstancePolicy(p)).collect(Collectors.toList());

        List<EndpointPolicy> endpointPolicies =
                entity.getEndpointPolicies().stream()
                .map(p -> idProvider.seedEndpointPolicy(p)).collect(Collectors.toList());

        List<QueryPolicy> queryPolicies =
                entity.getQueryPolicies().stream()
                .map(p -> idProvider.seedQueryPolicy(p)).collect(Collectors.toList());

        List<HygienePolicy> hygienePolicies =
                entity.getHygienePolicies().stream()
                .map(p -> idProvider.seedHygienePolicy(p)).collect(Collectors.toList());

        List<ResourceNotificationPolicy> resourceNotificationPolicies =
                entity.getResourceNotificationPolicies().stream()
                .map(p -> idProvider.seedResourceNotificationPolicy(p)).collect(Collectors.toList());

        List<LegacyPolicy> legacyPolicies =
                entity.getLegacyPolicies().stream()
                .map(p -> idProvider.seedLegacyPolicy(p)).collect(Collectors.toList());

        return Flux.fromIterable(applicationPolicies)
                .concatMap(this::saveApplicationPolicy)
                .thenMany(Flux.fromIterable(serviceInstancePolicies)
                        .concatMap(this::saveServiceInstancePolicy))
                .thenMany(Flux.fromIterable(endpointPolicies)
                        .concatMap(this::saveEndpointPolicy))
                .thenMany(Flux.fromIterable(queryPolicies)
                        .concatMap(this::saveQueryPolicy))
                .thenMany(Flux.fromIterable(hygienePolicies)
                        .concatMap(this::saveHygienePolicy))
                .thenMany(Flux.fromIterable(resourceNotificationPolicies)
                        .concatMap(this::saveResourceNotificationPolicy))
                .thenMany(Flux.fromIterable(legacyPolicies)
                        .concatMap(this::saveLegacyPolicy))
                .then(
                        Mono.just(
                                Policies
                                .builder()
                                .applicationPolicies(applicationPolicies)
                                .serviceInstancePolicies(serviceInstancePolicies)
                                .endpointPolicies(endpointPolicies)
                                .queryPolicies(queryPolicies)
                                .hygienePolicies(hygienePolicies)
                                .resourceNotificationPolicies(resourceNotificationPolicies)
                                .legacyPolicies(legacyPolicies)
                                .build()
                                )
                        );
    }

    private Mono<ApplicationPolicy> saveApplicationPolicy(ApplicationPolicy ap) {
        return
                dbClient
                .insert(ap);
    }

    private Mono<EndpointPolicy> saveEndpointPolicy(EndpointPolicy ep) {
        return
                dbClient
                .insert(ep);
    }

    private Mono<HygienePolicy> saveHygienePolicy(HygienePolicy hp) {
        return
                dbClient
                .insert(hp);
    }

    private Mono<ResourceNotificationPolicy> saveResourceNotificationPolicy(ResourceNotificationPolicy hp) {
        return
                dbClient
                .insert(hp);
    }

    private Mono<LegacyPolicy> saveLegacyPolicy(LegacyPolicy lp) {
        return
                dbClient
                .insert(lp);
    }

    private Mono<QueryPolicy> saveQueryPolicy(QueryPolicy qp) {
        return
                dbClient
                .insert(qp);
    }

    private Mono<ServiceInstancePolicy> saveServiceInstancePolicy(ServiceInstancePolicy sip) {
        return
                dbClient
                .insert(sip);
    }
}
