package org.cftoolsuite.cfapp.domain;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "from", "subject", "body" })
@Getter
public class OwnerNotificationTemplate {

    @JsonProperty("from")
    private String from;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("body")
    private String body;

    @JsonCreator
    public OwnerNotificationTemplate(
            @JsonProperty("from") String from,
            @JsonProperty("subject") String subject,
            @JsonProperty("body") String body
            ) {
        this.from = from;
        this.subject = subject;
        this.body = body;
    }

    @JsonIgnore
    public boolean isValid() {
        return EmailValidator.isValid(from)
                && StringUtils.isNotBlank(subject)
                && StringUtils.isNotBlank(body);
    }
}
