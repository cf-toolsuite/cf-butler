package io.pivotal.cfapp.notifier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.task.ProductsRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProductsConsoleNotifier implements ApplicationListener<ProductsRetrievedEvent> {

	private final ObjectMapper mapper;

    @Autowired
    public ProductsConsoleNotifier(ObjectMapper mapper) {
        this.mapper = mapper;
    }

	@Override
	public void onApplicationEvent(ProductsRetrievedEvent event) {
		try {
			log.trace(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event.getProducts()));
		} catch (JsonProcessingException jpe) {
			log.error("Could not list products from Pivotal Network.", jpe);
		}
	}


}