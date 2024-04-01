package org.cftoolsuite.cfapp.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.cftoolsuite.cfapp.config.PivnetSettings;
import org.cftoolsuite.cfapp.domain.Space;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
