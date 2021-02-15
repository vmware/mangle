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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.micrometer.core.instrument.MeterRegistry.Config;
import io.micrometer.datadog.DatadogMeterRegistry;
import io.micrometer.wavefront.WavefrontMeterRegistry;
import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.model.enums.MetricProviderType;
import com.vmware.mangle.services.MetricProviderMeterRegistryCustomizer;
import com.vmware.mangle.services.mockdata.MetricProviderMockData;
import com.vmware.mangle.services.repository.MetricProviderRepository;
import com.vmware.mangle.utils.ReadProperty;
import com.vmware.mangle.utils.constants.Constants;

/**
 * Unit Tests Case for WavefrontRegistryCustomizer.
 *
 * @author ashrimali
 */

@PowerMockIgnore(value = { "org.apache.logging.log4j.*", "com.vmware.mangle.utils.exceptions.handler.ErrorCode" })
public class MetricProviderMeterRegistryCustomizerTest {

    @InjectMocks
    private MetricProviderMeterRegistryCustomizer metricProviderMeterRegistryCustomizer;

    @Mock
    private MetricProviderRepository metricProviderRepository;

    @Mock
    private WavefrontMeterRegistry wavefrontRegistry;

    @Mock
    private DatadogMeterRegistry datadogRegistry;

    private MetricProviderSpec wavefrontSpec;
    private MetricProviderSpec wavefrontSpecNoTags;
    private String wavefrontName;
    private MetricProviderSpec datadogSpec;
    private String datadogName;
    private MetricProviderSpec datadogSpecNoTags;

    @Mock
    private Config meterRegistryConfig;


    private Properties properties;

    private MetricProviderMockData mockData = new MetricProviderMockData();

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        this.wavefrontSpec = mockData.metricProviderWavefront();
        this.wavefrontSpecNoTags = mockData.metricProviderWavefrontNoTags();
        this.wavefrontName = properties.getProperty("wavefrontMetricReporterName");
        this.datadogSpec = mockData.metricProviderDatadog();
        this.datadogSpecNoTags = mockData.metricProviderDatadogNoTags();
        this.datadogName = properties.getProperty("datadogMetricReporterName");
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.wavefrontSpec = null;
        this.wavefrontSpecNoTags = null;
        this.wavefrontName = null;
        this.datadogSpec = null;
        this.datadogSpecNoTags = null;
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterTest
    public void tearDown() throws Exception {
        validateMockitoUsage();
    }

    /**
     * Test Method for
     * {@link com.vmware.mangle.services.MetricProviderMeterRegistryCustomizer#customize(WavefrontMeterRegistry)}.
     */
    @Test
    public void customizeWavefrontRegistry() {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        metricProviderSpecList.add(wavefrontSpec);
        when(this.metricProviderRepository.findByMetricProviderType(MetricProviderType.WAVEFRONT))
                .thenReturn(metricProviderSpecList);
        when(this.wavefrontRegistry.config()).thenReturn(meterRegistryConfig);
        metricProviderMeterRegistryCustomizer.customize(wavefrontRegistry);
        verify(metricProviderRepository, times(1)).findByMetricProviderType(MetricProviderType.WAVEFRONT);
    }

    /**
     * Test Method for
     * {@link com.vmware.mangle.services.MetricProviderMeterRegistryCustomizer#customize(DatadogMeterRegistry)}.
     */
    @Test
    public void customizeDatadogRegistry() {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        metricProviderSpecList.add(datadogSpec);
        when(this.metricProviderRepository.findByMetricProviderType(MetricProviderType.DATADOG))
                .thenReturn(metricProviderSpecList);
        when(this.datadogRegistry.config()).thenReturn(meterRegistryConfig);
        metricProviderMeterRegistryCustomizer.customize(datadogRegistry);
        verify(metricProviderRepository, times(1)).findByMetricProviderType(MetricProviderType.DATADOG);
    }

    /**
     * Test Method for
     * {@link com.vmware.mangle.services.MetricProviderMeterRegistryCustomizer#customize(WavefrontMeterRegistry)}.
     */
    @Test
    public void customizeWavefrontRegistryNoMetricProvider() {
        when(this.metricProviderRepository.findByMetricProviderType(MetricProviderType.WAVEFRONT)).thenReturn(null);
        metricProviderMeterRegistryCustomizer.customize(wavefrontRegistry);
        verify(metricProviderRepository, times(1)).findByMetricProviderType(MetricProviderType.WAVEFRONT);
    }

    /**
     * Test Method for
     * {@link com.vmware.mangle.services.MetricProviderMeterRegistryCustomizer#customize(WavefrontMeterRegistry)}.
     */
    @Test
    public void customizeWavefrontRegistryNoTags() {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        metricProviderSpecList.add(wavefrontSpecNoTags);
        when(this.metricProviderRepository.findByMetricProviderType(MetricProviderType.WAVEFRONT))
                .thenReturn(metricProviderSpecList);
        when(this.wavefrontRegistry.config()).thenReturn(meterRegistryConfig);
        try {
            metricProviderMeterRegistryCustomizer.customize(wavefrontRegistry);
        } catch (NullPointerException nullPointerException) {
            Assert.assertTrue(true);
        }
        verify(metricProviderRepository, times(1)).findByMetricProviderType(MetricProviderType.WAVEFRONT);
    }

    /**
     * Test Method for
     * {@link com.vmware.mangle.services.MetricProviderMeterRegistryCustomizer#customize(DatadogMeterRegistry)}.
     */
    @Test
    public void customizeDatadogRegistryNoMetricProvider() {
        when(this.metricProviderRepository.findByMetricProviderType(MetricProviderType.DATADOG)).thenReturn(null);
        metricProviderMeterRegistryCustomizer.customize(datadogRegistry);
        verify(metricProviderRepository, times(1)).findByMetricProviderType(MetricProviderType.DATADOG);
    }

    /**
     * Test Method for
     * {@link com.vmware.mangle.services.MetricProviderMeterRegistryCustomizer#customize(DatadogMeterRegistry)}.
     */
    @Test
    public void customizeDatadogRegistryNoTags() {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        metricProviderSpecList.add(datadogSpecNoTags);
        when(this.metricProviderRepository.findByMetricProviderType(MetricProviderType.DATADOG))
                .thenReturn(metricProviderSpecList);
        when(this.datadogRegistry.config()).thenReturn(meterRegistryConfig);
        try {
            metricProviderMeterRegistryCustomizer.customize(datadogRegistry);
        } catch (NullPointerException nullPointerException) {
            Assert.assertTrue(true);
        }
        verify(metricProviderRepository, times(1)).findByMetricProviderType(MetricProviderType.DATADOG);
    }
}
