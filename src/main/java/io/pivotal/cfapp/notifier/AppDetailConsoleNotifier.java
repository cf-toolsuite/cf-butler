package io.pivotal.cfapp.notifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.report.AppDetailCsvReport;
import io.pivotal.cfapp.task.AppDetailRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AppDetailConsoleNotifier implements ApplicationListener<AppDetailRetrievedEvent> {

	private final AppDetailCsvReport report;

    @Autowired
    public AppDetailConsoleNotifier(ButlerSettings appSettings) {
        this.report = new AppDetailCsvReport(appSettings);
    }

	@Override
	public void onApplicationEvent(AppDetailRetrievedEvent event) {
		log.info(String.join("\n\n", report.generatePreamble(), report.generateDetail(event)));
	}


}