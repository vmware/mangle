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

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import io.micrometer.core.instrument.MeterRegistry.Config;
import io.micrometer.core.instrument.util.NamedThreadFactory;
import io.micrometer.datadog.DatadogMeterRegistry;
import io.micrometer.dynatrace.DynatraceMeterRegistry;
import io.micrometer.wavefront.WavefrontMeterRegistry;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.MangleAdminConfigurationSpec;
import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.model.enums.MetricProviderType;
import com.vmware.mangle.services.MetricProviderService;
import com.vmware.mangle.services.config.MangleDatadogConfig;
import com.vmware.mangle.services.config.MangleDynatraceConfig;
import com.vmware.mangle.services.config.MangleMetricsConfiguration;
import com.vmware.mangle.services.config.MangleWavefrontConfig;
import com.vmware.mangle.services.mockdata.MetricProviderMockData;
import com.vmware.mangle.services.mockdata.MockDataConstants;
import com.vmware.mangle.services.repository.AdminConfigurationRepository;
import com.vmware.mangle.services.repository.MetricProviderRepository;
import com.vmware.mangle.task.framework.metric.providers.MetricProviderClientFactory;
import com.vmware.mangle.utils.ReadProperty;
import com.vmware.mangle.utils.clients.metricprovider.MetricProviderClient;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.MetricProviderConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit Tests Case for MetricProviderService.
 *
 * @author ashrimali
 * @author dbhat
 */

@PrepareForTest({ MetricProviderClientFactory.class })
@PowerMockIgnore(value = { "org.apache.logging.log4j.*", "com.vmware.mangle.utils.exceptions.handler.ErrorCode" })
public class MetricProviderServiceTest extends PowerMockTestCase {

    @InjectMocks
    private MetricProviderService metricProviderService;

    @Mock
    private MetricProviderRepository metricProviderRepository;

    @Mock
    private MetricProviderClientFactory mangleMetricProviderClientFactory;

    @Mock
    private MetricProviderClient metricProviderClient;

    @Mock
    private DatadogMeterRegistry datadogMeterRegistry;

    @Mock
    private MangleDatadogConfig mangleDatadogConfig;

    @Mock
    private MangleWavefrontConfig mangelWavefrontConfig;

    @Mock
    private MangleDynatraceConfig mangleDynatraceConfig;

    @Mock
    private WavefrontMeterRegistry wavefrontMeterRegistry;

    @Mock
    private DynatraceMeterRegistry dynatraceMeterRegistry;

    @Mock
    private AdminConfigurationRepository adminConfigurationRepository;

    @Mock
    private Config meterConfig;

    @Mock
    private MangleMetricsConfiguration mangleMetricsConfiguration;

    private MetricProviderMockData mockData = new MetricProviderMockData();

    private MetricProviderSpec datadogSpec;

    private MetricProviderSpec wavefrontSpec;

    private MetricProviderSpec dynatraceSpec;

    private Properties properties;

    private String wavefrontName;

    private String datadogName;

