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

package com.vmware.mangle.metrics.models;


import java.util.Map;

/**
 * Client Interface for all types of Metric Providers.
 * 
 * @author ashrimali
 *
 */
public interface MetricProviderHelper {
    MetricProviderEventResponse queryEvents(Map<String, String> environment, String serviceFamilyName,
            String serviceName, String startTime, String endTime);

    WavefrontMetricQueryResponse queryMetrics(String metricName, String serviceName, long startTime, long endTime,
            String queryFilterString, String granularity);
}
