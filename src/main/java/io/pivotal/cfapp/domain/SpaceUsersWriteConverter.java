package io.pivotal.cfapp.domain;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.stereotype.Indexed;

@Indexed
@WritingConverter
public class SpaceUsersWriteConverter implements Converter<SpaceUsers, OutboundRow> {

    @Override
    public OutboundRow convert(SpaceUsers source) {
        OutboundRow row = new OutboundRow();
        row.put("organization", Parameter.fromOrEmpty(source.getOrganization(), String.class));
        row.put("space", Parameter.fromOrEmpty(source.getSpace(), String.class));
        row.put("auditors", Parameter.fromOrEmpty(source.getAuditors().stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(",")), String.class));
        row.put("developers", Parameter.fromOrEmpty(source.getDevelopers().stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(",")), String.class));
        row.put("managers", Parameter.fromOrEmpty(source.getManagers().stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(",")), String.class));
        return row;
    }

}
