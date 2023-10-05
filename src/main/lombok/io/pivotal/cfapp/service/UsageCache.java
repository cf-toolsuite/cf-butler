package io.pivotal.cfapp.service;

import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.accounting.application.AppUsageReport;
import io.pivotal.cfapp.domain.accounting.service.ServiceUsageReport;
import io.pivotal.cfapp.domain.accounting.task.TaskUsageReport;
import lombok.Data;

@Data
@Component
public class UsageCache {

    private AppUsageReport applicationReport;
    private ServiceUsageReport serviceReport;
    private TaskUsageReport taskReport;

}
