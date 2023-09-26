package io.pivotal.cfapp.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "from", "to", "subject", "body" })
@Getter
public class EmailNotificationTemplate {

    private static boolean areRecipientsValid(List<String> recipients) {
        boolean result = true;
        if (!ObjectUtils.isEmpty(recipients)) {
            for (String recipient: recipients) {
                if (!EmailValidator.isValid(recipient)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    @JsonProperty("from")
    private String from;

    @Default
    @JsonProperty("to")
    private List<String> to = new ArrayList<>();

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("body")
    private String body;

    @JsonCreator
    public EmailNotificationTemplate(
            @JsonProperty("from") String from,
            @JsonProperty("to") List<String> to,
            @JsonProperty("subject") String subject,
            @JsonProperty("body") String body
            ) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    @JsonIgnore
    public boolean isValid() {
        return EmailValidator.isValid(from)
                && areRecipientsValid(to)
                && StringUtils.isNotBlank(subject)
                && StringUtils.isNotBlank(body);
    }
}
