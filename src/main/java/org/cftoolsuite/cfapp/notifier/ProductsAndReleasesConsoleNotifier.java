package org.cftoolsuite.cfapp.notifier;

import org.cftoolsuite.cfapp.event.ProductsAndReleasesRetrievedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProductsAndReleasesConsoleNotifier implements ApplicationListener<ProductsAndReleasesRetrievedEvent> {

    private final ObjectMapper mapper;

    @Autowired
    public ProductsAndReleasesConsoleNotifier(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onApplicationEvent(ProductsAndReleasesRetrievedEvent event) {
        try {
            log.trace(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event.getProducts()));
            log.trace(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event.getAllReleases()));
            log.trace(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event.getLatestReleases()));
        } catch (JsonProcessingException jpe) {
            log.error("Could not list products from Pivotal Network.", jpe);
        }
    }


}
