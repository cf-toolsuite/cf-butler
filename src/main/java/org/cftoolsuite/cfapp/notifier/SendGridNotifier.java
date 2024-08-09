package org.cftoolsuite.cfapp.notifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.cftoolsuite.cfapp.domain.EmailAttachment;
import org.springframework.http.HttpStatus;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendGridNotifier extends EmailNotifier {

    private static void addAttachments(Mail mail, List<EmailAttachment> attachments) {
        attachments.forEach(ea -> {
            Attachments payload = new Attachments();
            String content = new String(Base64.getEncoder().encode(ea.getHeadedContent().getBytes()));
            log.trace("Content of attachment {}", content);
            payload.setContent(content);
            payload.setType(ea.getMimeType());
            payload.setFilename(ea.getFilename() + ea.getExtension());
            payload.setDisposition("attachment");
            payload.setContentId(UUID.randomUUID().toString());
            mail.addAttachments(payload);
        });
    }

    private SendGrid sendGrid;

    public SendGridNotifier(String customTemplatePath, SendGrid sendGrid) {
        super(customTemplatePath);
        this.sendGrid = sendGrid;
    }

    @Override
    public void sendMail(String originator, String[] recipients, String[] carbonCopyRecipients, String[] blindCarbonCopyRecipients, String subject, String body, List<EmailAttachment> attachments) {
        try {
            Email from = new Email(originator);
            Content content = new Content("text/html", body);
            //Mail mail = new Mail(from, subject, to, content);
            Mail mail = new Mail();
            Personalization personalization = new Personalization();
            personalization.setFrom(from);
            if (recipients != null && recipients.length > 0) { Arrays.asList(recipients).forEach(to -> personalization.addCc(new Email(to))); }
            personalization.setSubject(subject);
            if (carbonCopyRecipients != null && carbonCopyRecipients.length > 0) { Arrays.asList(carbonCopyRecipients).forEach(cc -> personalization.addCc(new Email(cc))); }
            if (blindCarbonCopyRecipients != null && blindCarbonCopyRecipients.length > 0) { Arrays.asList(blindCarbonCopyRecipients).forEach(bcc -> personalization.addBcc(new Email(bcc))); }
            mail.addContent(content);
            mail.addPersonalization(personalization);
            addAttachments(mail, attachments);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            log.info("Email sent to {} with subject: {}", recipients, subject);
            if (carbonCopyRecipients != null && carbonCopyRecipients.length > 0) { log.info("Also sent to cc: {}", String.join(", ", carbonCopyRecipients)); }
            if (blindCarbonCopyRecipients != null && blindCarbonCopyRecipients.length > 0) { log.info("Also sent to bcc: {}", String.join(", ", blindCarbonCopyRecipients)); }
            log.trace(String.format("\n\tStatus: %s\n\t%s", HttpStatus.valueOf(response.getStatusCode()).getReasonPhrase(), response.getBody()));
        } catch (IOException ioe) {
            log.warn("Could not send email!", ioe);
        }
    }
}
