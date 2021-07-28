package io.pivotal.cfapp.task;

import io.pivotal.cfapp.event.AppDetailReadyToBeRetrievedEvent;
import io.pivotal.cfapp.event.ProductsAndReleasesRetrievedEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ProductsAndReleasesRetrievedListener implements ApplicationListener<ProductsAndReleasesRetrievedEvent> {

    private final ApplicationEventPublisher publisher;
    private final AppDetailReadyToBeCollectedDecider appDetailReadyToBeCollectedDecider;

    @Autowired
    public ProductsAndReleasesRetrievedListener(
            ApplicationEventPublisher publisher,
            AppDetailReadyToBeCollectedDecider appDetailReadyToBeCollectedDecider) {
        this.publisher = publisher;
        this.appDetailReadyToBeCollectedDecider = appDetailReadyToBeCollectedDecider;
    }

    @Override
    public void onApplicationEvent(ProductsAndReleasesRetrievedEvent event) {
        appDetailReadyToBeCollectedDecider.informDecision();
        publisher.publishEvent(new AppDetailReadyToBeRetrievedEvent(this));
    }

}
