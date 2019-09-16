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

package com.vmware.mangle.unittest.services.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.MangleAdminConfigurationSpec;
import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.model.enums.MetricProviderByStatus;
import com.vmware.mangle.model.enums.MetricProviderType;
import com.vmware.mangle.model.response.MetricProviderResponse;
import com.vmware.mangle.services.EventService;
import com.vmware.mangle.services.MetricProviderService;
import com.vmware.mangle.services.controller.MetricProviderController;
import com.vmware.mangle.services.events.web.CustomEventPublisher;
import com.vmware.mangle.services.mockdata.MetricProviderMockData;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Unit Tests Case for MangleMetricProviderController
 *
 * @author ashrimali
 *
 */

@PowerMockIgnore(value = { "org.apache.logging.log4j.*" })
public class MetricProviderControllerTest {

    @InjectMocks
    private MetricProviderController mangleMetricProviderController;

    @Mock
    private MetricProviderService metricProviderService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CustomEventPublisher publisher;

    @Mock
    private EventService eventService;

    private MetricProviderMockData mockData = new MetricProviderMockData();

    private MetricProviderSpec datadogSpec;

    private MetricProviderSpec wavefrontSpec;

    @SuppressWarnings("javadoc")
    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        publisher = new CustomEventPublisher(applicationEventPublisher, eventService);
        when(eventService.save(any())).then(new ReturnsArgumentAt(0));
        datadogSpec = mockData.metricProviderDatadog();
        wavefrontSpec = mockData.metricProviderWavefront();

    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.mockData = null;
        this.publisher = null;
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
     * {@link MetricProviderController#getMetricProviders(MetricProviderType, Boolean)}.
     *
     * @throws MangleException
     */
    @Test
    public void getMetricProvierTest() throws MangleException {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        metricProviderSpecList.add(wavefrontSpec);
        metricProviderSpecList.add(datadogSpec);
        when(metricProviderService.getAllMetricProviders()).thenReturn(metricProviderSpecList);
        ResponseEntity<List<MetricProviderSpec>> response = mangleMetricProviderController.getMetricProviders(null);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        List<MetricProviderSpec> responseList = response.getBody();
        Assert.assertEquals(responseList.size(), 2);
        Assert.assertEquals(responseList.get(0), wavefrontSpec);
        verify(metricProviderService, times(1)).getAllMetricProviders();
    }

    /**
     * Test method for
     * {@link MetricProviderController#getMetricProviders(MetricProviderType, Boolean)}.
     *
     * @throws MangleException
     */
    @Test
    public void getMetricProviderTestNull() throws MangleException {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        when(metricProviderService.getAllMetricProviders()).thenReturn(metricProviderSpecList);
        ResponseEntity<List<MetricProviderSpec>> response = mangleMetricProviderController.getMetricProviders(null);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        List<MetricProviderSpec> responseList = response.getBody();
        Assert.assertEquals(responseList.size(), 0);
        verify(metricProviderService, times(1)).getAllMetricProviders();
    }

    /**
     * Test method for {@link MetricProviderController#addMetricProvider(MetricProviderSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testAddMetricProviderAlreadyExists() throws MangleException {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        metricProviderSpecList.add(wavefrontSpec);
        when(metricProviderService.getMetricProviderByType(MetricProviderType.WAVEFRONT))
                .thenReturn(metricProviderSpecList);
        ResponseEntity<MetricProviderSpec> response = null;
        try {
            response = mangleMetricProviderController.addMetricProvider(wavefrontSpec);
        } catch (MangleException exception) {
            Assert.assertEquals(response, null);
        }
        verify(metricProviderService, times(1)).getMetricProviderByType(MetricProviderType.WAVEFRONT);
    }

    /**
     * Test method for {@link MetricProviderController#addMetricProvider(MetricProviderSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testAddMetricProvider() throws MangleException {
        when(metricProviderService.getMetricProviderByType(MetricProviderType.WAVEFRONT))
                .thenReturn(new ArrayList<MetricProviderSpec>());
        when(metricProviderService.testConnectionMetricProvider(wavefrontSpec)).thenReturn(true);
        when(metricProviderService.addMetricProvider(wavefrontSpec)).thenReturn(wavefrontSpec);
        ResponseEntity<MetricProviderSpec> response = mangleMetricProviderController.addMetricProvider(wavefrontSpec);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), wavefrontSpec);
        verify(metricProviderService, times(1)).getMetricProviderByType(MetricProviderType.WAVEFRONT);
        verify(metricProviderService, times(1)).testConnectionMetricProvider(wavefrontSpec);
        verify(metricProviderService, times(1)).addMetricProvider(wavefrontSpec);
    }

    /**
     * Test method for {@link MetricProviderController#addMetricProvider(MetricProviderSpec)}.
     *
     * @throws MangleException
     */

    @Test
    public void testAddMetricProviderFailTestConnection() throws MangleException {
        when(metricProviderService.getMetricProviderByType(MetricProviderType.WAVEFRONT))
                .thenReturn(new ArrayList<MetricProviderSpec>());
        when(metricProviderService.testConnectionMetricProvider(wavefrontSpec)).thenReturn(false);
        when(metricProviderService.addMetricProvider(wavefrontSpec)).thenReturn(wavefrontSpec);
        ResponseEntity<MetricProviderSpec> response = mangleMetricProviderController.addMetricProvider(wavefrontSpec);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        verify(metricProviderService, times(1)).getMetricProviderByType(MetricProviderType.WAVEFRONT);
        verify(metricProviderService, times(1)).testConnectionMetricProvider(wavefrontSpec);
    }

    /**
     * Test method for {@link MetricProviderController#updateMetricProvider(MetricProviderSpec)}.
     *
     * @throws MangleException
     */

    @Test
    public void testUdateMetricProvider() throws MangleException {

        when(metricProviderService.testConnectionMetricProvider(wavefrontSpec)).thenReturn(true);
        when(metricProviderService.updateMetricProviderByName(wavefrontSpec.getName(), wavefrontSpec))
                .thenReturn(wavefrontSpec);
        ResponseEntity<MetricProviderSpec> response =
                mangleMetricProviderController.updateMetricProvider(wavefrontSpec);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), wavefrontSpec);
        verify(metricProviderService, times(1)).testConnectionMetricProvider(wavefrontSpec);
        verify(metricProviderService, times(1)).updateMetricProviderByName(wavefrontSpec.getName(), wavefrontSpec);
    }

    /**
     * Test method for {@link MetricProviderController#updateMetricProvider(MetricProviderSpec)}.
     *
     * @throws MangleException
     */

    @Test
    public void testUdateMetricProviderWrongData() throws MangleException {

        when(metricProviderService.testConnectionMetricProvider(mockData.wrongSpec())).thenReturn(false);
        when(metricProviderService.updateMetricProviderByName(wavefrontSpec.getName(), wavefrontSpec))
                .thenReturn(wavefrontSpec);
        ResponseEntity<MetricProviderSpec> response =
                mangleMetricProviderController.updateMetricProvider(wavefrontSpec);
        response.getStatusCode().equals(HttpStatus.OK);
        verify(metricProviderService, times(1)).testConnectionMetricProvider(wavefrontSpec);
    }


    /**
     * Test method for {@link MetricProviderController#deleteMetricProvider(String)}.
     *
     * @throws MangleException
     */

    @Test
    public void testDeleteMetricProviderByName() throws MangleException {
        when(metricProviderService.deleteMetricProvider(wavefrontSpec.getName())).thenReturn(true);
        ResponseEntity response = mangleMetricProviderController.deleteMetricProvider(wavefrontSpec.getName());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
        verify(metricProviderService, times(1)).deleteMetricProvider(wavefrontSpec.getName());
    }

    /**
     * Test method for {@link MetricProviderController#deleteMetricProvider(String)}.
     *
     * @throws MangleException
     */

    @Test
    public void testDeleteMetricProviderByNameFalse() throws MangleException {
        when(metricProviderService.deleteMetricProvider(wavefrontSpec.getName())).thenReturn(false);
        ResponseEntity response = mangleMetricProviderController.deleteMetricProvider(wavefrontSpec.getName());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
        verify(metricProviderService, times(1)).deleteMetricProvider(wavefrontSpec.getName());
    }

    /**
     * Test method for {@link MetricProviderController#deleteMetricProvider(String)}.
     *
     * @throws MangleException
     */

    @Test
    public void testDeleteMetricProviderAll() throws MangleException {
        when(metricProviderService.deleteAllMetricProviders()).thenReturn(true);
        ResponseEntity response = mangleMetricProviderController.deleteMetricProvider("");
        Assert.assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
        verify(metricProviderService, times(1)).deleteAllMetricProviders();
    }

    /**
     * Test method for {@link MetricProviderController#deleteMetricProvider(String)}.
     *
     * @throws MangleException
     */

    @Test
    public void testDeleteMetricProviderAllFalse() throws MangleException {
        when(metricProviderService.deleteAllMetricProviders()).thenReturn(false);
        ResponseEntity response = mangleMetricProviderController.deleteMetricProvider("");
        Assert.assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
        verify(metricProviderService, times(1)).deleteAllMetricProviders();
    }

    /**
     * Test method for {@link MetricProviderController#testConnection(String)}.
     *
     * @throws MangleException
     */

    @Test
    public void testTestConnection() throws MangleException {
        when(metricProviderService.testConnectionMetricProvider(wavefrontSpec)).thenReturn(true);
        ResponseEntity<MetricProviderResponse> response = mangleMetricProviderController.testConnection(wavefrontSpec);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        MetricProviderResponse mangleResponse = new MetricProviderResponse();
        mangleResponse.setResultStatus(true);
        Assert.assertEquals(response.getBody(), mangleResponse);
        verify(metricProviderService, times(1)).testConnectionMetricProvider(wavefrontSpec);
    }

    /**
     * Test method for {@link MetricProviderController#testConnection(String)}.
     *
     * @throws MangleException
     */

    @Test
    public void testTestConnectionFailure() throws MangleException {
        when(metricProviderService.testConnectionMetricProvider(wavefrontSpec)).thenReturn(false);
        ResponseEntity<MetricProviderResponse> response = null;
        try {
            response = mangleMetricProviderController.testConnection(wavefrontSpec);
        } catch (MangleException exception) {
            Assert.assertEquals(response, null);
        }
        verify(metricProviderService, times(1)).testConnectionMetricProvider(wavefrontSpec);
    }

    /**
     * Test method for
     * {@link MetricProviderController#getMetricProviders(MetricProviderType, Boolean)}.
     *
     * @throws MangleException
     *
     */

    @Test
    public void testGetActiveMetricProvider() throws MangleException {
        when(metricProviderService.getActiveMetricProvider()).thenReturn(wavefrontSpec);
        ResponseEntity<List<MetricProviderSpec>> response =
                mangleMetricProviderController.getMetricProviders(MetricProviderByStatus.ACTIVE);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody().get(0).getName(), wavefrontSpec.getName());
        verify(metricProviderService, times(1)).getActiveMetricProvider();
    }

    /**
     * Test method for
     * {@link MetricProviderController#getMetricProviders(MetricProviderType, Boolean)}.
     *
     * @throws MangleException
     *
     */

    @Test
    public void testGetActiveMetricProviderNull() throws MangleException {
        when(metricProviderService.getActiveMetricProvider()).thenReturn(null);
        ResponseEntity<List<MetricProviderSpec>> response =
                mangleMetricProviderController.getMetricProviders(MetricProviderByStatus.ACTIVE);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody().isEmpty(), true);
        verify(metricProviderService, times(1)).getActiveMetricProvider();
    }

    /**
     * Test method for {@link MetricProviderController#changeMetricProviderStatus(String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testChangeMetricProviderStatus() throws MangleException {
        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        when(this.metricProviderService.enableMetricProviderByName(datadogSpec.getName()))
                .thenReturn(activeMetricProvider);
        ResponseEntity<MetricProviderResponse> response =
                mangleMetricProviderController.changeMetricProviderStatus(datadogSpec.getName());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        MetricProviderResponse mangleResponse = new MetricProviderResponse();
        mangleResponse.setResultStatus(true);
        Assert.assertEquals(response.getBody(), mangleResponse);
        verify(metricProviderService, times(1)).enableMetricProviderByName(datadogSpec.getName());
    }


    /**
     * Test method for {@link MetricProviderController#changeMetricProviderStatus(String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testChangeMetricProviderStatusTrueAlreadyExist() throws MangleException {
        MangleAdminConfigurationSpec activeMetricProvider = mockData.getAdminPropertyForActiveMetricProviderWavefront();
        when(this.metricProviderService.enableMetricProviderByName(datadogSpec.getName()))
                .thenReturn(activeMetricProvider);
        ResponseEntity<MetricProviderResponse> response =
                mangleMetricProviderController.changeMetricProviderStatus(datadogSpec.getName());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        MetricProviderResponse mangleResponse = new MetricProviderResponse();
        mangleResponse.setResultStatus(true);
        Assert.assertEquals(response.getBody(), mangleResponse);
        verify(metricProviderService, times(1)).enableMetricProviderByName(datadogSpec.getName());
    }


    /**
     * Test method for {@link MetricProviderController#changeMetricProviderStatus(String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testChangeMetricProviderStatusNonExist() throws MangleException {
        when(this.metricProviderService.enableMetricProviderByName("someName")).thenReturn(null);
        ResponseEntity<MetricProviderResponse> response =
                mangleMetricProviderController.changeMetricProviderStatus("someName");
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        MetricProviderResponse mangleResponse = new MetricProviderResponse();
        mangleResponse.setResultStatus(true);
        Assert.assertEquals(response.getBody(), mangleResponse);
        verify(metricProviderService, times(1)).enableMetricProviderByName("someName");
    }

    /**
     * Test method for {@link MetricProviderController#changeMangleMetricCollectionStatus(Boolean)}.
     *
     * @throws MangleException
     */

    @Test
    public void testChangeMangleMetricCollectionStatusTrue() throws MangleException {
        when(metricProviderService.sendMetrics()).thenReturn(true);
        ResponseEntity<MetricProviderResponse> response =
                mangleMetricProviderController.changeMangleMetricCollectionStatus(true);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        MetricProviderResponse mangleResponse = new MetricProviderResponse();
        mangleResponse.setResultStatus(true);
        Assert.assertEquals(response.getBody(), mangleResponse);
        verify(metricProviderService, times(1)).sendMetrics();
    }

    /**
     * Test method for {@link MetricProviderController#changeMangleMetricCollectionStatus(Boolean)}.
     *
     * @throws MangleException
     */

    @Test
    public void testChangeMangleMetricCollectionStatusFalse() throws MangleException {
        when(metricProviderService.closeAllMetricCollection()).thenReturn(true);
        ResponseEntity<MetricProviderResponse> response =
                mangleMetricProviderController.changeMangleMetricCollectionStatus(false);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        MetricProviderResponse mangleResponse = new MetricProviderResponse();
        mangleResponse.setResultStatus(true);
        Assert.assertEquals(response.getBody(), mangleResponse);
        verify(metricProviderService, times(1)).closeAllMetricCollection();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.controller.MangleMetricProviderController#getMetricCollectionStatus()}
     */
    @Test
    public void testgetMetricCollectionStatus() {
        when(this.metricProviderService.isMangleMetricsEnabled()).thenReturn(true);
        ResponseEntity<MetricProviderResponse> response = mangleMetricProviderController.getMetricCollectionStatus();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        MetricProviderResponse mangleResponse = new MetricProviderResponse();
        mangleResponse.setResultStatus(true);
        Assert.assertEquals(response.getBody(), mangleResponse);
        verify(metricProviderService, times(1)).isMangleMetricsEnabled();
    }
}
