package io.pivotal.cfapp.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.PivnetSettings;
import io.pivotal.cfapp.domain.Space;

@Component
public class AppDetailReadyToBeCollectedDecider {

    private PivnetSettings settings;
    private AtomicInteger decision = new AtomicInteger();
    private List<Space> spaces = new ArrayList<>();

    @Autowired
    public AppDetailReadyToBeCollectedDecider(PivnetSettings settings) {
        this.settings = settings;
    }

    public List<Space> getSpaces() {
        return List.copyOf(spaces);
    }

    public int informDecision() {
        return decision.incrementAndGet();
    }

    public boolean isDecided() {
        return settings.isEnabled() ? decision.get() == 2: decision.get() == 1;
    }

    public void reset() {
        spaces.clear();
        decision.set(0);
    }

    public void setSpaces(List<Space> spaces) {
        this.spaces = spaces;
    }

}
