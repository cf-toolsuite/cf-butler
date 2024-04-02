package org.cftoolsuite.cfapp.service;

import org.cftoolsuite.cfapp.domain.accounting.application.AppUsageReport;
import org.cftoolsuite.cfapp.domain.accounting.service.ServiceUsageReport;
import org.cftoolsuite.cfapp.domain.accounting.task.TaskUsageReport;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class UsageCache {

    private AppUsageReport applicationReport;
    private ServiceUsageReport serviceReport;
    private TaskUsageReport taskReport;

}
