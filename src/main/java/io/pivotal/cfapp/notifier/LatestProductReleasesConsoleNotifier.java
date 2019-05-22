package io.pivotal.cfapp.notifier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.task.LatestProductReleasesRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LatestProductReleasesConsoleNotifier implements ApplicationListener<LatestProductReleasesRetrievedEvent> {

	private final ObjectMapper mapper;

    @Autowired
    public LatestProductReleasesConsoleNotifier(ObjectMapper mapper) {
        this.mapper = mapper;
    }

	@Override
	public void onApplicationEvent(LatestProductReleasesRetrievedEvent event) {
		try {
			log.trace(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event.getLatestReleases()));
		} catch (JsonProcessingException jpe) {
			log.error("Could not list latest product releases from Pivotal Network.", jpe);
		}
	}


}