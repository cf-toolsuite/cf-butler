package org.cftoolsuite.cfapp.notifier;

import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.event.AppRelationshipRetrievedEvent;
import org.cftoolsuite.cfapp.report.AppRelationshipCsvReport;
import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.cftoolsuite.cfapp.service.TkServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

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
