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

package com.vmware.mangle.services;

import java.util.List;

import io.micrometer.wavefront.WavefrontMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.model.enums.MetricProviderType;
import com.vmware.mangle.services.repository.MetricProviderRepository;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.constants.MetricProviderConstants;

/**
 * Class will customise the commons tags to wavefront meter registry. It will be taken from the
 * added wavefront metric provider to database.
 *
 * @author ashrimali
 *
 */
@Configuration
@DependsOn("hazelcastInstance")
public class WavefrontRegistryCustomizer implements MeterRegistryCustomizer<WavefrontMeterRegistry> {

    @Autowired
    private MetricProviderRepository metricProviderRepository;

    @Value("${hazelcast.config.cluster.name}")
    private String clusterName;

    /* (non-Javadoc)
     * @see org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer#customize(io.micrometer.core.instrument.MeterRegistry)
     */
    @Override
    public void customize(WavefrontMeterRegistry registry) {
        String hazelcastHostName = System.getProperty(MetricProviderConstants.NODE_ADDRESS);
        List<MetricProviderSpec> metricProviders =
                this.metricProviderRepository.findByMetricProviderType(MetricProviderType.WAVEFRONT);
        if (!CollectionUtils.isEmpty(metricProviders) && metricProviders.size() == 1) {
            String[] tags = CommonUtils
                    .getStringArrayFromMap(metricProviders.get(0).getWaveFrontConnectionProperties().getStaticTags());
            if (!ObjectUtils.isEmpty(tags) && tags.length >= 2) {
                registry.config().commonTags(tags);
                registry.config().commonTags("host", hazelcastHostName, "cluster", clusterName);
            }
        }

    }
}
