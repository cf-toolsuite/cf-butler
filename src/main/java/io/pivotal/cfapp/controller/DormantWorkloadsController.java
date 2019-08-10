package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.DormantWorkloads;
import io.pivotal.cfapp.domain.DormantWorkloads.DormantWorkloadsBuilder;
import io.pivotal.cfapp.service.DormantWorkloadsService;
import io.pivotal.cfapp.service.TkService;
import io.pivotal.cfapp.service.TkServiceUtil;
import reactor.core.publisher.Mono;

@RestController
public class DormantWorkloadsController {

    private final DormantWorkloadsService service;
    private final TkServiceUtil util;

    @Autowired
    public DormantWorkloadsController(
        DormantWorkloadsService service,
        TkService tkService) {
        this.service = service;
        this.util = new TkServiceUtil(tkService);
    }

    @GetMapping(value = { "/snapshot/detail/dormant/{daysSinceLastUpdate}" } )
	public Mono<ResponseEntity<DormantWorkloads>> getDormantWorkloads(@PathVariable("daysSinceLastUpdate") Integer daysSinceLastUpdate) {
        final DormantWorkloadsBuilder builder = DormantWorkloads.builder();
        return service
            .getDormantApplications(daysSinceLastUpdate)
            .map(list -> builder.applications(list))
            .then(service.getDormantServiceInstances(daysSinceLastUpdate))
            .map(list -> builder.serviceInstances(list))
            .flatMap(dwb -> util
                            .getHeaders()
                                .map(h -> new ResponseEntity<DormantWorkloads>(dwb.build(), h, HttpStatus.OK)))
			.defaultIfEmpty(ResponseEntity.notFound().build());
	}

}