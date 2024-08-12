package org.cftoolsuite.cfapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.cftoolsuite.cfapp.domain.EmailAttachment;
import org.cftoolsuite.cfapp.domain.EmailAttachment.EmailAttachmentBuilder;
import org.cftoolsuite.cfapp.event.EmailNotificationEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EmailTestUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static EmailNotificationEvent readEmailTemplate(String filename) throws IOException {
        try (InputStream is = EmailTestUtil.class.getResourceAsStream("/email-templates/" + filename)) {
            Map<String, Object> data = objectMapper.readValue(is, Map.class);

            EmailNotificationEvent event = new EmailNotificationEvent("Something special");
            event.from((String) data.get("from"));
            event.recipients(Set.copyOf((List<String>) data.get("recipients")));
            event.carbonCopyRecipients(Set.copyOf((List<String>) data.get("carbonCopyRecipients")));
            event.blindCarbonCopyRecipients(Set.copyOf((List<String>) data.get("blindCarbonCopyRecipients")));
            event.subject((String) data.get("subject"));
            event.body((String) data.get("body"));
            event.domain((String) data.get("domain"));

            List<Map<String, Object>> attachmentsData = (List<Map<String, Object>>) data.get("attachments");
            List<EmailAttachment> attachments = attachmentsData.stream()
                .map(EmailTestUtil::createEmailAttachment)
                .collect(Collectors.toList());
            event.attachments(attachments);

            return event;
        }
    }

    private static EmailAttachment createEmailAttachment(Map<String, Object> data) {
        EmailAttachmentBuilder attachment = EmailAttachment.builder();
        attachment.filename((String) data.get("filename"));
        attachment.extension((String) data.get("extension"));
        attachment.mimeType((String) data.get("mimeType"));
        String base64EncodedContent = (String) data.get("content");
        byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedContent);
        String content = new String(decodedBytes, StandardCharsets.UTF_8);
        attachment.content(content);
        return attachment.build();
    }
}
