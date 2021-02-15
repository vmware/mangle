package com.vmware.mangle.metrics.models;

import lombok.Data;

@Data
public class ResiliencyScoreMetricConfig {
    private String metricSource;
    private String metricName;
    private String outputMetricName;
    private String outputUrlMetricName;
    private String functionScoreMetricName;
    private Short testReferenceWindow;
    private Short resiliencyCalculationWindow;
    private String metricQueryGranularity;
}
