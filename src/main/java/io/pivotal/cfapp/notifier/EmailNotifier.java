package io.pivotal.cfapp.notifier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationListener;

import io.pivotal.cfapp.event.EmailNotificationEvent;

public abstract class EmailNotifier implements ApplicationListener<EmailNotificationEvent> {

    public abstract void sendMail(String from, String to, String subject, String body, Map<String, String> attachmentContents);

    @Override
    public void onApplicationEvent(EmailNotificationEvent event) {
        List<String> recipients = event.getRecipients();
        String from = event.getFrom();
        String footer =
            String.format("[This email was sent from %s on %s]",
                event.getDomain(), DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()));
        String body = String.format("%s%n%n%s", event.getBody(), footer);
        String subject = event.getSubject();
        Map<String, String> attachmentContents = event.getAttachmentContents();
        recipients.forEach(recipient -> {
            sendMail(from, recipient, subject, body, attachmentContents);
        });
    }

}