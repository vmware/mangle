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
import java.util.Optional;
import java.util.Properties;

import io.micrometer.core.instrument.util.NamedThreadFactory;
import io.micrometer.datadog.DatadogMeterRegistry;
import io.micrometer.wavefront.WavefrontMeterRegistry;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
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
import com.vmware.mangle.services.config.MangleWavefrontConfig;
import com.vmware.mangle.services.mockdata.MetricProviderMockData;
import com.vmware.mangle.services.repository.AdminConfigurationRepository;
import com.vmware.mangle.services.repository.MetricProviderRepository;
import com.vmware.mangle.task.framework.metric.providers.MetricProviderClientFactory;
import com.vmware.mangle.utils.ReadProperty;
import com.vmware.mangle.utils.clients.metricprovider.MetricProviderClient;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.MetricProviderConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;

/**
 * Unit Tests Case for MetricProviderService.
 *
 * @author ashrimali
 */

@PrepareForTest({ MetricProviderClientFactory.class })
@PowerMockIgnore(value = { "org.apache.logging.log4j.*" })
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
    private WavefrontMeterRegistry wavefrontMeterRegistry;

    @Mock
    private AdminConfigurationRepository adminConfigurationRepository;

    private MetricProviderMockData mockData = new MetricProviderMockData();

    private MetricProviderSpec datadogSpec;

    private MetricProviderSpec wavefrontSpec;

    private Properties properties;

    private String wavefrontName;

    private String datadogName;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        datadogSpec = mockData.metricProviderDatadog();
        wavefrontSpec = mockData.metricProviderWavefront();
        wavefrontName = properties.getProperty("wavefrontMetricReporterName");
        datadogName = properties.getProperty("datadogMetricReporterName");
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.mockData = null;
        this.datadogSpec = null;
        this.wavefrontSpec = null;
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
     * {@link com.vmware.mangle.service.MetricProviderService#getAllMetricProviders()}.
     */
    @Test
    public void testGetAllMetricProviders() {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        metricProviderSpecList.add(wavefrontSpec);
        metricProviderSpecList.add(datadogSpec);
        when(metricProviderRepository.findAll()).thenReturn(metricProviderSpecList);
        Assert.assertEquals(metricProviderService.getAllMetricProviders().size(), 2);
        verify(metricProviderRepository, times(1)).findAll();
    }


    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#getMetricProviderByType(MetricProviderType)}.
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
     * {@link com.vmware.mangle.service.MetricProviderService#getMetricProviderByType(MetricProviderType)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetMetricProviderByNull() throws MangleException {
        when(this.metricProviderRepository.findByMetricProviderType(null)).thenReturn(null);
        List<MetricProviderSpec> result = null;
        try {
            this.metricProviderService.getMetricProviderByType(null);
        } catch (MangleException exception) {
            Assert.assertEquals(result, null);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#getMetricProviderByType(MetricProviderType)}.
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
     * {@link com.vmware.mangle.service.MetricProviderService#getMetricProviderByType(MetricProviderType)}.
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
     * {@link com.vmware.mangle.service.MetricProviderService#getActiveMetricProvider()}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testGetActiveMetricProvider() throws MangleException {
        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> value = Optional.of(activeMetricProvider);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(value);

        Optional<MetricProviderSpec> metricProviderSpecOptional = Optional.of(wavefrontSpec);
        when(metricProviderRepository.findByName(activeMetricProvider.getPropertyValue()))
                .thenReturn(metricProviderSpecOptional);

        Assert.assertEquals(this.metricProviderService.getActiveMetricProvider().getName(),
                properties.getProperty(activeMetricProvider.getPropertyValue()));
        verify(adminConfigurationRepository, times(1))
                .findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        verify(metricProviderRepository, times(1)).findByName(activeMetricProvider.getPropertyValue());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#getActiveMetricProvider()}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testGetActiveMetricProviderNotPresent() throws MangleException {
        MangleAdminConfigurationSpec activeMetricProvider = new MangleAdminConfigurationSpec();
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(Optional.of(activeMetricProvider).empty());
        MetricProviderSpec result = null;
        try {
            result = this.metricProviderService.getActiveMetricProvider();
        } catch (MangleException exception) {
            Assert.assertEquals(result, null);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#getActiveMetricProvider()}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testGetActiveMetricProviderFailure() throws MangleException {
        Optional<MangleAdminConfigurationSpec> value = Optional.of(new MangleAdminConfigurationSpec());
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(value);

        MetricProviderSpec actualResult = null;
        try {
            actualResult = this.metricProviderService.getActiveMetricProvider();
        } catch (MangleRuntimeException exception) {
            Assert.assertEquals(actualResult, null);
        }
        verify(adminConfigurationRepository, times(1))
                .findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#getMetricProviderByName(String)}.
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
     * {@link com.vmware.mangle.service.MetricProviderService#getMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testGetMetricProviderByNameNotPresent() throws MangleException {
        MetricProviderSpec metricProviderSpec = new MetricProviderSpec();
        Optional<MetricProviderSpec> optional = Optional.of(metricProviderSpec);
        when(metricProviderRepository.findByName(wavefrontName)).thenReturn(optional.empty());
        MetricProviderSpec result = null;
        try {
            result = this.metricProviderService.getMetricProviderByName(wavefrontName);
        } catch (MangleRuntimeException exception) {
            Assert.assertEquals(result, null);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#getMetricProviderByName(String)}.
     *
     */
    @Test
    public void testGetMetricProviderByNameFailureNoData() {
        MetricProviderSpec actualResult = null;
        try {
            actualResult = this.metricProviderService.getMetricProviderByName("someName");
        } catch (NullPointerException nullPointerException) {
            Assert.assertEquals(actualResult, null);
        } catch (MangleException exception) {
        }
        verify(this.metricProviderRepository, times(1)).findByName("someName");
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#getMetricProviderByName(String)}.
     *
     */
    @Test
    public void testGetMetricProviderByNameFailureNullName() {
        MetricProviderSpec actualResult = null;
        try {
            actualResult = this.metricProviderService.getMetricProviderByName(null);
        } catch (NullPointerException nullPointerException) {
            Assert.assertEquals(actualResult, null);
        } catch (MangleException exception) {
        }
        verify(this.metricProviderRepository, times(0)).findByName("someName");
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#addMetricProvider(MetricProviderSpec)}.
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
     * {@link com.vmware.mangle.service.MetricProviderService#addMetricProvider(MetricProviderSpec)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testAddMetricProviderWithNull() throws MangleException {
        MetricProviderSpec actualResult = null;
        try {
            actualResult = this.metricProviderService.addMetricProvider(null);

        } catch (MangleException exception) {
            Assert.assertEquals(actualResult, null);
        }
        verify(this.metricProviderRepository, times(0)).save(null);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#addMetricProvider(MetricProviderSpec)}.
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
     * {@link com.vmware.mangle.service.MetricProviderService#updateMetricProviderById(String,MetricProviderSpec)}.
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
     * {@link com.vmware.mangle.service.MetricProviderService#updateMetricProviderById(String,MetricProviderSpec)}.
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
        MetricProviderSpec result = null;
        try {
            result = this.metricProviderService.updateMetricProviderById(id, metricProviderSpec);
        } catch (MangleRuntimeException exception) {
            Assert.assertEquals(result, null);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#updateMetricProviderByName(String,MetricProviderSpec)}.
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
     * {@link com.vmware.mangle.service.MetricProviderService#updateMetricProviderById(String,MetricProviderSpec)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testUpdateMetricProviderByNameEmpty() throws MangleException {
        MetricProviderSpec metricProviderSpec = new MetricProviderSpec();
        Optional<MetricProviderSpec> optional = Optional.of(metricProviderSpec);
        when(metricProviderRepository.findById("someName")).thenReturn(optional.empty());
        MetricProviderSpec result = null;
        try {
            result = this.metricProviderService.updateMetricProviderById("someName", metricProviderSpec);
        } catch (MangleRuntimeException exception) {
            Assert.assertEquals(result, null);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#updateMetricProviderById(String,MetricProviderSpec)}.
     *
     */
    @Test
    public void testUpdateMetricProviderByIdNull() {

        MetricProviderSpec actualResult = new MetricProviderSpec();
        try {
            actualResult = this.metricProviderService.updateMetricProviderById(null, null);
        } catch (MangleException e) {
            Assert.assertEquals(actualResult.getName(), null);
        }

        verify(this.metricProviderRepository, times(0)).save(null);
        verify(this.metricProviderRepository, times(0)).findById(null);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#updateMetricProviderByName(String,MetricProviderSpec)}.
     *
     */
    @Test
    public void testUpdateMetricProviderByNameNull() {

        MetricProviderSpec actualResult = new MetricProviderSpec();
        try {
            actualResult = this.metricProviderService.updateMetricProviderByName(null, null);
        } catch (MangleException e) {
            Assert.assertEquals(actualResult.getName(), null);
        }

        verify(this.metricProviderRepository, times(0)).save(null);
        verify(this.metricProviderRepository, times(0)).findByName(null);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#testConnectionMetricProvider(String)}.
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
        Assert.assertEquals(this.metricProviderService.testConnectionMetricProvider(wavefrontName), true);
        verify(this.metricProviderRepository, times(1)).findByName(wavefrontName);
        verify(this.mangleMetricProviderClientFactory, times(1)).getMetricProviderClient(metricProviderSpec);
        verify(this.metricProviderClient, times(1)).testConnection();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#testConnectionMetricProvider(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testTestConnectionMetricProviderEmpty() throws MangleException {
        MetricProviderSpec metricProviderSpec = new MetricProviderSpec();
        Optional<MetricProviderSpec> optional = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName(wavefrontName)).thenReturn(optional.empty());
        boolean result = false;
        try {
            result = this.metricProviderService.testConnectionMetricProvider(wavefrontName);
        } catch (MangleRuntimeException exception) {
            Assert.assertEquals(result, false);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#testConnectionMetricProvider(MetricProviderSpec)}.
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
     * {@link com.vmware.mangle.service.MetricProviderService#enableMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testEnableMetricProviderByNameStatusWavefront() throws MangleException {
        MetricProviderSpec metricProviderSpec = wavefrontSpec;
        Optional<MetricProviderSpec> valueMetricSpec = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName(wavefrontName)).thenReturn(valueMetricSpec);

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> valueAdminSpec = Optional.of(activeMetricProvider);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(valueAdminSpec);

        when(this.adminConfigurationRepository.save(activeMetricProvider)).thenReturn(activeMetricProvider);

        MangleAdminConfigurationSpec sendMetricSpec = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> sendMetrics = Optional.of(sendMetricSpec);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(sendMetrics);

        Assert.assertEquals(this.metricProviderService.enableMetricProviderByName(wavefrontName).getPropertyValue(),
                wavefrontName);
        verify(this.adminConfigurationRepository, times(2))
                .findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        verify(this.adminConfigurationRepository, times(1)).save(activeMetricProvider);
        verify(this.metricProviderRepository, times(2)).findByName(wavefrontName);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#enableMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testEnableMetricProviderByNameEmpty() throws MangleException {
        MetricProviderSpec metricProviderSpec = new MetricProviderSpec();
        Optional<MetricProviderSpec> optional = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName(wavefrontName)).thenReturn(optional.empty());

        MangleAdminConfigurationSpec result = null;
        try {
            result = this.metricProviderService.enableMetricProviderByName(wavefrontName);
        } catch (MangleRuntimeException exception) {
            Assert.assertEquals(result, null);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#enableMetricProviderByName(String)}.
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
     * {@link com.vmware.mangle.service.MetricProviderService#enableMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void testEnableMetricProviderByNameStatusDatadog() throws MangleException {
        MetricProviderSpec metricProviderSpec = datadogSpec;
        Optional<MetricProviderSpec> valueMetricSpec = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName(datadogName)).thenReturn(valueMetricSpec);

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> valueAdminSpec = Optional.of(activeMetricProvider);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(valueAdminSpec);

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
            verify(this.adminConfigurationRepository, times(2))
                    .findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
            verify(this.adminConfigurationRepository, times(1)).save(activeMetricProvider);
            verify(this.metricProviderRepository, times(2)).findByName(datadogName);
            throw nullPointerException;
        }

    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#enableMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testEnableMetricProviderByNameStatusFalse() throws MangleException {
        MetricProviderSpec metricProviderSpec = wavefrontSpec;
        Optional<MetricProviderSpec> valueMetricSpec = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName(wavefrontName)).thenReturn(valueMetricSpec);

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> valueAdminSpec = Optional.of(activeMetricProvider);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(valueAdminSpec);

        when(this.adminConfigurationRepository.save(activeMetricProvider)).thenReturn(activeMetricProvider);

        MangleAdminConfigurationSpec sendMetricSpec = mockData.getAdminPropertyForSendingMetricStatusFalse();
        Optional<MangleAdminConfigurationSpec> sendMetrics = Optional.of(sendMetricSpec);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(sendMetrics);

        Assert.assertEquals(this.metricProviderService.enableMetricProviderByName(wavefrontName).getPropertyValue(),
                wavefrontName);
        verify(this.adminConfigurationRepository, times(1))
                .findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        verify(this.adminConfigurationRepository, times(1)).save(activeMetricProvider);
        verify(this.metricProviderRepository, times(1)).findByName(wavefrontName);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#enableMetricProviderByName(String)}.
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
     * {@link com.vmware.mangle.service.MetricProviderService#enableMetricProviderByName(String)}.
     *
     * @throws MangleException
     *
     */
    @Test
    public void testEnableMetricProviderNoRecordFound() throws MangleException {
        MetricProviderSpec metricProviderSpec = new MetricProviderSpec();
        Optional<MetricProviderSpec> valueMetricSpec = Optional.of(metricProviderSpec);
        when(this.metricProviderRepository.findByName("someName")).thenReturn(valueMetricSpec);
        MangleAdminConfigurationSpec actualResult = new MangleAdminConfigurationSpec();
        try {
            actualResult = this.metricProviderService.enableMetricProviderByName("someName");
        } catch (NullPointerException e) {
            Assert.assertEquals(actualResult.getPropertyName(), null);
        }
        verify(this.adminConfigurationRepository, times(1))
                .findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#closeAllMetricCollection()}.
     *
     * @throws Exception
     */
    @Test
    public void testCloseAllConnection() throws Exception {
        PowerMockito.doNothing().when(this.wavefrontMeterRegistry).stop();
        PowerMockito.doNothing().when(this.datadogMeterRegistry).stop();
        PowerMockito.doNothing().when(this.wavefrontMeterRegistry).close();
        PowerMockito.doNothing().when(this.datadogMeterRegistry).close();

        MangleAdminConfigurationSpec mangleAdminConfigurationSpec = new MangleAdminConfigurationSpec();
        Optional<MangleAdminConfigurationSpec> optional = Optional.of(mangleAdminConfigurationSpec);

        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(optional);
        when(this.adminConfigurationRepository.save(mangleAdminConfigurationSpec))
                .thenReturn(mangleAdminConfigurationSpec);

        boolean actualResult = this.metricProviderService.closeAllMetricCollection();
        Assert.assertEquals(actualResult, true);
        verify(this.adminConfigurationRepository, times(1))
                .findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        verify(this.adminConfigurationRepository, times(1)).save(mangleAdminConfigurationSpec);
        verify(this.wavefrontMeterRegistry, times(1)).stop();
        verify(this.datadogMeterRegistry, times(1)).stop();
        verify(this.wavefrontMeterRegistry, times(1)).close();
        verify(this.datadogMeterRegistry, times(1)).close();
    }

    /**
     * * Test method for {@link com.vmware.mangle.service.MetricProviderService#sendMetrics()}.
     *
     * @throws MangleException
     */
    @Test
    public void sendMetricStatusWavefront() throws MangleException {

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> valueMetricProvider = Optional.of(activeMetricProvider);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(valueMetricProvider);

        Optional<MetricProviderSpec> metricProviderSpecOptional = Optional.of(wavefrontSpec);
        when(metricProviderRepository.findByName(activeMetricProvider.getPropertyValue()))
                .thenReturn(metricProviderSpecOptional);

        MangleAdminConfigurationSpec sendMetricTrue = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> valueSendMetrics = Optional.of(sendMetricTrue);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(valueSendMetrics);
        PowerMockito.doNothing().when(this.wavefrontMeterRegistry)
                .start(new NamedThreadFactory("wavefront-metrics-publisher"));
        boolean actualResult = this.metricProviderService.sendMetrics();
        Assert.assertEquals(actualResult, true);
        verify(this.adminConfigurationRepository, times(1))
                .findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        verify(this.adminConfigurationRepository, times(1))
                .findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        verify(this.metricProviderRepository, times(1)).findByName(activeMetricProvider.getPropertyValue());
    }

    /**
     * * Test method for {@link com.vmware.mangle.service.MetricProviderService#sendMetrics()}.
     *
     * @throws MangleException
     */
    @Test
    public void sendMetricStatusWavefrontFalse() throws MangleException {

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> valueMetricProvider = Optional.of(activeMetricProvider);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(valueMetricProvider);

        Optional<MetricProviderSpec> metricProviderSpecOptional = Optional.of(wavefrontSpec);
        when(metricProviderRepository.findByName(activeMetricProvider.getPropertyValue()))
                .thenReturn(metricProviderSpecOptional);

        MangleAdminConfigurationSpec sendMetricTrue = mockData.getAdminPropertyForSendingMetricStatusFalse();
        Optional<MangleAdminConfigurationSpec> valueSendMetrics = Optional.of(sendMetricTrue);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(valueSendMetrics);
        PowerMockito.doNothing().when(this.wavefrontMeterRegistry)
                .start(new NamedThreadFactory("wavefront-metrics-publisher"));
        boolean actualResult = this.metricProviderService.sendMetrics();
        Assert.assertEquals(actualResult, true);
        verify(this.adminConfigurationRepository, times(1))
                .findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        verify(this.adminConfigurationRepository, times(1))
                .findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        verify(this.metricProviderRepository, times(1)).findByName(activeMetricProvider.getPropertyValue());
    }

    /**
     * * Test method for {@link com.vmware.mangle.service.MetricProviderService#sendMetrics()}.
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void sendMetricStatusDatadog() throws MangleException {

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderDatadog();
        Optional<MangleAdminConfigurationSpec> valueMetricProvider = Optional.of(activeMetricProvider);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(valueMetricProvider);

        Optional<MetricProviderSpec> metricProviderSpecOptional = Optional.of(datadogSpec);
        when(metricProviderRepository.findByName(activeMetricProvider.getPropertyValue()))
                .thenReturn(metricProviderSpecOptional);

        MangleAdminConfigurationSpec sendMetricTrue = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> valueSendMetrics = Optional.of(sendMetricTrue);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(valueSendMetrics);
        PowerMockito.doNothing().when(this.datadogMeterRegistry)
                .start(new NamedThreadFactory("datadog-metrics-publisher"));

        boolean actualResult = true;
        try {
            actualResult = this.metricProviderService.sendMetrics();
        } catch (NullPointerException nullPointerException) {
            Assert.assertEquals(actualResult, true);
            verify(this.adminConfigurationRepository, times(1))
                    .findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
            verify(this.adminConfigurationRepository, times(1))
                    .findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
            verify(this.metricProviderRepository, times(1)).findByName(activeMetricProvider.getPropertyValue());
            throw nullPointerException;
        }

    }

    /**
     * Test method for {@link com.vmware.mangle.service.MetricProviderService#sendMetrics()}.
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void sendMetricStatusDatadogEmpty() throws MangleException {

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> valueMetricProvider = Optional.of(activeMetricProvider);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(valueMetricProvider);

        Optional<MetricProviderSpec> metricProviderSpecOptional = Optional.of(datadogSpec);
        when(metricProviderRepository.findByName(activeMetricProvider.getPropertyValue()))
                .thenReturn(metricProviderSpecOptional);

        MangleAdminConfigurationSpec sendMetricTrue = new MangleAdminConfigurationSpec();
        Optional<MangleAdminConfigurationSpec> valueSendMetrics = Optional.of(sendMetricTrue);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(valueSendMetrics);
        PowerMockito.doNothing().when(this.datadogMeterRegistry)
                .start(new NamedThreadFactory("datadog-metrics-publisher"));
        boolean actualResult = true;
        try {
            actualResult = this.metricProviderService.sendMetrics();
        } catch (NullPointerException nullPointerException) {
            Assert.assertEquals(actualResult, true);
            verify(this.adminConfigurationRepository, times(1))
                    .findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
            verify(this.adminConfigurationRepository, times(1))
                    .findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
            verify(this.metricProviderRepository, times(1)).findByName(activeMetricProvider.getPropertyValue());
            throw nullPointerException;
        }

    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#initializeMeterRegistryAtBoot()}.
     *
     * @throws MangleException
     */
    @Test
    public void testInitialization() throws MangleException {

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> valueMetricProvider = Optional.of(activeMetricProvider);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(valueMetricProvider);

        Optional<MetricProviderSpec> metricProviderSpecOptional = Optional.of(wavefrontSpec);
        when(metricProviderRepository.findByName(activeMetricProvider.getPropertyValue()))
                .thenReturn(metricProviderSpecOptional);

        MangleAdminConfigurationSpec sendMetricTrue = mockData.getAdminPropertyForSendingMetricStatusTrue();
        Optional<MangleAdminConfigurationSpec> valueSendMetrics = Optional.of(sendMetricTrue);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(valueSendMetrics);
        PowerMockito.doNothing().when(this.wavefrontMeterRegistry)
                .start(new NamedThreadFactory("wavefront-metrics-publisher"));
        this.metricProviderService.initializeMeterRegistryAtBoot();
        verify(this.adminConfigurationRepository, times(1))
                .findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        verify(this.adminConfigurationRepository, times(2))
                .findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        verify(this.metricProviderRepository, times(1)).findByName(activeMetricProvider.getPropertyValue());

    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#deleteAllMetricProviders()}.
     */
    @Test
    public void testDeleteAllMetricProviders() {
        PowerMockito.doNothing().when(this.metricProviderRepository).deleteAll();

        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> value = Optional.of(activeMetricProvider);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(value);

        when(this.adminConfigurationRepository.save(activeMetricProvider)).thenReturn(activeMetricProvider);

        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(value);

        List<MetricProviderSpec> listSpec = new ArrayList<>();
        when(this.metricProviderRepository.findAll()).thenReturn(listSpec);
        Assert.assertEquals(this.metricProviderService.deleteAllMetricProviders(), true);

        verify(this.adminConfigurationRepository, times(1))
                .findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        verify(this.adminConfigurationRepository, times(1))
                .findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        verify(this.adminConfigurationRepository, times(2)).save(activeMetricProvider);

    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#deleteMetricProvider(String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testDeleteMetricProvider() throws MangleException {
        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        Optional<MangleAdminConfigurationSpec> value = Optional.of(activeMetricProvider);
        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER))
                .thenReturn(value);

        Optional<MetricProviderSpec> metricProviderSpecOptional = Optional.of(wavefrontSpec);
        when(this.metricProviderRepository.findByName(wavefrontName)).thenReturn(metricProviderSpecOptional);

        when(this.adminConfigurationRepository.save(activeMetricProvider)).thenReturn(activeMetricProvider);

        when(this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS))
                .thenReturn(value);
        boolean status = false;
        try {
            status = this.metricProviderService.deleteMetricProvider(wavefrontName);
        } catch (NullPointerException nullPointerException) {

        }
        Assert.assertEquals(status, false);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#deleteMetricProvider(String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testDeleteMetricProviderEmpty() throws MangleException {

        MetricProviderSpec metricProviderSpec = new MetricProviderSpec();
        Optional<MetricProviderSpec> optional = Optional.of(metricProviderSpec);
        when(metricProviderRepository.findByName(wavefrontName)).thenReturn(optional.empty());
        boolean result = false;
        try {
            result = this.metricProviderService.deleteMetricProvider(wavefrontName);
        } catch (MangleRuntimeException exception) {
            Assert.assertEquals(result, false);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.MetricProviderService#deleteMetricProvider(String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testDeleteMetricProviderNull() throws MangleException {
        boolean result = false;
        try {
            result = this.metricProviderService.deleteMetricProvider(null);
        } catch (MangleException exception) {
            Assert.assertEquals(result, false);
        }

    }
}
