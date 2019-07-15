package io.pivotal.cfapp.notifier;

import java.io.IOException;
import java.util.Map;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JavaMailNotifier extends EmailNotifier {

    private final JavaMailSender javaMailSender;

    public JavaMailNotifier(JavaMailSender javaMailSender) {
        super();
        this.javaMailSender = javaMailSender;
    }

    public void sendMail(String from, String to, String subject, String body, Map<String, String> attachmentContents) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setSubject(subject);
            helper.setTo(to);
            helper.setText(body, true);
            attachmentContents.entrySet().forEach(e -> addAttachment(helper, e.getKey(), e.getValue()));
            javaMailSender.send(message);
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