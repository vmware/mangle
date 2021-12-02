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

package com.vmware.mangle.services.config;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.micrometer.dynatrace.DynatraceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.vmware.mangle.cassandra.model.metricprovider.DynatraceConnectionProperties;
import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.model.enums.MetricProviderType;
import com.vmware.mangle.services.repository.MetricProviderRepository;

/**
 * Mangle Dynatrace configuration
 *
 * @author dbhat
 *
 */

@Configuration
@NotEmpty
public class MangleDynatraceConfig implements DynatraceConfig {

    @Autowired
    private MetricProviderRepository metricProviderRepository;

    @Override
    public String get(String key) {
        List<MetricProviderSpec> dynatraceInstances =
                this.metricProviderRepository.findByMetricProviderType(MetricProviderType.DYNATRACE);

        if (!dynatraceInstances.isEmpty()) {
            DynatraceConnectionProperties dynatraceConnectionProperties =
                    dynatraceInstances.get(0).getDynatraceConnectionProperties();

            switch (key) {
            case "dynatrace.uri":
                return dynatraceConnectionProperties.getUri();
            case "dynatrace.apiToken":
                return dynatraceConnectionProperties.getApiToken();
            case "dynatrace.deviceId":
                return dynatraceConnectionProperties.getDeviceId();
            default:
                break;
            }
        } else {
            switch (key) {
            case "dynatrace.uri":
                return String.valueOf("");
            case "dynatrace.apiToken":
                return String.valueOf("");
            case "dynatrace.deviceId":
                return String.valueOf("");
            default:
                break;
            }
        }
        return null;
    }
}
