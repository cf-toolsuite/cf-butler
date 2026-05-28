package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.cftoolsuite.cfapp.service.JavaAppDetailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class JavaAppDetailControllerTest extends ControllerTestBase {

    private JavaAppDetailService service;
    private JavaAppDetailController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        service = mock(JavaAppDetailService.class);
        controller = new JavaAppDetailController(service);
    }

    @Test
    void getSpringApplications_delegatesToService() {
        Map<String, String> springApps = new HashMap<>();
        springApps.put("app1", "spring-boot-3.0");
        when(service.findSpringApplications()).thenReturn(Flux.just(springApps));

        ResponseEntity<Flux<Map<String, String>>> result = controller.getSpringApplications();

        assertNotNull(result);
        assertNotNull(result.getBody());

        StepVerifier.create(result.getBody())
                .assertNext(map -> {
                    assertEquals(1, map.size());
                    assertEquals("spring-boot-3.0", map.get("app1"));
                })
                .verifyComplete();

        verify(service).findSpringApplications();
    }

    @Test
    void getSpringApplications_whenEmpty() {
        when(service.findSpringApplications()).thenReturn(Flux.empty());

        ResponseEntity<Flux<Map<String, String>>> result = controller.getSpringApplications();

        assertNotNull(result);
        assertNotNull(result.getBody());

        StepVerifier.create(result.getBody())
                .verifyComplete();

        verify(service).findSpringApplications();
    }

    @Test
    void calculateSpringDependencyFrequency_delegatesToService() {
        Map<String, Integer> freq = new HashMap<>();
        freq.put("spring-web", 5);
        when(service.calculateSpringDependencyFrequency()).thenReturn(Mono.just(freq));

        ResponseEntity<Mono<Map<String, Integer>>> result = controller.calculateSpringDependencyFrequency();

        assertNotNull(result);
        assertNotNull(result.getBody());

        StepVerifier.create(result.getBody())
                .assertNext(map -> {
                    assertEquals(1, map.size());
                    assertEquals(Integer.valueOf(5), map.get("spring-web"));
                })
                .verifyComplete();

        verify(service).calculateSpringDependencyFrequency();
    }

    @Test
    void calculateSpringDependencyFrequency_whenEmpty() {
        when(service.calculateSpringDependencyFrequency()).thenReturn(Mono.empty());

        ResponseEntity<Mono<Map<String, Integer>>> result = controller.calculateSpringDependencyFrequency();

        assertNotNull(result);
        assertNotNull(result.getBody());

        StepVerifier.create(result.getBody())
                .verifyComplete();

        verify(service).calculateSpringDependencyFrequency();
    }
}
