package io.pivotal.cfapp.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.accounting.application.AppUsageReport;
import io.pivotal.cfapp.domain.accounting.service.ServiceUsageReport;
import io.pivotal.cfapp.domain.accounting.task.TaskUsageReport;
import io.pivotal.cfapp.service.UsageService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class UsageController {

    private final UsageService service;

    @Autowired
    public UsageController(UsageService service) {
        this.service = service;
    }

    @GetMapping(value = "accounting/tasks")
    public Mono<TaskUsageReport> getTaskReport() {
        return service.getTaskReport();
    }

    @GetMapping(value = "accounting/applications")
    public Mono<AppUsageReport> getApplicationReport() {
        return service.getApplicationReport();
    }

    @GetMapping(value = "accounting/services")
    public Mono<ServiceUsageReport> getServiceReport() {
        return service.getServiceReport();
    }

    //----------
    // FIXME Refactor Mono<String> JSON-like output to domain objects so we can start to drive aggregate calculations
    // TODO How about adding convenience methods for a month-over-month and year's worth of usage data ty once?

    @GetMapping(value = "/usage/tasks/{orgGuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getTaskUsage(@PathVariable("orgGuid") String orgGuid, @DateTimeFormat(iso = ISO.DATE) @RequestParam("start") LocalDate start, @DateTimeFormat(iso = ISO.DATE) @RequestParam("end") LocalDate end) {
        return service.getTaskUsage(orgGuid, start, end);
    }

    @GetMapping(value = "/usage/applications/{orgGuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getApplicationUsage(@PathVariable("orgGuid") String orgGuid, @DateTimeFormat(iso = ISO.DATE) @RequestParam("start") LocalDate start, @DateTimeFormat(iso = ISO.DATE) @RequestParam("end") LocalDate end) {
        return service.getApplicationUsage(orgGuid, start, end);
    }

    @GetMapping(value = "/usage/services/{orgGuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getServiceUsage(@PathVariable("orgGuid") String orgGuid, @DateTimeFormat(iso = ISO.DATE) @RequestParam("start") LocalDate start, @DateTimeFormat(iso = ISO.DATE) @RequestParam("end") LocalDate end) {
        return service.getServiceUsage(orgGuid, start, end);
    }

    @GetMapping(value = "/usage/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<String> getTaskUsage(@DateTimeFormat(iso = ISO.DATE) @RequestParam("start") LocalDate start, @DateTimeFormat(iso = ISO.DATE) @RequestParam("end") LocalDate end) {
        return service.getTaskUsage(start, end);
    }

    @GetMapping(value = "/usage/applications", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<String> getApplicationUsage(@DateTimeFormat(iso = ISO.DATE) @RequestParam("start") LocalDate start, @DateTimeFormat(iso = ISO.DATE) @RequestParam("end") LocalDate end) {
        return service.getApplicationUsage(start, end);
    }

    @GetMapping(value = "/usage/services", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<String> getServiceUsage(@DateTimeFormat(iso = ISO.DATE) @RequestParam("start") LocalDate start, @DateTimeFormat(iso = ISO.DATE) @RequestParam("end") LocalDate end) {
        return service.getServiceUsage(start, end);
    }

}