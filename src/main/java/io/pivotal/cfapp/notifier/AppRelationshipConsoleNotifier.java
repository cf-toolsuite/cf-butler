package io.pivotal.cfapp.notifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.report.AppRelationshipCsvReport;
import io.pivotal.cfapp.task.AppRelationshipRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AppRelationshipConsoleNotifier implements ApplicationListener<AppRelationshipRetrievedEvent> {

	private final AppRelationshipCsvReport report;

    @Autowired
    public AppRelationshipConsoleNotifier(ButlerSettings appSettings) {
        this.report = new AppRelationshipCsvReport(appSettings);
    }

	@Override
	public void onApplicationEvent(AppRelationshipRetrievedEvent event) {
		log.info(String.join("\n\n", report.generatePreamble(), report.generateDetail(event)));
	}

}