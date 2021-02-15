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

import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.datadog.DatadogMeterRegistry;
import io.micrometer.wavefront.WavefrontMeterRegistry;
import lombok.extern.log4j.Log4j2;
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
 * @author ashrimali
 *
 */

@Configuration
@DependsOn("hazelcastInstance")
@Log4j2
public class MetricProviderMeterRegistryCustomizer implements MeterRegistryCustomizer<StepMeterRegistry> {
    @Autowired
    private MetricProviderRepository metricProviderRepository;

    @Value("${hazelcast.config.cluster.name}")
    private String clusterName;

    /* (non-Javadoc)
     * @see org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer#customize(io.micrometer.core.instrument.MeterRegistry)
     */
    @Override
    public void customize(StepMeterRegistry registry) {
        String hazelcastHostName = System.getProperty(MetricProviderConstants.NODE_ADDRESS);
        String[] tags = {};
        if (registry instanceof DatadogMeterRegistry) {
            tags = getDatadogTags(this.metricProviderRepository.findByMetricProviderType(MetricProviderType.DATADOG));
        } else if (registry instanceof WavefrontMeterRegistry) {
            tags = getWavefrontTags(
                    this.metricProviderRepository.findByMetricProviderType(MetricProviderType.WAVEFRONT));
        }
        if (!ObjectUtils.isEmpty(tags) && tags.length >= 1) {
            registry.config().commonTags(tags);
            registry.config().commonTags("host", hazelcastHostName, "cluster", clusterName);
        }

    }

    /**
     * Method to retrieve datadog tags
     * @param metricProviders
     */
    private String[] getDatadogTags(List<MetricProviderSpec> metricProviders) {
        String[] tags = {};
        if (!CollectionUtils.isEmpty(metricProviders) && metricProviders.size() == 1) {
            try {
                tags = CommonUtils
                        .getStringArrayFromMap(metricProviders.get(0).getDatadogConnectionProperties().getStaticTags());
            } catch (NullPointerException nullPointerException) {
                log.debug("No tags are associated with metric provider");
                log.catching(nullPointerException);
            }
        } else {
            log.debug("No Metric Provider Found");
        }
        return tags;
    }

    /**
     * Method to retrieve wavefront tags
     * @param metricProviders
     */
    private String[] getWavefrontTags(List<MetricProviderSpec> metricProviders) {
        String[] tags = {};
        if (!CollectionUtils.isEmpty(metricProviders) && metricProviders.size() == 1) {
            try {
                return CommonUtils.getStringArrayFromMap(
                        metricProviders.get(0).getWaveFrontConnectionProperties().getStaticTags());
            } catch (NullPointerException nullPointerException) {
                log.debug("No tags are associated with metric provider");
                log.catching(nullPointerException);
            }
        } else {
            log.debug("No Metric Provider Found");
        }
        return tags;
    }
}
