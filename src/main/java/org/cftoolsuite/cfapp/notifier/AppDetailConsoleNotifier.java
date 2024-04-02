package org.cftoolsuite.cfapp.notifier;

import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.event.AppDetailRetrievedEvent;
import org.cftoolsuite.cfapp.report.AppDetailCsvReport;
import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.cftoolsuite.cfapp.service.TkServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AppDetailConsoleNotifier implements ApplicationListener<AppDetailRetrievedEvent> {

    private final AppDetailCsvReport report;
    private final TkServiceUtil util;

    @Autowired
    public AppDetailConsoleNotifier(
            PasSettings appSettings,
            TimeKeeperService tkService) {
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