    private String dynatraceName;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        datadogSpec = mockData.metricProviderDatadog();
        wavefrontSpec = mockData.metricProviderWavefront();
        dynatraceSpec = mockData.getMetricProviderSpecForDynatrace();
        wavefrontName = properties.getProperty("wavefrontMetricReporterName");
        datadogName = properties.getProperty("datadogMetricReporterName");
        dynatraceName = MockDataConstants.DYNATRACE_INSTANCE_NAME;
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.mockData = null;
        this.datadogSpec = null;
        this.wavefrontSpec = null;
        this.dynatraceSpec = null;
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterTest
    public void tearDown() throws Exception {
        validateMockitoUsage();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#getAllMetricProviders()}.
     */
    @Test
    public void testGetAllMetricProviders() {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        metricProviderSpecList.add(wavefrontSpec);
        metricProviderSpecList.add(datadogSpec);
        metricProviderSpecList.add(dynatraceSpec);
        when(metricProviderRepository.findAll()).thenReturn(metricProviderSpecList);
        Assert.assertEquals(metricProviderService.getAllMetricProviders().size(), 3);
        verify(metricProviderRepository, times(1)).findAll();
    }


    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#getMetricProviderByType(MetricProviderType)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetMetricProviderByTypeWavefront() throws MangleException {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        metricProviderSpecList.add(wavefrontSpec);
        when(this.metricProviderRepository.findByMetricProviderType(MetricProviderType.WAVEFRONT))
                .thenReturn(metricProviderSpecList);
        Assert.assertEquals(this.metricProviderService.getMetricProviderByType(MetricProviderType.WAVEFRONT).size(), 1);
        verify(metricProviderRepository, times(1)).findByMetricProviderType(MetricProviderType.WAVEFRONT);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#getMetricProviderByType(MetricProviderType)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetMetricProviderByNull() throws MangleException {
        when(this.metricProviderRepository.findByMetricProviderType(null)).thenReturn(null);
        try {
            this.metricProviderService.getMetricProviderByType(null);
        } catch (MangleException exception) {
            Assert.assertTrue(true);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#getMetricProviderByType(MetricProviderType)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetMetricProviderByTypeDatadog() throws MangleException {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        metricProviderSpecList.add(datadogSpec);
        when(this.metricProviderRepository.findByMetricProviderType(MetricProviderType.DATADOG))
                .thenReturn(metricProviderSpecList);
        Assert.assertEquals(this.metricProviderService.getMetricProviderByType(MetricProviderType.DATADOG).size(), 1);
        verify(metricProviderRepository, times(1)).findByMetricProviderType(MetricProviderType.DATADOG);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#getMetricProviderByType(MetricProviderType)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetMetricProviderByTypeDynatrace() throws MangleException {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        metricProviderSpecList.add(dynatraceSpec);
        when(this.metricProviderRepository.findByMetricProviderType(MetricProviderType.DYNATRACE))
                .thenReturn(metricProviderSpecList);
        Assert.assertEquals(this.metricProviderService.getMetricProviderByType(MetricProviderType.DYNATRACE).size(), 1);
        verify(metricProviderRepository, times(1)).findByMetricProviderType(MetricProviderType.DYNATRACE);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#getMetricProviderByType(MetricProviderType)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetMetricProviderFailure() throws MangleException {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        when(this.metricProviderRepository.findByMetricProviderType(MetricProviderType.WAVEFRONT))
                .thenReturn(metricProviderSpecList);
        Assert.assertEquals(this.metricProviderService.getMetricProviderByType(MetricProviderType.WAVEFRONT).size(), 0);
        verify(metricProviderRepository, times(1)).findByMetricProviderType(MetricProviderType.WAVEFRONT);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#getActiveMetricProvider()}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testGetActiveMetricProvider() throws MangleException {
        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();

        when(this.mangleMetricsConfiguration.getActiveMetricProvider())
                .thenReturn(activeMetricProvider.getPropertyValue());

        Optional<MetricProviderSpec> metricProviderSpecOptional = Optional.of(wavefrontSpec);
        when(metricProviderRepository.findByName(activeMetricProvider.getPropertyValue()))
                .thenReturn(metricProviderSpecOptional);

        Assert.assertEquals(this.metricProviderService.getActiveMetricProvider().getName(),
                properties.getProperty(activeMetricProvider.getPropertyValue()));
        verify(metricProviderRepository, times(1)).findByName(activeMetricProvider.getPropertyValue());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#getActiveMetricProvider()}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testGetActiveMetricProviderNotPresent() throws MangleException {
        MangleAdminConfigurationSpec activeMetricProvider = new MangleAdminConfigurationSpec();
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(Optional.of(activeMetricProvider).empty());
        try {
            this.metricProviderService.getActiveMetricProvider();
        } catch (MangleRuntimeException exception) {
            Assert.assertTrue(true);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#getActiveMetricProvider()}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testGetActiveMetricProviderFailure() throws MangleException {
        Optional<MangleAdminConfigurationSpec> value = Optional.of(new MangleAdminConfigurationSpec());
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(value);
        try {
            this.metricProviderService.getActiveMetricProvider();
        } catch (MangleRuntimeException exception) {
            Assert.assertTrue(true);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#getMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testGetMetricProviderByName() throws MangleException {
        MetricProviderSpec metricProviderSpec = wavefrontSpec;
        Optional<MetricProviderSpec> optional = Optional.of(metricProviderSpec);
        when(metricProviderRepository.findByName(wavefrontName)).thenReturn(optional);
        Assert.assertEquals(this.metricProviderService.getMetricProviderByName(wavefrontName), metricProviderSpec);
        verify(this.metricProviderRepository, times(1)).findByName(wavefrontName);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#getMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testGetMetricProviderByNameNotPresent() throws MangleException {
        MetricProviderSpec metricProviderSpec = new MetricProviderSpec();
        Optional<MetricProviderSpec> optional = Optional.of(metricProviderSpec);
        when(metricProviderRepository.findByName(wavefrontName)).thenReturn(optional.empty());
        try {
            this.metricProviderService.getMetricProviderByName(wavefrontName);
        } catch (MangleRuntimeException exception) {
            Assert.assertTrue(true);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#getMetricProviderByName(String)}.
     *
     */
    @Test
    public void testGetMetricProviderByNameFailureNoData() {
        try {
            this.metricProviderService.getMetricProviderByName("someName");
        } catch (NullPointerException nullPointerException) {
            Assert.assertTrue(true);
        } catch (MangleException exception) {
        }
        verify(this.metricProviderRepository, times(1)).findByName("someName");
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#getMetricProviderByName(String)}.
     *
     */
    @Test
    public void testGetMetricProviderByNameFailureNullName() {
        try {
            this.metricProviderService.getMetricProviderByName(null);
        } catch (NullPointerException nullPointerException) {
            Assert.assertTrue(true);
        } catch (MangleException exception) {
        }
        verify(this.metricProviderRepository, times(0)).findByName("someName");
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#addMetricProvider(MetricProviderSpec)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testAddMetricProvider() throws MangleException {
        MetricProviderSpec metricProviderSpec = wavefrontSpec;
        when(metricProviderRepository.save(metricProviderSpec)).thenReturn(metricProviderSpec);
        Assert.assertEquals(this.metricProviderService.addMetricProvider(metricProviderSpec), metricProviderSpec);
        verify(this.metricProviderRepository, times(1)).save(metricProviderSpec);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#addMetricProvider(MetricProviderSpec)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testAddMetricProviderWithNull() throws MangleException {
        try {
            this.metricProviderService.addMetricProvider(null);

        } catch (MangleException exception) {
            Assert.assertTrue(true);
        }
        verify(this.metricProviderRepository, times(0)).save(null);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#addMetricProvider(MetricProviderSpec)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testAddMetricProviderWithoutRequiredField() throws MangleException {
        MetricProviderSpec metricProviderSpec = new MetricProviderSpec();
        metricProviderSpec.setName("");
        Assert.assertEquals(this.metricProviderService.addMetricProvider(metricProviderSpec), null);
        verify(this.metricProviderRepository, times(1)).save(metricProviderSpec);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#updateMetricProviderById(String,MetricProviderSpec)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testUpdateMetricProviderById() throws MangleException {
        String id = properties.getProperty("wavefrontId");
        MetricProviderSpec metricProviderSpecOrig = wavefrontSpec;
        Optional<MetricProviderSpec> value = Optional.of(metricProviderSpecOrig);
        when(this.metricProviderRepository.findById(id)).thenReturn(value);

        MetricProviderSpec metricProviderSpecToUpdate = wavefrontSpec;
        metricProviderSpecToUpdate.setName("updatedName");
        when(metricProviderRepository.save(metricProviderSpecToUpdate)).thenReturn(metricProviderSpecToUpdate);
        Assert.assertEquals(
                this.metricProviderService.updateMetricProviderById(id, metricProviderSpecToUpdate).getName(),
                "updatedName");
        verify(this.metricProviderRepository, times(1)).save(metricProviderSpecToUpdate);
        verify(this.metricProviderRepository, times(1)).findById(id);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#updateMetricProviderById(String,MetricProviderSpec)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testUpdateMetricProviderByIdEmpty() throws MangleException {
        String id = properties.getProperty("wavefrontId");

        MetricProviderSpec metricProviderSpec = new MetricProviderSpec();
        Optional<MetricProviderSpec> optional = Optional.of(metricProviderSpec);
        when(metricProviderRepository.findById(id)).thenReturn(optional.empty());
        try {
            this.metricProviderService.updateMetricProviderById(id, metricProviderSpec);
        } catch (MangleRuntimeException exception) {
            Assert.assertTrue(true);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#updateMetricProviderByName(String,MetricProviderSpec)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testUpdateMetricProviderByName() throws MangleException {
        MetricProviderSpec metricProviderSpecOrig = wavefrontSpec;
        Optional<MetricProviderSpec> value = Optional.of(metricProviderSpecOrig);
        when(this.metricProviderRepository.findByName(wavefrontName)).thenReturn(value);

        MetricProviderSpec metricProviderSpecToUpdate = wavefrontSpec;
        metricProviderSpecToUpdate.setName("updatedName");
        when(metricProviderRepository.save(metricProviderSpecToUpdate)).thenReturn(metricProviderSpecToUpdate);
        Assert.assertEquals(this.metricProviderService
                .updateMetricProviderByName(wavefrontName, metricProviderSpecToUpdate).getName(), "updatedName");
        verify(this.metricProviderRepository, times(1)).save(metricProviderSpecToUpdate);
        verify(this.metricProviderRepository, times(1)).findByName(wavefrontName);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#updateMetricProviderById(String,MetricProviderSpec)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testUpdateMetricProviderByNameEmpty() throws MangleException {
        MetricProviderSpec metricProviderSpec = new MetricProviderSpec();
        Optional<MetricProviderSpec> optional = Optional.of(metricProviderSpec);
        when(metricProviderRepository.findById("someName")).thenReturn(optional.empty());
        try {
            this.metricProviderService.updateMetricProviderById("someName", metricProviderSpec);
        } catch (MangleRuntimeException exception) {
            Assert.assertTrue(true);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#updateMetricProviderById(String,MetricProviderSpec)}.
     *
     */
    @Test
    public void testUpdateMetricProviderByIdNull() {

        try {
            this.metricProviderService.updateMetricProviderById(null, null);
        } catch (MangleException e) {
            Assert.assertTrue(true);
        }

        verify(this.metricProviderRepository, times(0)).save(null);
        verify(this.metricProviderRepository, times(0)).findById(null);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#updateMetricProviderByName(String,MetricProviderSpec)}.
     *
     */
    @Test
    public void testUpdateMetricProviderByNameNull() {
        try {
            this.metricProviderService.updateMetricProviderByName(null, null);
        } catch (MangleException e) {
            Assert.assertTrue(true);
        }

        verify(this.metricProviderRepository, times(0)).save(null);
        verify(this.metricProviderRepository, times(0)).findByName(null);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#testConnectionMetricProvider(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testTestConnectionMetricProviderByName() throws MangleException {
        MetricProviderSpec metricProviderSpec = wavefrontSpec;
        Optional<MetricProviderSpec> value = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName(wavefrontName)).thenReturn(value);
        when(this.mangleMetricProviderClientFactory.getMetricProviderClient(metricProviderSpec))
                .thenReturn(metricProviderClient);
        when(this.metricProviderClient.testConnection()).thenReturn(true);
        Assert.assertEquals(this.metricProviderService.testConnectionMetricProvider(wavefrontSpec), true);
        verify(this.mangleMetricProviderClientFactory, times(1)).getMetricProviderClient(metricProviderSpec);
        verify(this.metricProviderClient, times(1)).testConnection();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#testConnectionMetricProvider(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testTestConnectionMetricProviderEmpty() throws MangleException {
        MetricProviderSpec metricProviderSpec = new MetricProviderSpec();
        Optional<MetricProviderSpec> optional = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName(wavefrontName)).thenReturn(optional.empty());
        when(this.mangleMetricProviderClientFactory.getMetricProviderClient(metricProviderSpec))
                .thenReturn(metricProviderClient);
        when(this.metricProviderClient.testConnection()).thenReturn(true);
        boolean result = false;
        try {
            result = this.metricProviderService.testConnectionMetricProvider(wavefrontSpec);
        } catch (NullPointerException exception) {
            Assert.assertEquals(result, false);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#testConnectionMetricProvider(MetricProviderSpec)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testTestConnectionMetricProviderBySpec() throws MangleException {

        MetricProviderSpec metricProviderSpec = wavefrontSpec;
        when(this.mangleMetricProviderClientFactory.getMetricProviderClient(metricProviderSpec))
                .thenReturn(metricProviderClient);
        when(this.metricProviderClient.testConnection()).thenReturn(true);
        Assert.assertEquals(this.metricProviderService.testConnectionMetricProvider(metricProviderSpec), true);
        verify(this.mangleMetricProviderClientFactory, times(1)).getMetricProviderClient(metricProviderSpec);
        verify(this.metricProviderClient, times(1)).testConnection();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#testConnectionMetricProvider(MetricProviderSpec)}.
     *
     * @throws MangleException
     *
     */
    @Test(expectedExceptions = MangleException.class)
    public void testTestConnectionMetricProviderFail() throws MangleException {

        MetricProviderSpec metricProviderSpec = wavefrontSpec;
        when(this.mangleMetricProviderClientFactory.getMetricProviderClient(metricProviderSpec))
                .thenReturn(metricProviderClient);
        when(this.metricProviderClient.testConnection()).thenReturn(false);
        try {
            this.metricProviderService.testConnectionMetricProvider(metricProviderSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.TEST_CONNECTION_FAILED);
            verify(this.mangleMetricProviderClientFactory, times(1)).getMetricProviderClient(metricProviderSpec);
            verify(this.metricProviderClient, times(1)).testConnection();
            throw e;
        }

    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#testConnectionMetricProvider(MetricProviderSpec)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testTestConnectionMetricProviderFailFieldEmpty() throws MangleException {

        MetricProviderSpec metricProviderSpec = wavefrontSpec;
        metricProviderSpec.setName("");
        when(this.metricProviderClient.testConnection()).thenReturn(false);
        try {
            this.metricProviderService.testConnectionMetricProvider(metricProviderSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            verify(this.mangleMetricProviderClientFactory, times(0)).getMetricProviderClient(metricProviderSpec);
            verify(this.metricProviderClient, times(0)).testConnection();
        }

    }


    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#enableMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testEnableMetricProviderByNameStatusWavefront() throws MangleException {
        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", wavefrontSpec);
        MetricProviderSpec metricProviderSpec = wavefrontSpec;
        Optional<MetricProviderSpec> valueMetricSpec = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName(anyString())).thenReturn(valueMetricSpec);

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> valueAdminSpec = Optional.of(activeMetricProvider);
        when(this.mangleMetricsConfiguration.getActiveMetricProvider())
                .thenReturn(activeMetricProvider.getPropertyValue());

        when(this.adminConfigurationRepository.save(activeMetricProvider)).thenReturn(activeMetricProvider);

        MangleAdminConfigurationSpec sendMetricSpec = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> sendMetrics = Optional.of(sendMetricSpec);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(sendMetrics);

        Mockito.doNothing().when(this.wavefrontMeterRegistry)
                .start(new NamedThreadFactory("wavefront-metrics-publisher"));
        when(this.wavefrontMeterRegistry.config()).thenReturn(this.meterConfig);
        when(this.wavefrontMeterRegistry.config().commonTags("")).thenReturn(this.meterConfig);

        try {
            this.metricProviderService.enableMetricProviderByName(wavefrontName);
        } catch (MangleRuntimeException mangleRuntimeException) {
            Assert.assertTrue(true);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#enableMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testEnableMetricProviderByNameNullActive() throws MangleException {

        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", null);
        MetricProviderSpec metricProviderSpec = wavefrontSpec;
        Optional<MetricProviderSpec> valueMetricSpec = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName(anyString())).thenReturn(valueMetricSpec);

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> valueAdminSpec = Optional.of(activeMetricProvider);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(valueAdminSpec);

        when(this.adminConfigurationRepository.save(activeMetricProvider)).thenReturn(activeMetricProvider);

        MangleAdminConfigurationSpec sendMetricSpec = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> sendMetrics = Optional.of(sendMetricSpec);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(sendMetrics);

        Mockito.doNothing().when(this.wavefrontMeterRegistry)
                .start(new NamedThreadFactory("wavefront-metrics-publisher"));
        when(this.wavefrontMeterRegistry.config()).thenReturn(this.meterConfig);
        when(this.wavefrontMeterRegistry.config().commonTags("")).thenReturn(this.meterConfig);
        try {
            this.metricProviderService.enableMetricProviderByName(wavefrontName);
        } catch (MangleRuntimeException mangleRuntimeException) {
            Assert.assertTrue(true);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#enableMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testEnableMetricProviderByNameEmpty() throws MangleException {
        MetricProviderSpec metricProviderSpec = new MetricProviderSpec();
        Optional<MetricProviderSpec> optional = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName(wavefrontName)).thenReturn(optional.empty());
        try {
            this.metricProviderService.enableMetricProviderByName(wavefrontName);
        } catch (MangleRuntimeException exception) {
            Assert.assertTrue(true);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#enableMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void testEnableMetricProviderByPropertyEmpty() throws MangleException {

        MetricProviderSpec metricProviderSpec = wavefrontSpec;
        Optional<MetricProviderSpec> valueMetricSpec = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName(wavefrontName)).thenReturn(valueMetricSpec);

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> valueAdminSpec = Optional.of(activeMetricProvider);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(valueAdminSpec);

        when(this.adminConfigurationRepository.save(activeMetricProvider)).thenReturn(activeMetricProvider);
        Assert.assertEquals(this.metricProviderService.enableMetricProviderByName(wavefrontName).getPropertyName(),
                null);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#enableMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void testEnableMetricProviderByNameStatusDatadog() throws MangleException {
        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", datadogSpec);

        MetricProviderSpec metricProviderSpec = datadogSpec;
        Optional<MetricProviderSpec> valueMetricSpec = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName(datadogName)).thenReturn(valueMetricSpec);

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        when(this.adminConfigurationRepository.save(activeMetricProvider)).thenReturn(activeMetricProvider);
        MangleAdminConfigurationSpec sendMetricSpec = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> sendMetrics = Optional.of(sendMetricSpec);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(sendMetrics);
        String name = datadogName;
        try {
            name = this.metricProviderService.enableMetricProviderByName(datadogName).getPropertyValue();
        } catch (NullPointerException nullPointerException) {
            Assert.assertEquals(name, datadogName);
            verify(this.metricProviderRepository, times(1)).findByName(datadogName);
            throw nullPointerException;
        }

    }


    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#enableMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void testEnableMetricProviderByNameStatusDatadogSuccessful() throws MangleException {
        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", datadogSpec);
        MetricProviderSpec metricProviderSpec = datadogSpec;
        MetricProviderService providerService = spy(metricProviderService);
        Optional<MetricProviderSpec> valueMetricSpec = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName(datadogName)).thenReturn(valueMetricSpec);

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        when(this.adminConfigurationRepository.save(activeMetricProvider)).thenReturn(activeMetricProvider);

        MangleAdminConfigurationSpec sendMetricSpec = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> sendMetrics = Optional.of(sendMetricSpec);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(sendMetrics);
        doReturn(null).when(providerService).getActiveMetricProvider();
        String name = datadogName;
        try {
            name = providerService.enableMetricProviderByName(datadogName).getPropertyValue();
        } catch (NullPointerException nullPointerException) {
            Assert.assertEquals(name, datadogName);
            verify(this.metricProviderRepository, times(1)).findByName(datadogName);
            throw nullPointerException;
        }

    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#enableMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testEnableMetricProviderByNameStatusFalse() throws MangleException {
        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", wavefrontSpec);

        MetricProviderSpec metricProviderSpec = wavefrontSpec;
        Optional<MetricProviderSpec> valueMetricSpec = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName(anyString())).thenReturn(valueMetricSpec);

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> valueAdminSpec = Optional.of(activeMetricProvider);
        when(this.mangleMetricsConfiguration.getActiveMetricProvider())
                .thenReturn(activeMetricProvider.getPropertyValue());

        when(this.adminConfigurationRepository.save(activeMetricProvider)).thenReturn(activeMetricProvider);

        MangleAdminConfigurationSpec sendMetricSpec = mockData.getAdminPropertyForSendingMetricStatusFalse();
        Optional<MangleAdminConfigurationSpec> sendMetrics = Optional.of(sendMetricSpec);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(sendMetrics);
        try {
            this.metricProviderService.enableMetricProviderByName(wavefrontName);
        } catch (MangleRuntimeException mangleRuntimeException) {
            Assert.assertTrue(true);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#enableMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testEnableMetricProviderByNull() throws MangleException {
        MangleAdminConfigurationSpec actualResult = new MangleAdminConfigurationSpec();
        try {
            actualResult = this.metricProviderService.enableMetricProviderByName(null);
        } catch (MangleException e) {
            Assert.assertEquals(actualResult.getPropertyName(), null);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#enableMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testEnableMetricProviderNoRecordFound() throws MangleException {
        MetricProviderSpec metricProviderSpec = new MetricProviderSpec();
        Optional<MetricProviderSpec> valueMetricSpec = Optional.of(metricProviderSpec);
        when(this.mangleMetricsConfiguration.getActiveMetricProvider()).thenReturn("someName");
        when(this.metricProviderRepository.findByName("someName")).thenReturn(valueMetricSpec);
        MangleAdminConfigurationSpec actualResult = new MangleAdminConfigurationSpec();
        try {
            actualResult = this.metricProviderService.enableMetricProviderByName("someName");
        } catch (NullPointerException e) {
            Assert.assertEquals(actualResult.getPropertyName(), null);
            verify(mangleMetricsConfiguration, times(1)).getActiveMetricProvider();
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#closeAllMetricCollection()}.
     *
     * @throws Exception
     */
    @Test
    public void testCloseAllConnection() throws Exception {
        PowerMockito.doNothing().when(this.wavefrontMeterRegistry).stop();
        PowerMockito.doNothing().when(this.datadogMeterRegistry).stop();
        PowerMockito.doNothing().when(this.dynatraceMeterRegistry).stop();
        PowerMockito.doNothing().when(this.wavefrontMeterRegistry).close();
        PowerMockito.doNothing().when(this.datadogMeterRegistry).close();
        PowerMockito.doNothing().when(this.dynatraceMeterRegistry).close();

        MangleAdminConfigurationSpec mangleAdminConfigurationSpec = new MangleAdminConfigurationSpec();
        Optional<MangleAdminConfigurationSpec> optional = Optional.of(mangleAdminConfigurationSpec);

        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(optional);
        when(this.adminConfigurationRepository.save(mangleAdminConfigurationSpec))
                .thenReturn(mangleAdminConfigurationSpec);

        boolean actualResult = this.metricProviderService.closeAllMetricCollection();
        Assert.assertEquals(actualResult, true);
        verify(this.wavefrontMeterRegistry, times(1)).stop();
        verify(this.datadogMeterRegistry, times(1)).stop();
        verify(this.dynatraceMeterRegistry, times(1)).stop();
    }

    /**
     * * Test method for {@link com.vmware.mangle.services.MetricProviderService#sendMetrics()}.
     *
     * @throws MangleException
     */
    @Test
    public void sendMetricStatusWavefront() throws MangleException {
        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", wavefrontSpec);
        MangleAdminConfigurationSpec sendMetricTrue = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> valueSendMetrics = Optional.of(sendMetricTrue);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(valueSendMetrics);
        Mockito.doNothing().when(this.wavefrontMeterRegistry)
                .start(new NamedThreadFactory("wavefront-metrics-publisher"));
        when(this.wavefrontMeterRegistry.config()).thenReturn(this.meterConfig);
        when(this.wavefrontMeterRegistry.config().commonTags("")).thenReturn(this.meterConfig);
        boolean actualResult = this.metricProviderService.sendMetrics();
        Assert.assertEquals(actualResult, true);
    }

    /**
     * * Test method for {@link com.vmware.mangle.services.MetricProviderService#sendMetrics()}.
     *
     * @throws MangleException
     */
    @Test
    public void sendMetricStatusWavefrontFalse() throws MangleException {
        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", datadogSpec);
        MangleAdminConfigurationSpec sendMetricTrue = mockData.getAdminPropertyForSendingMetricStatusFalse();
        Optional<MangleAdminConfigurationSpec> valueSendMetrics = Optional.of(sendMetricTrue);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(valueSendMetrics);
        Mockito.doNothing().when(this.wavefrontMeterRegistry)
                .start(new NamedThreadFactory("wavefront-metrics-publisher"));
        when(this.wavefrontMeterRegistry.config()).thenReturn(this.meterConfig);
        when(this.wavefrontMeterRegistry.config().commonTags("")).thenReturn(this.meterConfig);
        boolean actualResult = this.metricProviderService.sendMetrics();
        Assert.assertEquals(actualResult, true);
    }

    /**
     * * Test method for {@link com.vmware.mangle.services.MetricProviderService#sendMetrics()}.
     *
     * @throws MangleException
     */
    @Test
    public void sendMetricStatusDatadog() throws MangleException {
        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", datadogSpec);
        MangleAdminConfigurationSpec sendMetricTrue = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> valueSendMetrics = Optional.of(sendMetricTrue);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(valueSendMetrics);
        PowerMockito.doNothing().when(this.datadogMeterRegistry)
                .start(new NamedThreadFactory("datadog-metrics-publisher"));

        boolean actualResult = true;

        actualResult = this.metricProviderService.sendMetrics();

        Assert.assertEquals(actualResult, true);
    }

    /**
     * * Test method for {@link com.vmware.mangle.services.MetricProviderService#sendMetrics()}.
     *
     * @throws MangleException
     */
    @Test
    public void sendMetricStatusDynatrace() throws MangleException {
        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", dynatraceSpec);
        MangleAdminConfigurationSpec sendMetricTrue = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> valueSendMetrics = Optional.of(sendMetricTrue);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(valueSendMetrics);
        PowerMockito.doNothing().when(this.dynatraceMeterRegistry)
                .start(new NamedThreadFactory("dynatrace-metrics-publisher"));

        boolean actualResult = true;

        actualResult = this.metricProviderService.sendMetrics();

        Assert.assertEquals(actualResult, true);
    }

    /**
     * Test method for {@link com.vmware.mangle.services.MetricProviderService#sendMetrics()}.
     *
     * @throws MangleException
     */
    @Test
    public void sendMetricStatusDatadogEmpty() throws MangleException {
        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", datadogSpec);
        MangleAdminConfigurationSpec sendMetricTrue = new MangleAdminConfigurationSpec();
        Optional<MangleAdminConfigurationSpec> valueSendMetrics = Optional.of(sendMetricTrue);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(valueSendMetrics);
        PowerMockito.doNothing().when(this.datadogMeterRegistry)
                .start(new NamedThreadFactory("datadog-metrics-publisher"));
        boolean actualResult = true;

        actualResult = this.metricProviderService.sendMetrics();

        Assert.assertEquals(actualResult, true);
    }

    /**
     * Test method for {@link com.vmware.mangle.services.MetricProviderService#sendMetrics()}.
     *
     * @throws MangleException
     */
    @Test
    public void sendMetricStatusDynatraceEmpty() throws MangleException {
        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", dynatraceSpec);
        MangleAdminConfigurationSpec sendMetricTrue = new MangleAdminConfigurationSpec();
        Optional<MangleAdminConfigurationSpec> valueSendMetrics = Optional.of(sendMetricTrue);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(valueSendMetrics);
        PowerMockito.doNothing().when(this.dynatraceMeterRegistry)
                .start(new NamedThreadFactory("dynatrace-metrics-publisher"));
        boolean actualResult = true;

        actualResult = this.metricProviderService.sendMetrics();

        Assert.assertEquals(actualResult, true);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#initializeMeterRegistryAtBoot()}.
     *
     * @throws MangleException
     */
    @Test
    public void testInitialization() throws MangleException {
        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", wavefrontSpec);
        MangleAdminConfigurationSpec sendMetricTrue = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> valueSendMetrics = Optional.of(sendMetricTrue);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(valueSendMetrics);

        Mockito.doNothing().when(this.wavefrontMeterRegistry)
                .start(new NamedThreadFactory("wavefront-metrics-publisher"));
        when(this.wavefrontMeterRegistry.config()).thenReturn(this.meterConfig);
        when(this.wavefrontMeterRegistry.config().commonTags("")).thenReturn(this.meterConfig);

        this.metricProviderService.initializeMeterRegistryAtBoot();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#initializeMeterRegistryAtBoot()}.
     *
     * @throws MangleException
     */
    @Test
    public void testInitializationFailure() throws MangleException {
        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", null);
        MangleAdminConfigurationSpec sendMetricTrue = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> valueSendMetrics = Optional.of(sendMetricTrue);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(valueSendMetrics);

        Mockito.doNothing().when(this.wavefrontMeterRegistry)
                .start(new NamedThreadFactory("wavefront-metrics-publisher"));
        when(this.wavefrontMeterRegistry.config()).thenReturn(this.meterConfig);
        when(this.wavefrontMeterRegistry.config().commonTags("")).thenReturn(this.meterConfig);

        this.metricProviderService.initializeMeterRegistryAtBoot();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#deleteAllMetricProviders()}.
     *
     * @throws MangleException
     */
    @Test
    public void testDeleteAllMetricProviders() throws MangleException {
        PowerMockito.doNothing().when(this.metricProviderRepository).deleteAll();

        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", datadogSpec);

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> value = Optional.of(activeMetricProvider);
        when(this.mangleMetricsConfiguration.getActiveMetricProvider())
                .thenReturn(activeMetricProvider.getPropertyValue());

        when(this.adminConfigurationRepository.save(activeMetricProvider)).thenReturn(activeMetricProvider);

        List<MetricProviderSpec> listSpec = new ArrayList<>();
        when(this.metricProviderRepository.findAll()).thenReturn(listSpec);
        when(this.metricProviderRepository.findByName(activeMetricProvider.getPropertyValue()))
                .thenReturn(Optional.empty());
        Assert.assertEquals(this.metricProviderService.deleteAllMetricProviders(), true);
        verify(metricProviderRepository, times(1)).findAll();
        verify(metricProviderRepository, times(1)).findByName(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#deleteMetricProvider(String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testDeleteMetricProvider() throws MangleException {
        ReflectionTestUtils.setField(metricProviderService, "activeMetricProvider", wavefrontSpec);
        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();

        Optional<MetricProviderSpec> metricProviderSpecOptional = Optional.of(wavefrontSpec);
        when(this.mangleMetricsConfiguration.getActiveMetricProvider()).thenReturn(wavefrontName);
        when(this.metricProviderRepository.findByName(wavefrontName)).thenReturn(metricProviderSpecOptional);

        boolean status = false;
        try {
            status = this.metricProviderService.deleteMetricProvider(wavefrontName);
        } catch (NullPointerException nullPointerException) {

        }
        Assert.assertEquals(status, false);
        verify(mangleMetricsConfiguration, times(3)).getActiveMetricProvider();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#deleteMetricProvider(String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testDeleteMetricProviderDeleteSuccessfulFalse() throws MangleException {
        MetricProviderService providerService = spy(metricProviderService);
        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> value = Optional.of(activeMetricProvider);
        when(this.mangleMetricsConfiguration.getActiveMetricProvider())
                .thenReturn(activeMetricProvider.getPropertyValue());

        Optional<MetricProviderSpec> metricProviderSpecOptional = Optional.of(wavefrontSpec);
        when(this.metricProviderRepository.findByName(wavefrontName)).thenReturn(metricProviderSpecOptional);

        when(this.adminConfigurationRepository.save(activeMetricProvider)).thenReturn(activeMetricProvider);

        doReturn(wavefrontSpec).when(providerService).getActiveMetricProvider();
        boolean status = false;
        try {
            status = providerService.deleteMetricProvider(wavefrontName);
        } catch (NullPointerException nullPointerException) {

        }
        Assert.assertEquals(status, false);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#deleteMetricProvider(String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testDeleteMetricProviderEmpty() throws MangleException {

        MetricProviderSpec metricProviderSpec = new MetricProviderSpec();
        Optional<MetricProviderSpec> optional = Optional.of(metricProviderSpec);
        when(metricProviderRepository.findByName(wavefrontName)).thenReturn(optional.empty());
        try {
            this.metricProviderService.deleteMetricProvider(wavefrontName);
        } catch (MangleRuntimeException exception) {
            Assert.assertTrue(true);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#deleteMetricProvider(String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testDeleteMetricProviderNull() throws MangleException {
        try {
            this.metricProviderService.deleteMetricProvider(null);
        } catch (MangleException exception) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testIsMangleMetricsEnabled() {
        MangleAdminConfigurationSpec sendMetricSpec = mockData.getAdminPropertyForSendingMetricStatusTrue();
        when(this.mangleMetricsConfiguration.getMetricsEnabled())
                .thenReturn(Boolean.valueOf(sendMetricSpec.getPropertyValue()));
        Assert.assertEquals(this.metricProviderService.isMangleMetricsEnabled(), true);
    }

    @Test
    public void testIsMangleMetricsEnabledFailedNullProperty() {
        MangleAdminConfigurationSpec sendMetricSpec = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> sendMetrics = Optional.of(sendMetricSpec);
        sendMetricSpec.setPropertyValue(null);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(sendMetrics);
        Assert.assertFalse(this.metricProviderService.isMangleMetricsEnabled());
    }

    @Test
    public void testIsMangleMetricsEnabledFailedStatusFalse() {
        MangleAdminConfigurationSpec sendMetricSpec = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> sendMetrics = Optional.of(sendMetricSpec);
        sendMetricSpec.setPropertyValue("false");
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(sendMetrics);
        Assert.assertFalse(this.metricProviderService.isMangleMetricsEnabled());
    }

    @Test
    public void testResyncWhenSendMetricsEnabled() throws MangleException {
        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> value = Optional.of(activeMetricProvider);
        when(adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(value);
        MetricProviderService providerService = spy(metricProviderService);
        doReturn(true).when(providerService).sendMetrics();
        providerService.resync("");
    }

    @Test
    public void testResyncException() throws MangleException {
        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> value = Optional.of(activeMetricProvider);
        when(adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(value);
        MetricProviderService providerService = spy(metricProviderService);
        doThrow(new MangleException(ErrorCode.NO_RECORD_FOUND)).when(providerService).sendMetrics();
        providerService.resync("");
    }

    @Test
    public void testResyncWhenSendMetricsDisabled() throws MangleException {
        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForSendingMetricStatusFalse();
        Optional<MangleAdminConfigurationSpec> value = Optional.of(activeMetricProvider);
        when(adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(value);
        MetricProviderService providerService = spy(metricProviderService);
        doReturn(true).when(providerService).closeAllMetricCollection();
        providerService.resync("");

        verify(providerService, times(1)).closeAllMetricCollection();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#testConnectionMetricProvider(String)}.
     *
     */
    @Test
    public void testTestConnectionMetricProviderForPrometheus() {
        try {
            this.metricProviderService.testConnectionMetricProvider(mockData.getPrometheusMetricProvider());
            fail("testTestConnectionMetricProviderForPrometheus failed");
        } catch (MangleException e) {
            assertEquals(e.getErrorCode(), ErrorCode.UNSUPPORTED_METRIC_PROVIDER_TYPE);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.MetricProviderService#deleteMetricProvider(String)}.
     *
     */
    @Test
    public void testDeleteMetricProviderForPrometheus() {
        MetricProviderSpec metricProviderSpec = mockData.getPrometheusMetricProvider();
        when(mangleMetricsConfiguration.getActiveMetricProvider()).thenReturn(metricProviderSpec.getName());
        Optional<MetricProviderSpec> metricProviderSpecOptional = Optional.of(metricProviderSpec);
        when(metricProviderRepository.findByName(anyString())).thenReturn(metricProviderSpecOptional);
        metricProviderService.getActiveMetricProvider();
        doNothing().when(mangleMetricsConfiguration).setActiveMetricProvider(anyString());
        doNothing().when(mangleMetricsConfiguration).setMetricsEnabled(anyBoolean());
        try {
            metricProviderService.deleteMetricProvider(metricProviderSpec.getName());
            fail("testDeleteMetricProviderForPrometheus failed!");
        } catch (MangleException e) {
            assertEquals(e.getErrorCode(), ErrorCode.UNSUPPORTED_DELETE_METRIC_PROVIDER_TYPE);
            verify(metricProviderRepository, times(3)).findByName(anyString());
            verify(mangleMetricsConfiguration, times(1)).setActiveMetricProvider(anyString());
            verify(mangleMetricsConfiguration, times(1)).setMetricsEnabled(anyBoolean());
        }
    }

    @Test(description = "Validate Normalizing of URI when input URI contains multiple forward slashes.")
    public void testNormalizeURI_multipleForwardSlashes() {
        MetricProviderSpec spec = mockData.metricProviderWavefront();
        spec.getWaveFrontConnectionProperties()
                .setWavefrontInstance(MockDataConstants.INVALID_URI_MULTIPLE_FORWARD_SLASHES);

        MetricProviderSpec normalizedSpec = metricProviderService.normalizeURI(spec);

        assertEquals(MockDataConstants.VALID_URI,
                normalizedSpec.getWaveFrontConnectionProperties().getWavefrontInstance());
    }

    @Test(description = "Validate normalize URI when the input contains only single forward slash")
    public void testNormalizeURI_singleForwardSlash() {
        MetricProviderSpec spec = mockData.getDynatraceMetricConfig();
        spec.getDynatraceConnectionProperties().setUri(MockDataConstants.INVALID_URI_SINGLE_FORWARD_SLASH);

        MetricProviderSpec normalizedSpec = metricProviderService.normalizeURI(spec);

        assertEquals(MockDataConstants.VALID_URI, normalizedSpec.getDynatraceConnectionProperties().getUri());
    }

    @Test(description = "Validate normalize URI when the input URI doesn't contain any forward slashes")
    public void testNormalizeURI_validURI() {
        MetricProviderSpec spec = mockData.getDynatraceMetricConfig();
        spec.getDynatraceConnectionProperties().setUri(MockDataConstants.VALID_URI);

        MetricProviderSpec normalizedSpec = metricProviderService.normalizeURI(spec);

        assertEquals(MockDataConstants.VALID_URI, normalizedSpec.getDynatraceConnectionProperties().getUri());
    }

}