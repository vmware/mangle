/*
 * Copyright (c) 2016-2019 VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package com.vmware.mangle.metric.reporter.common;

import java.util.HashMap;
import java.util.Map;

import com.vmware.mangle.metric.reporter.helpers.metric.MetricsHelper;

/**
 * Common Parameters for any metric. The Metric Type can be consumed by any reporter.
 *
 * @author dbhat
 */
public class Metric {
    String metricName;
    double metricValue;
    Long metricTimeStamp;
    Map<String, String> tags = new HashMap<>();
    String source;

    public Metric(String metricName, double metricValue, String source) {
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.metricTimeStamp = MetricsHelper.getCurrentTimeStampInMillis();
        this.tags = null;
        this.source = source;
    }

    public Metric(String metricName, double metricValue, Map<String, String> tags, String source) {
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.metricTimeStamp = MetricsHelper.getCurrentTimeStampInMillis();
        this.tags = tags;
        this.source = source;
    }

    public Metric(String metricName, double metricValue, Long metricTimeStamp, Map<String, String> tags,
            String source) {
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.metricTimeStamp = metricTimeStamp;
        this.tags = tags;
        this.source = source;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public double getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(double metricValue) {
        this.metricValue = metricValue;
    }

    public Long getMetricTimeStamp() {
        return metricTimeStamp;
    }

    public void setMetricTimeStamp(Long metricTimeStamp) {
        this.metricTimeStamp = metricTimeStamp;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "metricName: " + getMetricName() + " metricValue: " + getMetricValue() + " timeStamp: "
                + getMetricTimeStamp() + " tags: " + getTags() + " source: " + getSource();
    }

}
