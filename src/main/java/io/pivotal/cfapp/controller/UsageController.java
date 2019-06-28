package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.accounting.application.AppUsageReport;
import io.pivotal.cfapp.domain.accounting.service.ServiceUsageReport;
import io.pivotal.cfapp.domain.accounting.task.TaskUsageReport;
import io.pivotal.cfapp.service.UsageCache;
import reactor.core.publisher.Mono;

@RestController
public class UsageController {

    private final UsageCache cache;

    @Autowired
    public UsageController(UsageCache cache) {
        this.cache = cache;
    }

    @GetMapping(value = "/accounting/tasks")
    public Mono<ResponseEntity<TaskUsageReport>> getTaskReport() {
        return Mono.justOrEmpty(cache.getTaskReport())
                .map(r -> ResponseEntity.ok(r))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/accounting/applications")
    public Mono<ResponseEntity<AppUsageReport>> getApplicationReport() {
        return Mono.justOrEmpty(cache.getApplicationReport())
                .map(r -> ResponseEntity.ok(r))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/accounting/caches")
    public Mono<ResponseEntity<ServiceUsageReport>> getcacheReport() {
        return Mono.justOrEmpty(cache.getServiceReport())
                .map(r -> ResponseEntity.ok(r))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}