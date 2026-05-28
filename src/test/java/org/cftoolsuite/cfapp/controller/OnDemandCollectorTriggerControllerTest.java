package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.cftoolsuite.cfapp.task.ProductsAndReleasesTask;
import org.cftoolsuite.cfapp.task.TkTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;

class OnDemandCollectorTriggerControllerTest extends ControllerTestBase {

    private TkTask tkCollector;
    private ProductsAndReleasesTask productsAndReleasesCollector;
    private OnDemandCollectorTriggerController controller;

    @BeforeEach
    void setUp() throws Exception {
        initMocks();
        tkCollector = mock(TkTask.class);
        productsAndReleasesCollector = mock(ProductsAndReleasesTask.class);
        controller = new OnDemandCollectorTriggerController();
        setField(controller, "tkCollector", tkCollector);
        setField(controller, "productsAndReleasesCollector", productsAndReleasesCollector);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void triggerCollection_whenAllCollectorsAvailable_returnsAccepted() throws Exception {
        assertAccepted(controller.triggerCollection());
    }

    @Test
    void triggerCollection_whenProductsAndReleasesCollectorNull_returnsAccepted() throws Exception {
        controller = new OnDemandCollectorTriggerController();
        setField(controller, "tkCollector", tkCollector);
        setField(controller, "productsAndReleasesCollector", null);

        assertAccepted(controller.triggerCollection());
    }
}
