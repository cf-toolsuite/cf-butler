package io.pivotal.cfapp.notifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.report.ServiceInstanceDetailCsvReport;
import io.pivotal.cfapp.service.TkService;
import io.pivotal.cfapp.service.TkServiceUtil;
import io.pivotal.cfapp.task.ServiceInstanceDetailRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServiceInstanceDetailConsoleNotifier implements ApplicationListener<ServiceInstanceDetailRetrievedEvent> {

	private final ServiceInstanceDetailCsvReport report;
	private final TkServiceUtil util;

    @Autowired
    public ServiceInstanceDetailConsoleNotifier(
		ButlerSettings appSettings,
		TkService tkService) {
		this.report = new ServiceInstanceDetailCsvReport(appSettings);
		this.util = new TkServiceUtil(tkService);
    }

	@Override
	public void onApplicationEvent(ServiceInstanceDetailRetrievedEvent event) {
		util
			.getTimeCollected()
        	.subscribe(tc -> log.trace(String.join("%n%n", report.generatePreamble(tc), report.generateDetail(event))));
	}

}