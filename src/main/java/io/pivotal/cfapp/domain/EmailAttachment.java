package io.pivotal.cfapp.domain;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EmailAttachment {

    private final String headers;
    private final String content;
    private final String filename;
    private final String mimeType;
    private final String extension;

    public String getHeadedContent() {
        String headers = hasHeaders() ? getHeaders() : "";
        String content = hasContent() ? getContent(): "";
        return headers + System.getProperty("line.separator") + content;
    }

    public boolean hasContent() {
        return StringUtils.isNotBlank(getContent());
    }

    public boolean hasHeaders() {
        return StringUtils.isNotBlank(getHeaders());
    }

}