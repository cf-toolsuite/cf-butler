package io.pivotal.cfapp.notifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.report.ServiceInstanceDetailCsvReport;
import io.pivotal.cfapp.task.ServiceInstanceDetailRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServiceInstanceDetailConsoleNotifier implements ApplicationListener<ServiceInstanceDetailRetrievedEvent> {

	private final ServiceInstanceDetailCsvReport report;

    @Autowired
    public ServiceInstanceDetailConsoleNotifier(ButlerSettings appSettings) {
        this.report = new ServiceInstanceDetailCsvReport(appSettings);
    }

	@Override
	public void onApplicationEvent(ServiceInstanceDetailRetrievedEvent event) {
		log.info(String.join("\n\n", report.generatePreamble(), report.generateDetail(event)));
	}

}