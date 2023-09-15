package io.pivotal.cfapp.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CsvUtil {

    public static Set<String> parse(String csvInput) {
        Set<String> result = new HashSet<>();
        if (StringUtils.isNotBlank(csvInput)) {
            try {
                CSVParser csvParser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
                for (CSVRecord csvRecord : csvParser) {
                    List<String> value = csvRecord.toList();
                    result.addAll(value);
                }
            } catch (IOException e) {
                log.error("Error parsing CSV input", e);
            }
        }
        return result;
    }
}
