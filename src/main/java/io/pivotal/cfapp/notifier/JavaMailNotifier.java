package io.pivotal.cfapp.notifier;

import java.io.IOException;
import java.util.List;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import io.pivotal.cfapp.domain.EmailAttachment;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JavaMailNotifier extends EmailNotifier {

    private final JavaMailSender javaMailSender;

    public JavaMailNotifier(JavaMailSender javaMailSender) {
        super();
        this.javaMailSender = javaMailSender;
    }

    public void sendMail(String from, String to, String subject, String body, List<EmailAttachment> attachments) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setSubject(subject);
            helper.setTo(to);
            helper.setText(body, true);
            attachments.forEach(ea -> addAttachment(helper, ea.getFilename(), ea.getHeadedContent()));
            javaMailSender.send(message);
            log.info("Email sent to {} with subject: {}!", to, subject);
        } catch (MessagingException me) {
            log.warn("Could not send email!", me);
        }
    }

    private static void addAttachment(MimeMessageHelper helper, String filename, String content) {
        try {
            DataSource ds = new ByteArrayDataSource(content, "text/csv");
            helper.addAttachment(filename + ".csv", ds);
        } catch (MessagingException | IOException e) {
            log.warn("Could not add attachment to email!", e);
        }
    }
}