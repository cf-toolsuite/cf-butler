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
        StringBuilder result = new StringBuilder();
        if (hasHeaders()) {
            result.append(getHeaders());
            result.append(System.getProperty("line.separator"));
        }
        if (hasContent()) {
            result.append(getContent());
        }
        return result.toString();
    }

    public boolean hasContent() {
        return StringUtils.isNotBlank(getContent());
    }

    public boolean hasHeaders() {
        return StringUtils.isNotBlank(getHeaders());
    }

}
