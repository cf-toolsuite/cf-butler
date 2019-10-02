package io.pivotal.cfapp.service;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
class ReportRequest {
    private String foundation;
    private String environment;
    private String period;
    private String filename;
}