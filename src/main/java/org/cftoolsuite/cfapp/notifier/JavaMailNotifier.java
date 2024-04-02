package org.cftoolsuite.cfapp.notifier;

import java.io.IOException;
import java.util.List;

import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;

import org.cftoolsuite.cfapp.domain.EmailAttachment;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JavaMailNotifier extends EmailNotifier {

    private static void addAttachment(MimeMessageHelper helper, EmailAttachment ea) {
        try {
            DataSource ds = new ByteArrayDataSource(ea.getHeadedContent(), ea.getMimeType());
            helper.addAttachment(ea.getFilename() + ea.getExtension(), ds);
        } catch (MessagingException | IOException e) {
            log.warn("Could not add attachment to email!", e);
        }
    }

    private final JavaMailSender javaMailSender;

    public JavaMailNotifier(JavaMailSender javaMailSender) {
        super();
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendMail(String from, String to, String subject, String body, List<EmailAttachment> attachments) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setSubject(subject);
            helper.setTo(to);
            helper.setText(body, true);
            attachments.forEach(ea -> addAttachment(helper, ea));
            javaMailSender.send(message);
            log.info("Email sent to {} with subject: {}", to, subject);
        } catch (MailException | MessagingException me) {
            log.warn("Could not send email!", me);
        }
    }
}
