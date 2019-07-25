package io.pivotal.cfapp.event;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEvent;

public class EmailNotificationEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private String domain;
    private List<String> recipients;
    private String from;
    private String subject;
    private String body;
    private Map<String, String> attachmentContents;

    public EmailNotificationEvent(Object source) {
        super(source);
    }

    public EmailNotificationEvent domain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public EmailNotificationEvent recipients(List<String> recipients) {
        this.recipients = recipients;
        return this;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public EmailNotificationEvent from(String from) {
        this.from = from;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public EmailNotificationEvent subject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public EmailNotificationEvent body(String body) {
        this.body = body;
        return this;
    }

    public String getBody() {
        return body;
    }

    public EmailNotificationEvent attachmentContents(Map<String, String> attachmentContents) {
        this.attachmentContents = attachmentContents;
        return this;
    }

    public Map<String, String> getAttachmentContents() {
        return attachmentContents;
    }

}