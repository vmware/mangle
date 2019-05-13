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

package com.vmware.mangle.services.helpers;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.services.MetricProviderService;
import com.vmware.mangle.task.framework.metric.providers.MetricProviderClientFactory;
import com.vmware.mangle.utils.clients.metricprovider.DatadogClient;
import com.vmware.mangle.utils.clients.metricprovider.MetricProviderClient;
import com.vmware.mangle.utils.clients.metricprovider.WaveFrontServerClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.helpers.notifiers.DatadogEventNotifier;
import com.vmware.mangle.utils.helpers.notifiers.Notifier;
import com.vmware.mangle.utils.helpers.notifiers.WavefrontNotifier;

/**
 * @author dbhat
 *
 */
@Component
@Log4j2
public class MetricProviderHelper {
    @Autowired
    MetricProviderService metricProviderService;

    @Autowired
    MetricProviderClientFactory metrciProviderClientFactory;


    public Notifier getActiveNotificationProvider() {
        try {
            MetricProviderSpec activeMetricProvider = metricProviderService.getActiveMetricProvider();
            if (null == activeMetricProvider) {
                log.debug(" No Active metric providers are found. ");
                return null;
            }
            MetricProviderClient client = metrciProviderClientFactory.getMetricProviderClient(activeMetricProvider);
            if (client instanceof DatadogClient) {
                DatadogClient datadogClient = (DatadogClient) client;
                return new DatadogEventNotifier(datadogClient);
            }
            if (client instanceof WaveFrontServerClient) {
                WaveFrontServerClient wavefrontClient = (WaveFrontServerClient) client;
                return new WavefrontNotifier(wavefrontClient);
            }
            return null;
        } catch (MangleException e) {
            return null;
        }
    }

}
