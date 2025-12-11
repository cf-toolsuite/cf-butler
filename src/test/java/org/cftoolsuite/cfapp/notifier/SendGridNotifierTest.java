package org.cftoolsuite.cfapp.notifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.cftoolsuite.cfapp.event.EmailNotificationEvent;
import org.cftoolsuite.cfapp.util.EmailTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

class SendGridNotifierTest {

    @Mock
    private SendGrid sendGrid;

    private SendGridNotifier sendGridNotifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sendGridNotifier = new SendGridNotifier(null, sendGrid);
    }

    @Test
    void testSendSimpleEmail() throws IOException {
        EmailNotificationEvent event = EmailTestUtil.readEmailTemplate("simple-email.json");
        Response mockResponse = new Response();
        mockResponse.setStatusCode(200);

        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        sendGridNotifier.onApplicationEvent(event);

        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void testSendEmailWithAttachments() throws IOException {
        EmailNotificationEvent event = EmailTestUtil.readEmailTemplate("email-with-attachments.json");
        Response mockResponse = new Response();
        mockResponse.setStatusCode(200);

        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        sendGridNotifier.onApplicationEvent(event);

        verify(sendGrid).api(any(Request.class));
    }
}
