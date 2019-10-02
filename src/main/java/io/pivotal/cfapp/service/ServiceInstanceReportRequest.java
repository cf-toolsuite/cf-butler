package io.pivotal.cfapp.service;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
class ServiceInstanceReportRequest {
    private String foundation;
    private String environment;
    private String period;
    private String filename;
}