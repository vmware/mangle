package com.vmware.mangle.metrics.models;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ResiliencyScoreProperties {
        private MonitoringToolConnectionProperties monitoringToolConnectionProperties;
        private ResiliencyScoreMetricConfig resiliencyScoreMetricConfig;
        private Map<String, String> tags;
        private List<ServiceFamily> serviceFamily;
}
