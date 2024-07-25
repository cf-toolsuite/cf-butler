package org.cftoolsuite.cfapp.util;

import static org.junit.jupiter.api.Assertions.fail;

import org.cftoolsuite.cfapp.ButlerTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;

@ButlerTest
@ExtendWith(SpringExtension.class)
public class JsonToCsvConverterTest {

    @Autowired
    private JsonToCsvConverter converter;

    @Test
    public void testValidJson() {
        try {
            converter.convert("{ \"foo\": \"bar\" }");
        } catch (JsonProcessingException e) {
            fail();
        }
    }

    @Test
    public void testValidJsonArray() {
        try {
            converter.convert("[{ \"foo\": \"bar\" }]");
        } catch (JsonProcessingException e) {
            fail();
        }
    }

    @Test
    public void testStringIsEmpty() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            converter.convert("[]");
        });
    }

    @Test
    public void testInvalidInput() {
        Assertions.assertThrows(JsonProcessingException.class, () -> {
            converter.convert("[{ \"foo\": \"bar\" }");
        });
    }
}
