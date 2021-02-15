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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.resiliencyscore.QueryDto;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreConfigSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreMetricConfig;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.resiliencyscore.Service;
import com.vmware.mangle.model.response.ResiliencyScoreDeleteResponse;
import com.vmware.mangle.services.controller.ResiliencyScoreController;
import com.vmware.mangle.services.helpers.ResiliencyScoreHelper;
import com.vmware.mangle.services.mockdata.ResiliencyScoreMockData;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreMetricConfigService;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreService;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author dbhat
 */
public class ResiliencyScoreControllerTest {
    @Mock
    private ResiliencyScoreService resiliencyScoreService;
    @Mock
    private ResiliencyScoreHelper resiliencyScoreHelper;
    @Mock
    private ResiliencyScoreMetricConfigService resiliencyScoreMetricConfigService;

    private ResiliencyScoreController resiliencyScoreController;

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
        resiliencyScoreController = spy(new ResiliencyScoreController(resiliencyScoreHelper, resiliencyScoreService,
                resiliencyScoreMetricConfigService));
        Link link = mock(Link.class);
        doReturn(link).when(resiliencyScoreController).getSelfLink();
    }

    @Test(priority = 1, description = "Validate creating resiliency score metric config ")
    public void validateAddingMetricConfig() throws MangleException {
        ResiliencyScoreMetricConfig metricConfig = ResiliencyScoreMockData.getResiliencyScoreMetricConfig();
        when(resiliencyScoreMetricConfigService.addMetricConfig(any())).thenReturn(metricConfig);

        ResponseEntity<ResiliencyScoreMetricConfig> response =
                resiliencyScoreController.createResiliencyScoreMetricConfig(metricConfig);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertNotNull(response.getBody());
        verify(resiliencyScoreMetricConfigService, times(1)).addMetricConfig(any());
    }

    @Test(priority = 2, description = "Validate Deleting of resiliency score metric config ")
    public void validateDeletingMetricConfig() throws MangleException {
        ResiliencyScoreMetricConfig metricConfig = ResiliencyScoreMockData.getResiliencyScoreMetricConfig();
        when(resiliencyScoreMetricConfigService.deleteMetricConfig(any())).thenReturn(true);

        ResponseEntity<ResiliencyScoreDeleteResponse> response =
                resiliencyScoreController.deleteResiliencyScoreMetricConfig(metricConfig.getName());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(response.getBody().isResultStatus());
        verify(resiliencyScoreMetricConfigService, times(1)).deleteMetricConfig(any());
    }

    @Test(priority = 3, description = "Validate Getting all the metric configurations defined in the system")
    public void getAllMetricConfig() {
        ResiliencyScoreMetricConfig metricConfig = ResiliencyScoreMockData.getResiliencyScoreMetricConfig();
        List<ResiliencyScoreMetricConfig> configs = new ArrayList<>();
        configs.add(metricConfig);
        when(resiliencyScoreMetricConfigService.getAllResiliencyScoreMetricConfigs()).thenReturn(configs);

        ResponseEntity<List<ResiliencyScoreMetricConfig>> response = resiliencyScoreController.getAllMetricConfigs();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(response.getBody().size() > 0);
        verify(resiliencyScoreMetricConfigService, times(1)).getAllResiliencyScoreMetricConfigs();
    }

    @Test(priority = 4, description = "Validate Getting all the metric configurations defined in the system")
    public void validateUpdateMetricConfig() throws MangleException {
        ResiliencyScoreMetricConfig metricConfig = ResiliencyScoreMockData.getResiliencyScoreMetricConfig();
        when(resiliencyScoreMetricConfigService.updateMetricConfig(metricConfig.getName(), metricConfig))
                .thenReturn(metricConfig);

        ResponseEntity<ResiliencyScoreMetricConfig> response =
                resiliencyScoreController.updateMetricConfig(metricConfig);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody().getName(), metricConfig.getName());
        verify(resiliencyScoreMetricConfigService, times(1)).updateMetricConfig(anyString(), any());
    }

    @Test(priority = 5, description = "Validate Calculate Resiliency score API")
    public void calculateResiliencyScore() throws MangleException {
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        ResiliencyScoreConfigSpec configSpec = ResiliencyScoreMockData.getResiliencyScoreConfigSpec();
        when(resiliencyScoreHelper.calculateResiliencyScore(configSpec)).thenReturn(resiliencyScoreTask);

        ResponseEntity<ResiliencyScoreTask> response = resiliencyScoreController.calculateResiliencyScore(configSpec);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), resiliencyScoreTask);
        verify(resiliencyScoreHelper, times(1)).calculateResiliencyScore(any());
    }

    @Test(priority = 6, description = "Validate get resiliency score by task id")
    public void getResiliencyScoreByTaskID() throws MangleException {
        ResiliencyScoreTask resiliencyScore = ResiliencyScoreMockData.getResiliencyScoreTask1();
        when(resiliencyScoreService.getTaskById(resiliencyScore.getId())).thenReturn(resiliencyScore);

        ResponseEntity<ResiliencyScoreTask> response = resiliencyScoreController.getTask(resiliencyScore.getId());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertFalse(response.getBody().isScheduledTask());
        verify(resiliencyScoreService, times(1)).getTaskById(anyString());
    }

    @Test(priority = 7, description = "Validate get resiliency score by task id when Task ID is Null", expectedExceptions = MangleException.class)
    public void getResiliencyScoreWhenTaskIDIsNull() throws MangleException {
        resiliencyScoreController.getTask(null);
    }

    @Test(priority = 8, description = "Validate get all the resiliency scores ")
    public void getAllResiliencyScores() {
        ResiliencyScoreTask resiliencyScore = ResiliencyScoreMockData.getResiliencyScoreTask1();
        List<ResiliencyScoreTask> scores = new ArrayList<>();
        scores.add(resiliencyScore);
        when(resiliencyScoreService.getAllTasks()).thenReturn(scores);

        ResponseEntity<List<ResiliencyScoreTask>> response = resiliencyScoreController.getAllTasks();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(response.getBody().size() > 0);
        verify(resiliencyScoreService, times(1)).getAllTasks();
    }

    @Test(priority = 9, description = "Validate Delete of Resiliency Score operations")
    public void deleteResiliencyScore() throws MangleException {
        when(resiliencyScoreService.deleteResiliencyScoreById(anyString())).thenReturn(true);

        ResponseEntity<ResiliencyScoreDeleteResponse> response =
                resiliencyScoreController.deleteResiliencyScore(ResiliencyScoreMockData.getRandomUUID());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(response.getBody().isResultStatus());
        verify(resiliencyScoreService, times(1)).deleteResiliencyScoreById(anyString());
    }

    @Test(priority = 10, description = "Validate delete resiliency score when Task ID is Null", expectedExceptions = MangleException.class)
    public void deleteNullResiliencyScoreID() throws MangleException {
        resiliencyScoreController.deleteResiliencyScore(null);
    }

    @Test(priority = 11, description = "Validate delete resiliency score metric config when name is Null", expectedExceptions = MangleException.class)
    public void deleteNullResiliencyScoreMetricConfig() throws MangleException {
        resiliencyScoreController.deleteResiliencyScoreMetricConfig(null);
    }

    @Test(priority = 12, description = "Validate to get all the services ")
    public void getAllServices() throws MangleException {
        Service service = ResiliencyScoreMockData.getServiceProperties();
        List<Service> services = new ArrayList<>();
        services.add(service);
        when(resiliencyScoreService.getAllServices()).thenReturn(services);

        ResponseEntity<Resources<Service>> response = resiliencyScoreController.getAllServices();
        Collection<Service> servicesList = response.getBody().getContent();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(servicesList.size() > 0);
        verify(resiliencyScoreService, times(1)).getAllServices();
    }

    @Test(priority = 13, description = "Validate update service definition")
    public void updateService() throws MangleException {
        Service service = ResiliencyScoreMockData.getServiceProperties();
        Service serviceUpdated = ResiliencyScoreMockData.getServiceProperties();

        when(resiliencyScoreService.updateService(anyString(), any())).thenReturn(serviceUpdated);

        ResponseEntity<Resource<Service>> persisted = resiliencyScoreController.updateService(service);
        verify(resiliencyScoreService, times(1)).updateService(anyString(), any());
        Assert.assertEquals(persisted.getBody().getContent(), serviceUpdated);
    }

    @Test(priority = 14, description = "Delete Service with all valid data")
    public void deleteService() throws MangleException {
        when(resiliencyScoreService.deleteServiceById(anyString())).thenReturn(true);

        ResponseEntity<Resource<ResiliencyScoreDeleteResponse>> persisted =
                resiliencyScoreController.deleteServiceById(ResiliencyScoreMockData.getRandomUUID());

        verify(resiliencyScoreService, times(1)).deleteServiceById(anyString());
    }

    @Test(priority = 15, description = "Validate add Service definition")
    public void addService() throws MangleException {
        Service serviceSpec = ResiliencyScoreMockData.getServiceProperties();
        when(resiliencyScoreService.addOrUpdateService(any())).thenReturn(serviceSpec);

        ResponseEntity<Resource<Service>> response = resiliencyScoreController.addService(serviceSpec);

        Assert.assertEquals(response.getBody().getContent(), serviceSpec);
        verify(resiliencyScoreService, times(1)).addOrUpdateService(any());
    }

    @Test(priority = 16, description = "Validate add QueryDto definition")
    public void addQuery() throws MangleException {
        QueryDto querySpec = ResiliencyScoreMockData.getQueryProperties();
        when(resiliencyScoreService.addQuery(any())).thenReturn(querySpec);

        ResponseEntity<Resource<QueryDto>> response = resiliencyScoreController.addQuery(querySpec);

        Assert.assertEquals(response.getBody().getContent(), querySpec);
        verify(resiliencyScoreService, times(1)).addQuery(any());
    }

    @Test(priority = 17, description = "Delete QueryDto with inValid data")
    public void deleteQueryWithInvalidData() throws MangleException {
        try {
            resiliencyScoreController.deleteQueryById(null);
            Assert.fail("Expected MangleException as the input QueryDto ID is invalid");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.EMPTY_QUERY_ID);
            verify(resiliencyScoreService, times(0)).deleteQuery(anyString());
        }
    }

    @Test(priority = 18, description = "Delete QueryDto with all valid data")
    public void deleteQuery() throws MangleException {
        when(resiliencyScoreService.deleteQuery(anyString())).thenReturn(true);

        ResponseEntity<Resource<ResiliencyScoreDeleteResponse>> persisted =
                resiliencyScoreController.deleteQueryById(ResiliencyScoreMockData.getRandomUUID());

        verify(resiliencyScoreService, times(1)).deleteQuery(anyString());
    }

    @Test(priority = 19, description = "Validate to get all the queries ")
    public void getAllQueries() throws MangleException {
        QueryDto query = ResiliencyScoreMockData.getQueryProperties();
        List<QueryDto> queries = new ArrayList<>();
        queries.add(query);
        when(resiliencyScoreService.getAllQueries()).thenReturn(queries);

        ResponseEntity<Resources<QueryDto>> response = resiliencyScoreController.getAllQueries();
        Collection<QueryDto> queriesList = response.getBody().getContent();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(queriesList.size() > 0);
        verify(resiliencyScoreService, times(1)).getAllQueries();
    }

    @Test(priority = 20, description = "Validate update service definition")
    public void updateQuery() throws MangleException {
        QueryDto query = ResiliencyScoreMockData.getQueryProperties();
        QueryDto queryUpdated = ResiliencyScoreMockData.getQueryProperties();

        when(resiliencyScoreService.updateQuery(anyString(), any())).thenReturn(queryUpdated);

        ResponseEntity<Resource<QueryDto>> persisted = resiliencyScoreController.updateQuery(query);
        verify(resiliencyScoreService, times(1)).updateQuery(anyString(), any());
        Assert.assertEquals(persisted.getBody().getContent(), queryUpdated);
    }
}
