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

package com.vmware.mangle.task.framework.metric.providers;

import lombok.NonNull;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.metricprovider.DatadogConnectionProperties;
import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.cassandra.model.metricprovider.WaveFrontConnectionProperties;
import com.vmware.mangle.utils.clients.metricprovider.DatadogClient;
import com.vmware.mangle.utils.clients.metricprovider.MetricProviderClient;
import com.vmware.mangle.utils.clients.metricprovider.WaveFrontServerClient;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * @author ashrimali
 * @author dbhat
 *
 */
@Component
public class MetricProviderClientFactory {

    /**
     * @param metricProviderSpec
     * @return MetricProviderClient
     * @throws MangleException
     */
    public MetricProviderClient getMetricProviderClient(@NonNull MetricProviderSpec metricProviderSpec)
            throws MangleException {
        switch (metricProviderSpec.getMetricProviderType()) {
        case DATADOG:
            return getDatagodMetricProvier(metricProviderSpec.getDatadogConnectionProperties());
        case WAVEFRONT:
            return getWavefrontMetricProvider(metricProviderSpec.getWaveFrontConnectionProperties());
        default:
            return null;
        }
    }

    private WaveFrontServerClient getWavefrontMetricProvider(
            WaveFrontConnectionProperties waveFrontConnectionProperties) {
        return new WaveFrontServerClient(waveFrontConnectionProperties);
    }

    private MetricProviderClient getDatagodMetricProvier(DatadogConnectionProperties datadogConnectionProperties)
            throws MangleException {
        return new DatadogClient(datadogConnectionProperties);
    }

}
