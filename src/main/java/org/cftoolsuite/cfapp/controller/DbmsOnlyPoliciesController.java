package org.cftoolsuite.cfapp.controller;

import org.cftoolsuite.cfapp.domain.Policies;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.util.DbmsOnlyCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@Conditional(DbmsOnlyCondition.class)
public class DbmsOnlyPoliciesController {

    private final PoliciesService policiesService;

    @Autowired
    public DbmsOnlyPoliciesController(
            PoliciesService policiesService) {
        this.policiesService = policiesService;
    }

    @DeleteMapping("/policies")
    public Mono<ResponseEntity<Void>> deleteAllPolicies() {
        return policiesService.deleteAll()
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/policies/application/{id}")
    public Mono<ResponseEntity<Void>> deleteApplicationPolicy(@PathVariable String id) {
        return policiesService.deleteApplicationPolicyById(id)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/policies/endpoint/{id}")
    public Mono<ResponseEntity<Void>> deleteEndpointPolicy(@PathVariable String id) {
        return policiesService.deleteEndpointPolicyById(id)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/policies/hygiene/{id}")
    public Mono<ResponseEntity<Void>> deleteHygienePolicy(@PathVariable String id) {
        return policiesService.deleteHygienePolicyById(id)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/policies/resourcenotification/{id}")
    public Mono<ResponseEntity<Void>> deleteResourceNotificationPolicy(@PathVariable String id) {
        return policiesService.deleteResourceNotificationPolicyById(id)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/policies/legacy/{id}")
    public Mono<ResponseEntity<Void>> deleteLegacyPolicy(@PathVariable String id) {
        return policiesService.deleteLegacyPolicyById(id)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/policies/query/{id}")
    public Mono<ResponseEntity<Void>> deleteQueryPolicy(@PathVariable String id) {
        return policiesService.deleteQueryPolicyById(id)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/policies/serviceInstance/{id}")
    public Mono<ResponseEntity<Void>> deleteServiceInstancePolicy(@PathVariable String id) {
        return policiesService.deleteServiceInstancePolicyById(id)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/policies")
    public Mono<ResponseEntity<Policies>> establishPolicies(@RequestBody Policies policies) {
        return policiesService.save(policies)
                .map(ResponseEntity::ok);
    }
}
