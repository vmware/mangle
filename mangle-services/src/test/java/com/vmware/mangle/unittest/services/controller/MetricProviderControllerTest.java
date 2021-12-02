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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
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

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);

        mangleMetricProviderController = spy(new MetricProviderController(metricProviderService, publisher));

        publisher = new CustomEventPublisher(applicationEventPublisher, eventService);
        when(eventService.save(any())).then(new ReturnsArgumentAt(0));
        datadogSpec = mockData.metricProviderDatadog();
        wavefrontSpec = mockData.metricProviderWavefront();

        Link link = mock(Link.class);
        doReturn(link).when(mangleMetricProviderController).getSelfLink();

    }

    @AfterClass
    public void tearDownAfterClass() {
        this.mockData = null;
        this.publisher = null;
        this.datadogSpec = null;
        this.wavefrontSpec = null;
    }

    @AfterTest
    public void tearDown() {
        validateMockitoUsage();
    }

    /**
     * Test method for {@link MetricProviderController#getMetricProviders(MetricProviderByStatus)}
     *
     * @throws MangleException
     */
    @Test
    public void getMetricProviderTest() throws MangleException {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        metricProviderSpecList.add(wavefrontSpec);
        metricProviderSpecList.add(datadogSpec);
        when(metricProviderService.getAllMetricProviders()).thenReturn(metricProviderSpecList);
        ResponseEntity<Resources<MetricProviderSpec>> response =
                mangleMetricProviderController.getMetricProviders(null);

        Resources<MetricProviderSpec> resources = response.getBody();
        Assert.assertNotNull(resources);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Collection<MetricProviderSpec> responseList = resources.getContent();
        Assert.assertEquals(responseList.size(), 2);
        Assert.assertEquals(responseList.iterator().next(), wavefrontSpec);
        verify(metricProviderService, times(1)).getAllMetricProviders();
    }

    /**
     * Test method for {@link MetricProviderController#getMetricProviders(MetricProviderByStatus)} .
     *
     * @throws MangleException
     */
    @Test
    public void getMetricProviderTestNull() throws MangleException {
        List<MetricProviderSpec> metricProviderSpecList = new ArrayList<>();
        when(metricProviderService.getAllMetricProviders()).thenReturn(metricProviderSpecList);
        ResponseEntity<Resources<MetricProviderSpec>> response =
                mangleMetricProviderController.getMetricProviders(null);

        Resources<MetricProviderSpec> resources = response.getBody();
        Assert.assertNotNull(resources);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Collection<MetricProviderSpec> responseList = resources.getContent();
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
        try {
            mangleMetricProviderController.addMetricProvider(wavefrontSpec);
        } catch (MangleException exception) {
            verify(metricProviderService, times(1)).getMetricProviderByType(MetricProviderType.WAVEFRONT);
        }
    }

    /**
     * Test method for {@link MetricProviderController#addMetricProvider(MetricProviderSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testAddMetricProvider() throws MangleException {
        when(metricProviderService.getMetricProviderByType(MetricProviderType.WAVEFRONT)).thenReturn(new ArrayList<>());
        when(metricProviderService.testConnectionMetricProvider(wavefrontSpec)).thenReturn(true);
        when(metricProviderService.addMetricProvider(wavefrontSpec)).thenReturn(wavefrontSpec);
        when(metricProviderService.normalizeURI(wavefrontSpec)).thenReturn(wavefrontSpec);
        ResponseEntity<Resource<MetricProviderSpec>> response =
                mangleMetricProviderController.addMetricProvider(wavefrontSpec);

        Resource<MetricProviderSpec> resource = response.getBody();
        Assert.assertNotNull(resource);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        MetricProviderSpec resourceContent = resource.getContent();

        Assert.assertEquals(resourceContent, wavefrontSpec);
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
        when(metricProviderService.getMetricProviderByType(MetricProviderType.WAVEFRONT)).thenReturn(new ArrayList<>());
        when(metricProviderService.testConnectionMetricProvider(wavefrontSpec)).thenReturn(false);
        when(metricProviderService.addMetricProvider(wavefrontSpec)).thenReturn(wavefrontSpec);
        when(metricProviderService.normalizeURI(wavefrontSpec)).thenReturn(wavefrontSpec);
        ResponseEntity<Resource<MetricProviderSpec>> response =
                mangleMetricProviderController.addMetricProvider(wavefrontSpec);
        Resource<MetricProviderSpec> resource = response.getBody();
        Assert.assertNotNull(resource);
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
        ResponseEntity<Resource<MetricProviderSpec>> response =
                mangleMetricProviderController.updateMetricProvider(wavefrontSpec);

        Resource<MetricProviderSpec> resource = response.getBody();
        Assert.assertNotNull(resource);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(resource.getContent(), wavefrontSpec);
        verify(metricProviderService, times(1)).testConnectionMetricProvider(wavefrontSpec);
        verify(metricProviderService, times(1)).updateMetricProviderByName(wavefrontSpec.getName(), wavefrontSpec);
    }

    /**
     * Test method for {@link MetricProviderController#updateMetricProvider(MetricProviderSpec)}.
     *
     * @throws MangleException
     */

    @Test
    public void testUpdateMetricProviderWrongData() throws MangleException {

        when(metricProviderService.testConnectionMetricProvider(mockData.wrongSpec())).thenReturn(false);
        when(metricProviderService.updateMetricProviderByName(wavefrontSpec.getName(), wavefrontSpec))
                .thenReturn(wavefrontSpec);
        ResponseEntity<Resource<MetricProviderSpec>> response =
                mangleMetricProviderController.updateMetricProvider(wavefrontSpec);

        Resource<MetricProviderSpec> resource = response.getBody();
        Assert.assertNotNull(resource);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(resource.getContent(), wavefrontSpec);

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
        ResponseEntity<Void> response = mangleMetricProviderController.deleteMetricProvider(wavefrontSpec.getName());
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
        ResponseEntity<Void> response = mangleMetricProviderController.deleteMetricProvider(wavefrontSpec.getName());
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
        ResponseEntity<Void> response = mangleMetricProviderController.deleteMetricProvider("");
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
        ResponseEntity<Void> response = mangleMetricProviderController.deleteMetricProvider("");
        Assert.assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
        verify(metricProviderService, times(1)).deleteAllMetricProviders();
    }

    /**
     * Test method for {@link MetricProviderController#testConnection(MetricProviderSpec)}
     *
     * @throws MangleException
     */

    @Test
    public void testTestConnection() throws MangleException {
        when(metricProviderService.testConnectionMetricProvider(wavefrontSpec)).thenReturn(true);
        ResponseEntity<Resource<MetricProviderResponse>> response =
                mangleMetricProviderController.testConnection(wavefrontSpec);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        MetricProviderResponse mangleResponse = new MetricProviderResponse();
        mangleResponse.setResultStatus(true);

        Resource<MetricProviderResponse> resource = response.getBody();
        Assert.assertNotNull(resource);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);

        Assert.assertEquals(resource.getContent(), mangleResponse);
        verify(metricProviderService, times(1)).testConnectionMetricProvider(wavefrontSpec);
    }

    /**
     * Test method for {@link MetricProviderController#testConnection(MetricProviderSpec)}
     *
     * @throws MangleException
     */

    @Test
    public void testTestConnectionFailure() throws MangleException {
        when(metricProviderService.testConnectionMetricProvider(wavefrontSpec)).thenReturn(false);
        try {
            mangleMetricProviderController.testConnection(wavefrontSpec);
        } catch (MangleException exception) {
            verify(metricProviderService, times(1)).testConnectionMetricProvider(wavefrontSpec);
        }
    }

    /**
     * Test method for {@link MetricProviderController#getMetricProviders(MetricProviderByStatus)}
     *
     * @throws MangleException
     *
     */

    @Test
    public void testGetActiveMetricProvider() throws MangleException {
        when(metricProviderService.getActiveMetricProvider()).thenReturn(wavefrontSpec);
        ResponseEntity<Resources<MetricProviderSpec>> response =
                mangleMetricProviderController.getMetricProviders(MetricProviderByStatus.ACTIVE);

        Resources<MetricProviderSpec> resource = response.getBody();
        Assert.assertNotNull(resource);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Collection<MetricProviderSpec> specs = resource.getContent();
        Assert.assertEquals(specs.iterator().next().getName(), wavefrontSpec.getName());
        verify(metricProviderService, times(1)).getActiveMetricProvider();
    }

    /**
     * Test method for {@link MetricProviderController#getMetricProviders(MetricProviderByStatus)}
     *
     * @throws MangleException
     *
     */

    @Test
    public void testGetActiveMetricProviderNull() throws MangleException {
        when(metricProviderService.getActiveMetricProvider()).thenReturn(null);
        ResponseEntity<Resources<MetricProviderSpec>> response =
                mangleMetricProviderController.getMetricProviders(MetricProviderByStatus.ACTIVE);

        Resources<MetricProviderSpec> resource = response.getBody();
        Assert.assertNotNull(resource);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(resource.getContent().isEmpty());
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
        ResponseEntity<Resource<MetricProviderResponse>> response =
                mangleMetricProviderController.changeMetricProviderStatus(datadogSpec.getName());

        Resource<MetricProviderResponse> resource = response.getBody();
        Assert.assertNotNull(resource);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        MetricProviderResponse mangleResponse = new MetricProviderResponse();
        mangleResponse.setResultStatus(true);
        Assert.assertEquals(resource.getContent(), mangleResponse);
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

        ResponseEntity<Resource<MetricProviderResponse>> response =
                mangleMetricProviderController.changeMetricProviderStatus(datadogSpec.getName());

        Resource<MetricProviderResponse> resource = response.getBody();
        Assert.assertNotNull(resource);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(resource.getContent().isResultStatus());
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
        ResponseEntity<Resource<MetricProviderResponse>> response =
                mangleMetricProviderController.changeMetricProviderStatus("someName");

        Resource<MetricProviderResponse> resource = response.getBody();
        Assert.assertNotNull(resource);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(resource.getContent().isResultStatus());
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
        ResponseEntity<Resource<MetricProviderResponse>> response =
                mangleMetricProviderController.changeMangleMetricCollectionStatus(true);

        Resource<MetricProviderResponse> resource = response.getBody();
        Assert.assertNotNull(resource);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(resource.getContent().isResultStatus());
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
        ResponseEntity<Resource<MetricProviderResponse>> response =
                mangleMetricProviderController.changeMangleMetricCollectionStatus(false);

        Resource<MetricProviderResponse> resource = response.getBody();
        Assert.assertNotNull(resource);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(resource.getContent().isResultStatus());
        verify(metricProviderService, times(1)).closeAllMetricCollection();
    }

    /**
     * Test method for {@link MetricProviderController#getMetricCollectionStatus()}
     */
    @Test
    public void testgetMetricCollectionStatus() throws MangleException {
        when(this.metricProviderService.isMangleMetricsEnabled()).thenReturn(true);
        ResponseEntity<Resource<MetricProviderResponse>> response =
                mangleMetricProviderController.getMetricCollectionStatus();

        Resource<MetricProviderResponse> resource = response.getBody();
        Assert.assertNotNull(resource);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(resource.getContent().isResultStatus());
        verify(metricProviderService, times(1)).isMangleMetricsEnabled();
    }
}
