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

import java.util.Optional;

import javax.annotation.PostConstruct;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.MangleAdminConfigurationSpec;
import com.vmware.mangle.services.hazelcast.HazelcastClusterSyncAware;
import com.vmware.mangle.services.repository.AdminConfigurationRepository;
import com.vmware.mangle.utils.constants.MetricProviderConstants;

/**
 * @author hkilari
 * @author kumargautam
 */

@Component
@Log4j2
public class MangleMetricsConfiguration implements HazelcastClusterSyncAware {

    @Getter
    private Boolean metricsEnabled = false;
    @Getter
    private String activeMetricProvider;
    private AdminConfigurationRepository adminConfigurationRepository;

    @Autowired
    public MangleMetricsConfiguration(AdminConfigurationRepository adminConfigurationRepository) {
        this.adminConfigurationRepository = adminConfigurationRepository;
    }

    public void setActiveMetricProvider(String activeMetricProvider) {
        MangleAdminConfigurationSpec adminConfigSpec = new MangleAdminConfigurationSpec();
        adminConfigSpec.setPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        adminConfigSpec.setPropertyValue(activeMetricProvider);
        adminConfigurationRepository.save(adminConfigSpec);
        this.activeMetricProvider = activeMetricProvider;
        String objectIdentifier = activeMetricProvider + "#" + metricsEnabled;
        triggerMultiNodeResync(objectIdentifier);
    }

    public void setMetricsEnabled(Boolean metricsEnabled) {
        MangleAdminConfigurationSpec adminConfigSpec = new MangleAdminConfigurationSpec();
        adminConfigSpec.setPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        adminConfigSpec.setPropertyValue(metricsEnabled.toString());
        adminConfigurationRepository.save(adminConfigSpec);
        this.metricsEnabled = metricsEnabled;
        String objectIdentifier = activeMetricProvider + "#" + metricsEnabled;
        triggerMultiNodeResync(objectIdentifier);
    }

    @Override
    public void resync(String objectIdentifier) {
        log.debug("Received request to resync metrics enablement...");
        String[] input = objectIdentifier.split("#");
        if (input.length == 2) {
            this.activeMetricProvider = input[0];
            this.metricsEnabled = Boolean.valueOf(input[1]);
        }
    }

    @PostConstruct
    public void initializeMetricConfiguration() {
        Optional<MangleAdminConfigurationSpec> sendingMetricProperty =
                adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        if (sendingMetricProperty.isPresent()) {
            this.metricsEnabled = (Boolean.valueOf(sendingMetricProperty.get().getPropertyValue()));
        }
        Optional<MangleAdminConfigurationSpec> activeMetricProviderProperty =
                adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        if (activeMetricProviderProperty.isPresent()) {
            this.activeMetricProvider = (activeMetricProviderProperty.get().getPropertyValue());
        }
    }
}