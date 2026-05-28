package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.cftoolsuite.cfapp.domain.JavaAppDetail;
import org.cftoolsuite.cfapp.service.JavaAppDetailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class PomFileExportControllerTest extends ControllerTestBase {

    private JavaAppDetailService service;
    private PomFileExportController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        service = mock(JavaAppDetailService.class);
        controller = new PomFileExportController(service);
    }

    @Test
    void downloadDependenciesTarball_whenDataAvailable_returnsOk() {
        JavaAppDetail detail = JavaAppDetail.builder()
                .organization("myorg")
                .space("myspace")
                .appName("myapp")
                .pomContents("<project></project>")
                .build();

        when(service.findAll()).thenReturn(Flux.just(detail));

        ResponseEntity<Flux<DataBuffer>> result = controller.downloadDependenciesTarball();

        assertNotNull(result);
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, result.getHeaders().getContentType());
        assertNotNull(result.getHeaders().getFirst("Content-Disposition"));
        assertTrue(result.getHeaders().getFirst("Content-Disposition").contains("attachment"));
        assertTrue(result.getHeaders().getFirst("Content-Disposition").contains(".tar.gz"));
        assertNotNull(result.getBody());
    }

    @Test
    void downloadDependenciesTarball_whenEmpty_returnsOk() {
        when(service.findAll()).thenReturn(Flux.empty());

        ResponseEntity<Flux<DataBuffer>> result = controller.downloadDependenciesTarball();

        assertNotNull(result);
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, result.getHeaders().getContentType());
        assertNotNull(result.getBody());

        StepVerifier.create(result.getBody())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void downloadDependenciesTarball_whenMultipleApps_returnsOk() {
        JavaAppDetail detail1 = JavaAppDetail.builder()
                .organization("org1")
                .space("space1")
                .appName("app1")
                .pomContents("<project>app1</project>")
                .build();
        JavaAppDetail detail2 = JavaAppDetail.builder()
                .organization("org2")
                .space("space2")
                .appName("app2")
                .pomContents("<project>app2</project>")
                .build();

        when(service.findAll()).thenReturn(Flux.just(detail1, detail2));

        ResponseEntity<Flux<DataBuffer>> result = controller.downloadDependenciesTarball();

        assertNotNull(result);
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, result.getHeaders().getContentType());
        assertNotNull(result.getBody());
    }

    @Test
    void downloadDependenciesTarball_whenNoPomContents_returnsOk() {
        JavaAppDetail detail = JavaAppDetail.builder()
                .organization("myorg")
                .space("myspace")
                .appName("myapp")
                .build();

        when(service.findAll()).thenReturn(Flux.just(detail));

        ResponseEntity<Flux<DataBuffer>> result = controller.downloadDependenciesTarball();

        assertNotNull(result);
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, result.getHeaders().getContentType());
        assertNotNull(result.getBody());
    }
}
