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

import io.micrometer.wavefront.WavefrontConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.cassandra.model.metricprovider.WaveFrontConnectionProperties;
import com.vmware.mangle.model.enums.MetricProviderType;
import com.vmware.mangle.services.repository.MetricProviderRepository;

/**
 * Configuration class for Mangle Wavefront
 *
 * @author ashrimali
 */
@Configuration
@NotEmpty
public class MangleWavefrontConfig implements WavefrontConfig {

    @Autowired
    private MetricProviderRepository metricProviderRepository;

    @Override
    public String get(String key) {
        List<MetricProviderSpec> wf =
                this.metricProviderRepository.findByMetricProviderType(MetricProviderType.WAVEFRONT);
        if (!wf.isEmpty()) {
            WaveFrontConnectionProperties waveFrontConnectionProperties = wf.get(0).getWaveFrontConnectionProperties();

            switch (key) {
            case "wavefront.uri":
                return waveFrontConnectionProperties.getWavefrontInstance();
            case "wavefront.apiToken":
                return waveFrontConnectionProperties.getWavefrontAPIToken();
            case "wavefront.source":
                return waveFrontConnectionProperties.getSource();
            default:
                break;
            }
        } else {
            switch (key) {
            case "wavefront.uri":
                return String.valueOf("");
            case "wavefront.apiToken":
                return String.valueOf("DummyToken");
            case "wavefront.source":
                return String.valueOf("");
            default:
                break;
            }
        }
        return null;
    }

}
