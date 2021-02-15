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

package com.vmware.mangle.unittest.services.service;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Optional;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.services.PrometheusScrapeEndpointService;
import com.vmware.mangle.services.config.MangleMetricsConfiguration;
import com.vmware.mangle.services.mockdata.MetricProviderMockData;
import com.vmware.mangle.services.repository.MetricProviderRepository;

/**
 * Unit test cases for PrometheusScrapeEndpointService.
 *
 * @author kumargautam
 */
public class PrometheusScrapeEndpointServiceTest {

    @Mock
    private PrometheusScrapeEndpoint prometheusScrapeEndpoint;
    @Mock
    private MangleMetricsConfiguration enableMetrics;
    @Mock
    private MetricProviderRepository metricProviderRepository;
    private PrometheusScrapeEndpointService prometheusScrapeEndpointService;
    private MetricProviderMockData mockData = new MetricProviderMockData();
    private String metricData = "cpu-usage=40%";

    @BeforeMethod
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        this.prometheusScrapeEndpointService = spy(
                new PrometheusScrapeEndpointService(prometheusScrapeEndpoint, enableMetrics, metricProviderRepository));
    }

    /**
     * Test method for {@link com.vmware.mangle.services.PrometheusScrapeEndpointService#scrape()}.
     */
    @Test
    public void testScrape() {
        MetricProviderSpec metricProviderSpec = mockData.getPrometheusMetricProvider();
        when(enableMetrics.getMetricsEnabled()).thenReturn(true);
        when(enableMetrics.getActiveMetricProvider()).thenReturn(metricProviderSpec.getName());
        when(metricProviderRepository.findByName(anyString())).thenReturn(Optional.of(metricProviderSpec));
        when(prometheusScrapeEndpoint.scrape()).thenReturn(metricData);
        assertEquals(prometheusScrapeEndpointService.scrape(), metricData);
        verify(enableMetrics, times(2)).getActiveMetricProvider();
        verify(enableMetrics, times(1)).getMetricsEnabled();
        verify(metricProviderRepository, times(1)).findByName(anyString());
        verify(prometheusScrapeEndpoint, times(1)).scrape();
    }

    /**
     * Test method for {@link com.vmware.mangle.services.PrometheusScrapeEndpointService#scrape()}.
     */
    @Test
    public void testScrapeForDifferentMetricProviderEnabled() {
        MetricProviderSpec metricProviderSpec = mockData.metricProviderWavefront();
        when(enableMetrics.getMetricsEnabled()).thenReturn(true);
        when(enableMetrics.getActiveMetricProvider()).thenReturn(metricProviderSpec.getName());
        when(metricProviderRepository.findByName(anyString())).thenReturn(Optional.of(metricProviderSpec));
        when(prometheusScrapeEndpoint.scrape()).thenReturn(metricData);
        assertNotEquals(prometheusScrapeEndpointService.scrape(), metricData);
        verify(enableMetrics, times(2)).getActiveMetricProvider();
        verify(enableMetrics, times(1)).getMetricsEnabled();
        verify(metricProviderRepository, times(1)).findByName(anyString());
        verify(prometheusScrapeEndpoint, times(0)).scrape();
    }

    /**
     * Test method for {@link com.vmware.mangle.services.PrometheusScrapeEndpointService#scrape()}.
     */
    @Test
    public void testScrapeForNotPrometheusEnabled() {
        when(enableMetrics.getMetricsEnabled()).thenReturn(false);
        assertNotEquals(prometheusScrapeEndpointService.scrape(), metricData);
        verify(enableMetrics, times(1)).getMetricsEnabled();
    }

    /**
     * Test method for {@link com.vmware.mangle.services.PrometheusScrapeEndpointService#scrape()}.
     */
    @Test
    public void testScrapeForNoMetricsEnabled() {
        when(enableMetrics.getMetricsEnabled()).thenReturn(true);
        when(enableMetrics.getActiveMetricProvider()).thenReturn(null);
        assertNotEquals(prometheusScrapeEndpointService.scrape(), metricData);
        verify(enableMetrics, times(1)).getMetricsEnabled();
        verify(enableMetrics, times(1)).getActiveMetricProvider();
    }
}
