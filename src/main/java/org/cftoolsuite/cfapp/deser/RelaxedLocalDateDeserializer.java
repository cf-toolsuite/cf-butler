package org.cftoolsuite.cfapp.deser;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

public class RelaxedLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    private LocalDateDeserializer conformingDeserializer = LocalDateDeserializer.INSTANCE;

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        try {
            return conformingDeserializer.deserialize(p, ctxt);
        } catch (InvalidFormatException | DateTimeParseException e) {
            String date = p.getText();
            String[] iso8601DateParts = date.split("-");
            if (StringUtils.isNotBlank(iso8601DateParts[0]) && iso8601DateParts[0].length() > 4) {
                return LocalDate.MAX;
            }
            throw new RuntimeException(e);
        }
    }
}
