package org.cftoolsuite.cfapp.notifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import jakarta.mail.internet.MimeMessage;

import org.cftoolsuite.cfapp.event.EmailNotificationEvent;
import org.cftoolsuite.cfapp.util.EmailTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

class JavaMailNotifierTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    private JavaMailNotifier javaMailNotifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        javaMailNotifier = new JavaMailNotifier(null, javaMailSender);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void testSendSimpleEmail() throws IOException {
        EmailNotificationEvent event = EmailTestUtil.readEmailTemplate("simple-email.json");
        javaMailNotifier.onApplicationEvent(event);

        verify(javaMailSender).send(any(MimeMessage.class));
        verify(javaMailSender, times(1)).createMimeMessage();
    }

    @Test
    void testSendEmailWithAttachments() throws IOException {
        EmailNotificationEvent event = EmailTestUtil.readEmailTemplate("email-with-attachments.json");
        javaMailNotifier.onApplicationEvent(event);

        verify(javaMailSender).send(any(MimeMessage.class));
        verify(javaMailSender, times(1)).createMimeMessage();
    }
}
