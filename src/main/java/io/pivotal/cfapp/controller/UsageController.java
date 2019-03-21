package io.pivotal.cfapp.controller;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/usage/tasks/{orgGuid}")
    public Mono<String> getTaskUsage(@PathVariable("orgGuid") String orgGuid, @DateTimeFormat(iso = ISO.DATE) @RequestParam("start") LocalDate start, @DateTimeFormat(iso = ISO.DATE) @RequestParam("end") LocalDate end) {
        return service.getTaskUsage(orgGuid, start, end);
    }

    @GetMapping("/usage/applications/{orgGuid}")
    public Mono<String> getApplicationUsage(@PathVariable("orgGuid") String orgGuid, @DateTimeFormat(iso = ISO.DATE) @RequestParam("start") LocalDate start, @DateTimeFormat(iso = ISO.DATE) @RequestParam("end") LocalDate end) {
        return service.getApplicationUsage(orgGuid, start, end);
    }

    @GetMapping("/usage/services/{orgGuid}")
    public Mono<String> getServiceUsage(@PathVariable("orgGuid") String orgGuid, @DateTimeFormat(iso = ISO.DATE) @RequestParam("start") LocalDate start, @DateTimeFormat(iso = ISO.DATE) @RequestParam("end") LocalDate end) {
        return service.getServiceUsage(orgGuid, start, end);
    }

    @GetMapping("/usage/tasks")
    public Flux<String> getTaskUsage(@DateTimeFormat(iso = ISO.DATE) @RequestParam("start") LocalDate start, @DateTimeFormat(iso = ISO.DATE) @RequestParam("end") LocalDate end) {
        return service.getTaskUsage(start, end);
    }

    @GetMapping("/usage/applications")
    public Flux<String> getApplicationUsage(@DateTimeFormat(iso = ISO.DATE) @RequestParam("start") LocalDate start, @DateTimeFormat(iso = ISO.DATE) @RequestParam("end") LocalDate end) {
        return service.getApplicationUsage(start, end);
    }

    @GetMapping("/usage/services")
    public Flux<String> getServiceUsage(@DateTimeFormat(iso = ISO.DATE) @RequestParam("start") LocalDate start, @DateTimeFormat(iso = ISO.DATE) @RequestParam("end") LocalDate end) {
        return service.getServiceUsage(start, end);
    }

    // TODO How about adding convenience methods for a month-over-month and year's worth of usage data ty once?
}