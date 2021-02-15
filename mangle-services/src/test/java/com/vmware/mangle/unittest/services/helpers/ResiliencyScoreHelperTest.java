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

package com.vmware.mangle.unittest.services.helpers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreConfigSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreMetricConfig;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.resiliencyscore.Service;
import com.vmware.mangle.model.resiliencyscore.ResiliencyScoreProperties;
import com.vmware.mangle.services.helpers.MetricProviderHelper;
import com.vmware.mangle.services.helpers.ResiliencyScoreHelper;
import com.vmware.mangle.services.mockdata.MetricProviderMockData;
import com.vmware.mangle.services.mockdata.ResiliencyScoreMockData;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreMetricConfigService;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreService;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author dbhat
 */
public class ResiliencyScoreHelperTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private ResiliencyScoreService resiliencyScoreService;
    @Mock
    private ResiliencyScoreMetricConfigService resiliencyScoreMetricConfigService;
    @Mock
    private MetricProviderHelper metricProviderHelper;

    @InjectMocks
    ResiliencyScoreHelper resiliencyScoreHelper;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(priority = 1, description = "Validate calculate resiliency score when config is null")
    public void runTaskWhenConfigIsNull() {
        try {
            resiliencyScoreHelper.calculateResiliencyScore(null);
            Assert.fail("Expected exception : MangleException is not thrown");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.INVALID_RESILIENCY_SCORE_SPEC);
        }
    }

    @Test(priority = 2, description = "Validate calculate resiliency score when no service family definition is found")
    public void runCalculationWhenNoServiceFamilies() {
        ResiliencyScoreConfigSpec configSpec = new ResiliencyScoreConfigSpec();
        configSpec.setServiceName(null);
        try {
            resiliencyScoreHelper.calculateResiliencyScore(configSpec);
            Assert.fail("Expected exception : MangleException is not thrown");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.SERVICES_CANNOT_BE_EMPTY);
        }
    }

    @Test(priority = 3, description = "Validate calculate resiliency score when no queries are defined")
    public void runCalculationWhenNoQueries() {
        ResiliencyScoreConfigSpec configSpec = ResiliencyScoreMockData.getResiliencyScoreConfigSpec();
        Service service = ResiliencyScoreMockData.getServiceProperties();
        service.setQueryNames(null);
        when(resiliencyScoreService.getServiceByName(anyString())).thenReturn(service);
        try {
            resiliencyScoreHelper.calculateResiliencyScore(configSpec);
            Assert.fail("Expected exception : MangleException is not thrown");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.QUERY_CONDITIONS_CANNOT_BE_EMPTY);
        }
    }

    @Test(priority = 4, description = "Calculate score with all valid data")
    public void runCalculation() throws MangleException {
        ResiliencyScoreConfigSpec configSpec = ResiliencyScoreMockData.getResiliencyScoreConfigSpec();
        Service service = ResiliencyScoreMockData.getServiceProperties();
        ResiliencyScoreTask task = ResiliencyScoreMockData.getResiliencyScoreTask1();

        when(resiliencyScoreService.getServiceByName(anyString())).thenReturn(service);
        when(resiliencyScoreService.addOrUpdateTask(any())).thenReturn(task);
        doNothing().when(applicationEventPublisher).publishEvent(any());

        ResiliencyScoreTask resiliencyScoreTask = resiliencyScoreHelper.calculateResiliencyScore(configSpec);
        Assert.assertNotNull(resiliencyScoreTask.getId(), "Calculation has resulted in error");
    }

    @Test(priority = 5, description = "Retrieve resiliency score config spec")
    public void validateResiliencyScoreConfigSpec() {
        ResiliencyScoreMetricConfig metricConfig = ResiliencyScoreMockData.getResiliencyScoreMetricConfig();
        MetricProviderMockData metricProviderMockData = new MetricProviderMockData();
        MetricProviderSpec spec = metricProviderMockData.metricProviderWavefront();

        when(resiliencyScoreMetricConfigService.getResiliencyScoreMetricConfig()).thenReturn(metricConfig);
        when(metricProviderHelper.getActiveMetricProvider()).thenReturn(spec);

        ResiliencyScoreProperties resiliencyScoreProperties = resiliencyScoreHelper.getResiliencyScoreTaskSpec();
        Assert.assertEquals(resiliencyScoreProperties.getMetricProviderSpec().getName(), spec.getName());
    }

}
