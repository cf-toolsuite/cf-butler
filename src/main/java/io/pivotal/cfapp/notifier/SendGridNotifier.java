package io.pivotal.cfapp.notifier;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import org.springframework.http.HttpStatus;

import io.pivotal.cfapp.domain.EmailAttachment;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendGridNotifier extends EmailNotifier {

    private SendGrid sendGrid;

    public SendGridNotifier(SendGrid sendGrid) {
        super();
        this.sendGrid = sendGrid;
    }

    public void sendMail(String originator, String recipient, String subject, String body, List<EmailAttachment> attachments) {
        try {
            Email from = new Email(originator);
            Email to = new Email(recipient);
            Content content = new Content("text/html", body);
            Mail mail = new Mail(from, subject, to, content);
            addAttachments(mail, attachments);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            log.info("Email sent to {} with subject: {}!", to, subject);
            log.info(HttpStatus.valueOf(response.getStatusCode()).getReasonPhrase());
        } catch (IOException ioe) {
            log.warn("Could not send email!", ioe);
        }
    }

	private static void addAttachments(Mail mail, List<EmailAttachment> attachments) {
        attachments.forEach(ea -> {
            Attachments payload = new Attachments();
            payload.setContent(new String(Base64.getEncoder().encode(ea.getHeadedContent().getBytes())));
            payload.setType(ea.getMimeType());
            payload.setFilename(ea.getFilename() + ea.getExtension());
            payload.setDisposition("attachment");
            payload.setContentId(UUID.randomUUID().toString());
            mail.addAttachments(payload);
        });
    }
}