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

package com.vmware.mangle.unittest.services.config;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Optional;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.MangleAdminConfigurationSpec;
import com.vmware.mangle.services.config.MangleMetricsConfiguration;
import com.vmware.mangle.services.repository.AdminConfigurationRepository;
import com.vmware.mangle.utils.constants.MetricProviderConstants;

/**
 * Unit test cases for MangleMetricsConfiguration.
 *
 * @author kumargautam
 */
public class MangleMetricsConfigurationTest {

    @Mock
    private AdminConfigurationRepository adminConfigurationRepository;

    private MangleMetricsConfiguration mangleMetricsConfiguration;

    @BeforeMethod
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        mangleMetricsConfiguration = spy(new MangleMetricsConfiguration(adminConfigurationRepository));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.config.MangleMetricsConfiguration#initializeMetricConfiguration()}.
     */
    @Test
    public void testInitializeMetricConfiguration() {
        MangleAdminConfigurationSpec adminConfigurationSpec = new MangleAdminConfigurationSpec();
        adminConfigurationSpec.setPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        adminConfigurationSpec.setPropertyValue("false");
        when(adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(Optional.of(adminConfigurationSpec));
        MangleAdminConfigurationSpec adminConfigurationSpec1 = new MangleAdminConfigurationSpec();
        adminConfigurationSpec.setPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        String name = "Prometheus_test";
        adminConfigurationSpec1.setPropertyValue(name);
        when(adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(Optional.of(adminConfigurationSpec1));
        mangleMetricsConfiguration.initializeMetricConfiguration();
        assertEquals(mangleMetricsConfiguration.getActiveMetricProvider(), name);
        verify(adminConfigurationRepository, times(2)).findByPropertyName(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.config.MangleMetricsConfiguration#setActiveMetricProvider(java.lang.String)}.
     */
    @Test
    public void testSetActiveMetricProvider() {
        String name = "test";
        doNothing().when(mangleMetricsConfiguration).triggerMultiNodeResync(any());
        mangleMetricsConfiguration.setActiveMetricProvider(name);
        assertEquals(mangleMetricsConfiguration.getActiveMetricProvider(), name);
        verify(mangleMetricsConfiguration, times(1)).triggerMultiNodeResync(any());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.config.MangleMetricsConfiguration#setMetricsEnabled(java.lang.Boolean)}.
     */
    @Test
    public void testSetMetricsEnabled() {
        Boolean flag = true;
        doNothing().when(mangleMetricsConfiguration).triggerMultiNodeResync(any());
        mangleMetricsConfiguration.setMetricsEnabled(flag);
        assertEquals(mangleMetricsConfiguration.getMetricsEnabled(), flag);
        verify(mangleMetricsConfiguration, times(1)).triggerMultiNodeResync(any());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.config.MangleMetricsConfiguration#resync(java.lang.String)}.
     */
    @Test
    public void testResync() {
        String objectIdentifier = mangleMetricsConfiguration.getActiveMetricProvider() + "#" + "false";
        mangleMetricsConfiguration.resync(objectIdentifier);
        assertEquals(mangleMetricsConfiguration.getMetricsEnabled(), Boolean.FALSE);
    }
}