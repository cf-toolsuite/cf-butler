package io.pivotal.cfapp.notifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.report.ServiceInstanceInfoCsvReport;
import io.pivotal.cfapp.task.ServiceInfoRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServiceInstanceInfoConsoleNotifier implements ApplicationListener<ServiceInfoRetrievedEvent> {

	private final ServiceInstanceInfoCsvReport report;
	
    @Autowired
    public ServiceInstanceInfoConsoleNotifier(ButlerSettings appSettings) {
        this.report = new ServiceInstanceInfoCsvReport(appSettings);
    }

	@Override
	public void onApplicationEvent(ServiceInfoRetrievedEvent event) {
		log.info(String.join("\n\n", report.generatePreamble(), report.generateDetail(event)));
	}
  
}