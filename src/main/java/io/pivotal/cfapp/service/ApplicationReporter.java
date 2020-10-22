package io.pivotal.cfapp.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;

import io.pivotal.cfapp.domain.accounting.application.AppUsageMonthly;
import io.pivotal.cfapp.domain.accounting.application.AppUsageMonthly.AppUsageMonthlyBuilder;
import io.pivotal.cfapp.domain.accounting.application.AppUsageReport;
import io.pivotal.cfapp.domain.accounting.application.AppUsageReport.AppUsageReportBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ApplicationReporter {

    private static final String REPORT_HEADER = "foundation,time_period,maximum_instances,average_instances,instance_hours\n";
    private final ObjectMapper mapper;

    @Autowired
    public ApplicationReporter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String createReport(ReportRequest[] requests) {
        List<ReportRequest> list = Arrays.asList(requests);
        try { log.info(mapper.writeValueAsString(list)); } catch (JsonProcessingException jpe) {}
        StringBuilder result = new StringBuilder();
        result.append(REPORT_HEADER);
        for (ReportRequest r: list) {
            result.append(createReport(r.getFoundation(), r.getEnvironment(), r.getPeriod(), r.getFilename()));
        }
        return result.toString();
    }

    public void createReport(String outputFilename, ReportRequest[] requests) {
        Path path = Paths.get(outputFilename);
        try {
            Files.write(path, createReport(requests).getBytes());
        } catch (IOException ioe) {
            log.warn("Could not create report output file!", ioe);
        }
    }

    public String createReport(String foundation, String environment, String period, String filename) {
        Assert.hasText(foundation, "Foundation must be specified!");
        Assert.hasText(environment, "Environment must be specified!");
        Assert.hasText(period, "Time period must be specified! It should have the form YYYY-MM");
        Assert.hasText(filename, "Filename must be specified!");
        StringBuilder result = new StringBuilder();
        Integer year = Integer.valueOf(period.split("-")[0]);
        Integer month = Integer.valueOf(period.split("-")[1]);
        try {
            AppUsageReport report = readAppUsageReport(filename);
            List<AppUsageMonthly> usage = report.getMonthlyReports();
            List<AppUsageMonthly> filtered =
                    usage
                    .stream()
                    .filter(u -> u.getYear().equals(year) && u.getMonth() != null && u.getMonth().equals(month))
                    .collect(Collectors.toList());
            for (AppUsageMonthly u: filtered) {
                result.append(foundation + "," + period + "," + u.getMaximumAppInstances() + "," + u.getAverageAppInstances() + "," + u.getAppInstanceHours() + "\n");
            }
        } catch (JsonParseException jpe) {
            log.warn(String.format("Could not parse file contents of %s into a AppUsageReport!", filename), jpe);
        } catch (JsonMappingException jme) {
            log.warn(String.format("Could not map file contents in %s into JSON!", filename), jme);
        } catch (IOException ioe) {
            log.warn(String.format("Trouble creating report from %s!", filename), ioe);
        }
        return result.toString();
    }

    protected AppUsageReport readAppUsageReport(String filename) throws JsonParseException, JsonMappingException, IOException {
        String content = readFile(filename);
        if (filename.endsWith(".json")) {
            return mapper.readValue(content, AppUsageReport.class);
        } else if (filename.endsWith(".csv")) {
            CsvMapper csvMapper = new CsvMapper();
            csvMapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
            File csvFile = new File(filename);
            MappingIterator<String[]> it = csvMapper.readerFor(String[].class).readValues(csvFile);
            AppUsageReportBuilder builder = AppUsageReport.builder();
            List<AppUsageMonthly> reports = new ArrayList<>();
            int rowNum = 0;
            while (it.hasNext()) {
                String[] row = it.next();
                if (rowNum > 0) {
                    AppUsageMonthlyBuilder amb = AppUsageMonthly.builder();
                    for (int i = 0; i < row.length; i++) {
                        if (i == 0) {
                            String[] period = row[i].split("-");
                            if (period.length == 2) {
                                amb.month(Integer.valueOf(period[1]));
                            }
                            amb.year(Integer.valueOf(period[0]));
                        }
                        if (i == 1) {
                            amb.averageAppInstances(Double.valueOf(row[i]));
                        }
                        if (i == 2) {
                            amb.maximumAppInstances(Integer.valueOf(row[i]));
                        }
                        if (i == 3) {
                            amb.appInstanceHours(Double.valueOf(row[i]));
                        }
                    }
                    reports.add(amb.build());
                }
                rowNum++;
            }
            builder.monthlyReports(reports);
            return builder.build();
        } else {
            return AppUsageReport.builder().build();
        }
    }

    protected String readFile(String filename) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException ioe) {
            log.warn(String.format("Trouble reading file %s contents", filename), ioe);
        }
        return content;
    }

}
