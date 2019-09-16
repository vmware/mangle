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

package com.vmware.mangle.unittest.metric.reporter.common;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.metric.reporter.common.Metric;

/**
 *
 *
 * @author chetanc
 */
public class MetricTest {

    private String metricName = "Metric-name";
    private double metricValue = 2346.13461134;
    private String source = "Metric-source";

    /**
     * Test method for {@link Metric#getMetricName()}
     */
    @Test(description = "To verify the creation of the Metric instance using the constructor that takes metricName, metricValue, and source attributes")
    public void testMetricInstantiation() {
        Metric metric = new Metric("", 0, "");
        HashMap<String, String> tags = new HashMap<>();
        metric.setTags(tags);
        metric.setMetricName(metricName);
        metric.setSource(source);
        metric.setMetricValue(metricValue);
        Long time = System.currentTimeMillis();
        metric.setMetricTimeStamp(time);

        Assert.assertEquals(metric.getMetricName(), metricName);
        Assert.assertEquals(metric.getMetricValue(), metricValue);
        Assert.assertEquals(metric.getSource(), source);
        Assert.assertEquals(metric.getMetricTimeStamp(), time);
    }

    /**
     * Test method for {@link Metric#getMetricName()}
     */
    @Test(description = "To verify the creation of the metric instance using the constructor that takes metricName, metricValue, metricTimeStamp, tags and source attributes")
    public void testMetricInstantiation1() {
        HashMap<String, String> tags = new HashMap<>();
        Metric metric = new Metric("", 0, System.currentTimeMillis(), tags, "");

        metric.setMetricName(metricName);
        metric.setSource(source);
        metric.setMetricValue(metricValue);

        Assert.assertEquals(metric.getMetricName(), metricName);
        Assert.assertEquals(metric.getMetricValue(), metricValue);
        Assert.assertEquals(metric.getSource(), source);
    }
}
