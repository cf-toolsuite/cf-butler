package org.cftoolsuite.cfapp.controller;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.cftoolsuite.cfapp.service.TkServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class TimeKeeperController {

    private final TkServiceUtil util;

    @Autowired
    public TimeKeeperController(
            TimeKeeperService tkService
            ) {
        this.util = new TkServiceUtil(tkService);
    }

    @GetMapping(value = { "/collect" },
            produces = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public Mono<ResponseEntity<?>> getCollectionTime(
            @RequestHeader(value = "Accept", defaultValue = MediaType.TEXT_PLAIN_VALUE) String acceptHeader) {

        return util.getTimeCollected()
                .map(timestamp -> {
                    if (acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE)) {
                        // Return JSON response
                        Map<String, String> response = new HashMap<>();
                        response.put("timestamp", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(timestamp));
                        return ResponseEntity.ok(response);
                    } else {
                        // Return plain text response (default)
                        return ResponseEntity.ok(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(timestamp));
                    }
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}