package io.pivotal.cfapp.notifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.report.AppDetailCsvReport;
import io.pivotal.cfapp.service.TkService;
import io.pivotal.cfapp.service.TkServiceUtil;
import io.pivotal.cfapp.event.AppDetailRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AppDetailConsoleNotifier implements ApplicationListener<AppDetailRetrievedEvent> {

	private final AppDetailCsvReport report;
	private final TkServiceUtil util;

    @Autowired
    public AppDetailConsoleNotifier(
		    PasSettings appSettings,
		    TkService tkService) {
		    this.report = new AppDetailCsvReport(appSettings);
		    this.util = new TkServiceUtil(tkService);
    }

	@Override
	public void onApplicationEvent(AppDetailRetrievedEvent event) {
			util
			    .getTimeCollected()
          		.subscribe(tc -> log.trace(String.join("%n%n", report.generatePreamble(tc), report.generateDetail(event))));

	}

}