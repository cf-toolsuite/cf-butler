package org.cftoolsuite.cfapp.domain;

import java.util.HashSet;
import java.util.Set;

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
@JsonPropertyOrder({ "from", "to", "cc", "bcc", "subject", "body" })
@Getter
public class EmailNotificationTemplate {

    private static boolean areRecipientsValid(Set<String> recipients) {
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
    private Set<String> to = new HashSet<>();

    @Default
    @JsonProperty("cc")
    private Set<String> cc = new HashSet<>();

    @Default
    @JsonProperty("bcc")
    private Set<String> bcc = new HashSet<>();

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("body")
    private String body;

    @JsonCreator
    public EmailNotificationTemplate(
            @JsonProperty("from") String from,
            @JsonProperty("to") Set<String> to,
            @JsonProperty("cc") Set<String> cc,
            @JsonProperty("bcc") Set<String> bcc,
            @JsonProperty("subject") String subject,
            @JsonProperty("body") String body
            ) {
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.body = body;
    }

    @JsonIgnore
    public boolean isValid() {
        return EmailValidator.isValid(from)
                && areRecipientsValid(to)
                && areRecipientsValid(cc)
                && areRecipientsValid(bcc)
                && StringUtils.isNotBlank(subject)
                && StringUtils.isNotBlank(body);
    }
}
