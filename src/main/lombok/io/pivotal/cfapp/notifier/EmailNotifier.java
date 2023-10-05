package io.pivotal.cfapp.notifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;

import io.pivotal.cfapp.domain.EmailAttachment;
import io.pivotal.cfapp.event.EmailNotificationEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class EmailNotifier implements ApplicationListener<EmailNotificationEvent> {

    private final String template = getEmailTemplate();

    protected String buildBody(String template, String body, String subject, String footer) {
        String result = "";
        if (StringUtils.isNotBlank(template) && isEmailTemplate(template)) {
            result = template.replace("{{header}}", subject).replace("{{body}}", body).replace("{{footer}}", footer);
        } else {
            result = String.format("%s<br/><br/>%s", body, footer);
        }
        return result;
    }

    private String getEmailTemplate() {
        String result = null;
        try (
                InputStream resource = new ClassPathResource("email-template.html").getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
                ) {
            result =
                    reader
                    .lines()
                    .collect(Collectors.joining("\n"));
        } catch (IOException ioe) {
            log.warn("Problem reading email-template.html. {}", ioe.getMessage());
        }
        return result;
    }

    protected boolean isEmailTemplate(String template) {
        boolean result = false;
        if (template.contains("{{header}}") && template.contains("{{body}}") && template.contains("{{footer}}")) {
            result = true;
        }
        return result;
    }

    @Override
    public void onApplicationEvent(EmailNotificationEvent event) {
        List<String> recipients = event.getRecipients();
        String from = event.getFrom();
        String footer =
                String.format("This email was sent from %s on %s",
                        event.getDomain(), DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()));
        String subject = event.getSubject();
        final String body = buildBody(template, event.getBody(), subject, footer);
        log.trace("About to send email using ||> From: {}, To: {}, Subject: {}, Body: {}", from, recipients.toString(), subject, body);
        List<EmailAttachment> prunedAttachments = new ArrayList<EmailAttachment>();;
        if (event.getAttachments()!=null){
            prunedAttachments = event.getAttachments().stream().filter(EmailAttachment::hasContent).collect(Collectors.toList());
        }
        List<EmailAttachment> attachments = prunedAttachments;
        recipients.forEach(recipient -> {
                sendMail(from, recipient, subject, body, attachments);
        });
    }
    public abstract void sendMail(String from, String to, String subject, String body, List<EmailAttachment> attachments);

}
