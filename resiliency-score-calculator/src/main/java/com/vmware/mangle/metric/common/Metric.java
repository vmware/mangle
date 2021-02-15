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

package com.vmware.mangle.metric.common;

import java.util.HashMap;

import com.vmware.mangle.metric.utils.MetricsHelper;

import lombok.Data;

/**
 * Common Parameters for any metric. The Metric Type can be consumed by any reporter.
 *
 * @author dbhat
 */
@Data
public class Metric {

    String metricName;
    double metricValue;
    Long metricTimeStamp;
    HashMap<String, String> tags = new HashMap<String, String>();
    String source;

    public Metric(String metricName, double metricValue, String source) {
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.metricTimeStamp = MetricsHelper.getCurrentTimeStampInMillis();
        this.tags = null;
        this.source = source;
    }

    public Metric(String metricName, double metricValue, HashMap<String, String> tags, String source) {
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.metricTimeStamp = MetricsHelper.getCurrentTimeStampInMillis();
        this.tags = tags;
        this.source = source;
    }

    public Metric(String metricName, double metricValue, Long metricTimeStamp, HashMap<String, String> tags,
            String source) {
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.metricTimeStamp = metricTimeStamp;
        this.tags = tags;
        this.source = source;
    }

    @Override
    public String toString() {
        String metric = "metricName: " + getMetricName() + " metricValue: " + getMetricValue() + " timeStamp: "
                + getMetricTimeStamp() + " tags: " + getTags() + " source: " + getSource();
        return metric;
    }

}
