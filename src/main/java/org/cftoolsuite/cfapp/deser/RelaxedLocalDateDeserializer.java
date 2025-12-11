package org.cftoolsuite.cfapp.deser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.StringUtils;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class RelaxedLocalDateDeserializer extends ValueDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        try {
            String dateStr = p.getText();
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            try {
                String date = p.getText();
                String[] iso8601DateParts = date.split("-");
                if (StringUtils.isNotBlank(iso8601DateParts[0]) && iso8601DateParts[0].length() > 4) {
                    return LocalDate.MAX;
                }
            } catch (JacksonException je) {
                throw je;
            }
            throw new RuntimeException(e);
        }
    }
}
