package org.cftoolsuite.cfapp.event;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cftoolsuite.cfapp.domain.EmailAttachment;
import org.springframework.context.ApplicationEvent;

public class EmailNotificationEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private String domain;
    private Set<String> recipients;
    private Set<String> carbonCopyRecipients;
    private Set<String> blindCarbonCopyRecipients;
    private String from;
    private String subject;
    private String body;
    private List<EmailAttachment> attachments;

    public EmailNotificationEvent(Object source) {
        super(source);
    }

    public EmailNotificationEvent attachments(List<EmailAttachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public EmailNotificationEvent body(String body) {
        this.body = body;
        return this;
    }

    public EmailNotificationEvent domain(String domain) {
        this.domain = domain;
        return this;
    }

    public EmailNotificationEvent from(String from) {
        this.from = from;
        return this;
    }

    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    public String getBody() {
        return body;
    }

    public String getDomain() {
        return domain;
    }

    public String getFrom() {
        return from;
    }

    public Set<String> getRecipients() {
        return recipients;
    }

    public Set<String> getCarbonCopyRecipients() {
        return carbonCopyRecipients;
    }

    public Set<String> getBlindCarbonCopyRecipients() {
        return blindCarbonCopyRecipients;
    }

    public String getSubject() {
        return subject;
    }

    public EmailNotificationEvent recipient(String recipient) {
        if (this.recipients == null) {
            this.recipients = new HashSet<>();
        }
        this.recipients.add(recipient);
        return this;
    }

    public EmailNotificationEvent recipients(Set<String> recipients) {
        this.recipients = recipients;
        return this;
    }

    public EmailNotificationEvent carbonCopyRecipient(String carbonCopyRecipient) {
        if (this.carbonCopyRecipients == null) {
            this.carbonCopyRecipients = new HashSet<>();
        }
        this.recipients.add(carbonCopyRecipient);
        return this;
    }

    public EmailNotificationEvent carbonCopyRecipients(Set<String> carbonCopyRecipients) {
        this.carbonCopyRecipients = carbonCopyRecipients;
        return this;
    }

    public EmailNotificationEvent blindCarbonCopyRecipient(String blindCarbonCopyRecipient) {
        if (this.blindCarbonCopyRecipients == null) {
            this.blindCarbonCopyRecipients = new HashSet<>();
        }
        this.recipients.add(blindCarbonCopyRecipient);
        return this;
    }

    public EmailNotificationEvent blindCarbonCopyRecipients(Set<String> blindCarbonCopyRecipients) {
        this.blindCarbonCopyRecipients = blindCarbonCopyRecipients;
        return this;
    }

    public EmailNotificationEvent subject(String subject) {
        this.subject = subject;
        return this;
    }

}
