package io.pivotal.cfapp.event;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.EmailAttachment;

public class EmailNotificationEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private String domain;
    private List<String> recipients;
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

    public List<String> getRecipients() {
        return recipients;
    }

    public String getSubject() {
        return subject;
    }

    public EmailNotificationEvent recipient(String recipient) {
        if (this.recipients == null) {
            this.recipients = new ArrayList<>();
        }
        this.recipients.add(recipient);
        return this;
    }

    public EmailNotificationEvent recipients(List<String> recipients) {
        this.recipients = recipients;
        return this;
    }

    public EmailNotificationEvent subject(String subject) {
        this.subject = subject;
        return this;
    }

}
