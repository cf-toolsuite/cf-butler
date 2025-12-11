package org.cftoolsuite.cfapp.service;

import java.io.File;
import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.cftoolsuite.cfapp.ButlerTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;


@ButlerTest
@ExtendWith(SpringExtension.class)
public class ApplicationReporterTest {

    private final ApplicationReporter reporter;
    private final ObjectMapper mapper;

    @Autowired
    public ApplicationReporterTest(
            ApplicationReporter reporter,
            ObjectMapper mapper
            ) {
        this.reporter = reporter;
        this.mapper = mapper;
    }

    @Test
    public void testReportGeneration() throws StreamReadException, DatabindException, IOException {
        File file = new File(System.getProperty("user.home") + "/app-reporting-config.json");
        if (file.exists()) {
            ReportRequestSpec spec = mapper.readValue(file, ReportRequestSpec.class);
            reporter.createReport(spec.getOutput(), spec.getInput());
            Assertions.assertThat(new File(spec.getOutput()).exists()).isTrue();
        }
    }

}
