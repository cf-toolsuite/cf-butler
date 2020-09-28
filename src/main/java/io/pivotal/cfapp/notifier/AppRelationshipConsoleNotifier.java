package io.pivotal.cfapp.notifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.report.AppRelationshipCsvReport;
import io.pivotal.cfapp.service.TimeKeeperService;
import io.pivotal.cfapp.service.TkServiceUtil;
import io.pivotal.cfapp.event.AppRelationshipRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AppRelationshipConsoleNotifier implements ApplicationListener<AppRelationshipRetrievedEvent> {

	private final AppRelationshipCsvReport report;
	private final TkServiceUtil util;

    @Autowired
    public AppRelationshipConsoleNotifier(
		PasSettings appSettings,
		TimeKeeperService tkService) {
		this.report = new AppRelationshipCsvReport(appSettings);
		this.util = new TkServiceUtil(tkService);
    }

	@Override
	public void onApplicationEvent(AppRelationshipRetrievedEvent event) {
		util
			.getTimeCollected()
          	.subscribe(tc -> log.trace(String.join("%n%n", report.generatePreamble(tc), report.generateDetail(event))));
	}

}