package org.cftoolsuite.cfapp.task;

import org.cftoolsuite.cfapp.event.AppDetailReadyToBeRetrievedEvent;
import org.cftoolsuite.cfapp.event.SpacesRetrievedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class SpacesRetrievedListener implements ApplicationListener<SpacesRetrievedEvent> {

    private final ApplicationEventPublisher publisher;
    private final AppDetailReadyToBeCollectedDecider appDetailReadyToBeCollectedDecider;

    @Autowired
    public SpacesRetrievedListener(
            ApplicationEventPublisher publisher,
            AppDetailReadyToBeCollectedDecider appDetailReadyToBeCollectedDecider) {
        this.publisher = publisher;
        this.appDetailReadyToBeCollectedDecider = appDetailReadyToBeCollectedDecider;
    }

    @Override
    public void onApplicationEvent(SpacesRetrievedEvent event) {
        appDetailReadyToBeCollectedDecider.informDecision();
        appDetailReadyToBeCollectedDecider.setSpaces(event.getSpaces());
        publisher.publishEvent(new AppDetailReadyToBeRetrievedEvent(this));
    }
}
