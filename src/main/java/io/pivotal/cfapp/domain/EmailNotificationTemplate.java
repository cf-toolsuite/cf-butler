package io.pivotal.cfapp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

import lombok.Builder;
import lombok.Getter;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "from", "to", "subject", "body", "attachmentsFormat" })
@Getter
public class EmailNotificationTemplate {

    @JsonProperty("from")
    private String from;

    @JsonProperty("to")
    private String to;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("body")
    private String body;

    @JsonProperty("attachmentsFormat")
    private String attachmentsFormat;

    @JsonCreator
    public EmailNotificationTemplate(
        @JsonProperty("from") String from,
        @JsonProperty("to") String to,
        @JsonProperty("subject") String subject,
        @JsonProperty("body") String body,
        @JsonProperty("attachmentsFormat") String attachmentsFormat) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.attachmentsFormat = attachmentsFormat;
    }

    @JsonIgnore
    public boolean isValid() {
        return EmailValidator.isValid(from)
                && EmailValidator.isValid(to)
                && StringUtils.isNotBlank(subject)
                && StringUtils.isNotBlank(body)
                && isMediaType(attachmentsFormat);
    }

    private static boolean isMediaType(String value) {
        boolean result = true;
        try {
            MediaType.valueOf(value);
        } catch (InvalidMediaTypeException imte) {
            result = false;
        }
        return result;
    }
}