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

import io.micrometer.datadog.DatadogConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.vmware.mangle.cassandra.model.metricprovider.DatadogConnectionProperties;
import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.model.enums.MetricProviderType;
import com.vmware.mangle.services.repository.MetricProviderRepository;


/**
 * Configuration class for Mangle Datadog
 *
 * @author ashrimali
 *
 */
@Configuration
@NotEmpty
public class MangleDatadogConfig implements DatadogConfig {

    @Autowired
    private MetricProviderRepository metricProviderRepository;

    @Override
    public String get(String key) {
        List<MetricProviderSpec> dd =
                this.metricProviderRepository.findByMetricProviderType(MetricProviderType.DATADOG);
        if (!dd.isEmpty()) {
            DatadogConnectionProperties datadogConnectionProperties = dd.get(0).getDatadogConnectionProperties();

            switch (key) {
            case "datadog.apiKey":
                return datadogConnectionProperties.getApiKey();
            case "datadog.applicationKey":
                return datadogConnectionProperties.getApplicationKey();
            case "datadog.uri":
                return datadogConnectionProperties.getUri();
            default:
                break;
            }
        } else {
            switch (key) {
            case "datadog.apiKey":
                return String.valueOf("");
            case "datadog.applicationKey":
                return String.valueOf("");
            case "datadog.uri":
                return String.valueOf("");
            default:
                break;
            }
        }

        return null;
    }
}
