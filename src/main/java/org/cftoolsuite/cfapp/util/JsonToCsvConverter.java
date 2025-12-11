package org.cftoolsuite.cfapp.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class JsonToCsvConverter {

    private final ObjectMapper objectMapper;

    public JsonToCsvConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String convert(String jsonString) throws DatabindException, JacksonException {
        StringWriter writer = new StringWriter();
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        List<JsonNode> jsonNodes = new ArrayList<>();
        if (jsonNode.isArray()) {
            jsonNode.forEach(jsonNodes::add);
        } else {
            jsonNodes.add(jsonNode);
        }
        if (jsonNodes.isEmpty()) {
            throw new IllegalArgumentException("JSON string must contain at least one element");
        }
        List<String> headers = new ArrayList<>();
        jsonNodes.get(0).propertyNames().iterator().forEachRemaining(headers::add);
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(headers.toArray(new String[]{})).build();
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            for (JsonNode row : jsonNodes) {
                var values = new ArrayList<>();
                headers.forEach(header -> values.add(row.get(header).asString()));
                csvPrinter.printRecord(values);
            }
            csvPrinter.flush();
        } catch (IOException e) {
            log.error("Could not write comma-separated value string");
        }
        return writer.toString();
    }

}
