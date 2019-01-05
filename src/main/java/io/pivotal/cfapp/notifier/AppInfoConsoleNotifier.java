package io.pivotal.cfapp.notifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.report.AppInfoCsvReport;
import io.pivotal.cfapp.task.AppInfoRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AppInfoConsoleNotifier implements ApplicationListener<AppInfoRetrievedEvent> {

	private final AppInfoCsvReport report;
	
    @Autowired
    public AppInfoConsoleNotifier(ButlerSettings appSettings) {
        this.report = new AppInfoCsvReport(appSettings);
    }

	@Override
	public void onApplicationEvent(AppInfoRetrievedEvent event) {
		log.info(String.join("\n\n", report.generatePreamble(), report.generateDetail(event)));
	}

    
}