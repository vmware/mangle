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

package com.vmware.mangle.utils.helpers.metricprovider;

import java.util.List;
import java.util.Map;

import com.vmware.mangle.metric.reporter.common.Metric;
import com.vmware.mangle.model.metricprovider.wavefront.WavefrontEvent;
import com.vmware.mangle.model.resiliencyscore.TimeseriesData;

/**
 * @author dbhat
 *
 */
public interface IMetricProviderHelper {
    List<TimeseriesData> getTimeSeriesData(String query, long startTime, long endTime, String granularity);

    List<WavefrontEvent> getEvents(Map<String, String> tags, String serviceFamilyName, String serviceName,
            long startTime, long endTime);

    void sendMetric(Metric metric);
}
