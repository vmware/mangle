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

import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.model.enums.MetricProviderType;


/**
 * @author dbhat
 *
 */

@Log4j2
public class MetricProviderFactory {
    private MetricProviderFactory() {

    }

    /**
     * Method to retrieve instance of metric provider based on the configuration / properties
     * provided. The method will be enhanced each time when new Metric provider support is added.
     *
     * @param metricProviderSpec
     *            : Monitoring tool connection properties having details on monitoring tool.
     * @return : Metric provider instance.
     */
    public static IMetricProviderHelper getActiveMetricProvider(MetricProviderSpec metricProviderSpec) {

        String metricProvider = metricProviderSpec.getMetricProviderType().toString();
        MetricProviderType metricProviderType = MetricProviderType.valueOf(metricProvider);
        switch (metricProviderType) {
        case WAVEFRONT:
            return new WavefrontDataSourceHelper(metricProviderSpec);
        case DATADOG:
            // Implementation is not available yet.
            return null;
        default:
            log.error(" Specified metric Provider type: " + metricProvider + " is NOT supported.");
            break;
        }
        return null;
    }
}
