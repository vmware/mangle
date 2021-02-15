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

package com.vmware.mangle.unittest.services.service.resiliencyscore;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreMetricConfig;
import com.vmware.mangle.services.mockdata.MockDataConstants;
import com.vmware.mangle.services.mockdata.ResiliencyScoreMockData;
import com.vmware.mangle.services.repository.ResiliencyScoreMetricConfigRepository;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreMetricConfigService;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author dbhat
 */
public class ResiliencyScoreMetricConfigServiceTest {
    @Mock
    private ResiliencyScoreMetricConfigRepository repository;
    @InjectMocks
    private ResiliencyScoreMetricConfigService metricConfigService;

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(priority = 1, description = "Validate add metric config to DB with all valid Data")
    public void addMetricConfig() throws MangleException {
        ResiliencyScoreMetricConfig metricConfig = ResiliencyScoreMockData.getResiliencyScoreMetricConfig();
        when(repository.findAll()).thenReturn(new ArrayList<>());
        when(repository.save(metricConfig)).thenReturn(metricConfig);

        ResiliencyScoreMetricConfig savedConfig = metricConfigService.addMetricConfig(metricConfig);
        Assert.assertEquals(savedConfig.getName(), metricConfig.getName());
    }

    @Test(priority = 2, description = "Validate add metric config to DB when config is already present")
    public void addMetricConfigWhenConfigAlreadyAvailable() {
        ResiliencyScoreMetricConfig metricConfig = ResiliencyScoreMockData.getResiliencyScoreMetricConfig();
        List<ResiliencyScoreMetricConfig> configsInDb = new ArrayList<>();
        configsInDb.add(metricConfig);

        when(repository.findAll()).thenReturn(configsInDb);
        try {
            metricConfigService.addMetricConfig(metricConfig);
            Assert.fail("Expected Mangle exception is not Thrown");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.SAME_RECORD_EXISTS);
        }
    }

    @Test(priority = 3, description = "Validate add metric config to DB when config is null")
    public void addMetricConfigWhenConfigIsNull() {
        try {
            metricConfigService.addMetricConfig(null);
            Assert.fail("Expected Mangle exception is not Thrown");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
        }
    }

    @Test(priority = 4, description = "Validate update metric config in Db when all valid Data")
    public void updateMetricConfigData() throws MangleException {
        ResiliencyScoreMetricConfig metricConfig = ResiliencyScoreMockData.getResiliencyScoreMetricConfig();

        Optional<ResiliencyScoreMetricConfig> optional = Optional.of(metricConfig);
        when(repository.findByName(metricConfig.getName())).thenReturn(optional);
        when(repository.save(metricConfig)).thenReturn(metricConfig);

        ResiliencyScoreMetricConfig saved =
                metricConfigService.updateMetricConfig(metricConfig.getName(), metricConfig);
        Assert.assertEquals(saved.getName(), metricConfig.getName());
    }

    @Test(priority = 5, description = "Validate update metric config in Db when specified config is not present in DB")
    public void updateMetricConfigDataWhenConfigNotExists() throws MangleException {
        ResiliencyScoreMetricConfig metricConfig = ResiliencyScoreMockData.getResiliencyScoreMetricConfig();
        Optional<ResiliencyScoreMetricConfig> optional = Optional.empty();
        when(repository.findByName(metricConfig.getName())).thenReturn(optional);
        when(repository.save(metricConfig)).thenReturn(metricConfig);
        try {
            metricConfigService.updateMetricConfig(metricConfig.getName(), metricConfig);
            Assert.fail("Expected MangleRunTime exception is not Thrown");
        } catch (MangleRuntimeException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
        }
    }

    @Test(priority = 6, description = "Validate update metric config in Db when specified config name is null")
    public void updateMetricConfigDataWhenConfigNameIsNull() throws MangleRuntimeException {
        ResiliencyScoreMetricConfig metricConfig = ResiliencyScoreMockData.getResiliencyScoreMetricConfig();
        metricConfig.setName(null);
        try {
            metricConfigService.updateMetricConfig(metricConfig.getName(), metricConfig);
            Assert.fail("Expected Mangle exception is not Thrown");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
        }
    }

    @Test(priority = 7, description = "Validate update metric config in Db when specified config is null")
    public void updateMetricConfigDataWhenConfigIsNull() throws MangleRuntimeException, MangleException {
        try {
            metricConfigService.updateMetricConfig(MockDataConstants.RESILIENCY_SCORE_METRIC_CONFIG_METRIC_NAME, null);
            Assert.fail("Expected Mangle exception is not thrown.");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
        }
    }

    @Test(priority = 8, description = "Validate Delete metric config in Db when specified config is null")
    public void deleteMetricConfigDataWhenConfigIsNull() throws MangleRuntimeException {
        try {
            metricConfigService.deleteMetricConfig(null);
            Assert.fail("Expected Mangle exception is not thrown.");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
        }
    }

    @Test(priority = 9, description = "Validate Delete metric config in Db when specified config is not present in DB")
    public void deleteMetricConfigDataWhenConfigNotExists() throws MangleException {
        ResiliencyScoreMetricConfig metricConfig = ResiliencyScoreMockData.getResiliencyScoreMetricConfig();
        Optional<ResiliencyScoreMetricConfig> optional = Optional.empty();
        when(repository.findByName(metricConfig.getName())).thenReturn(optional);
        try {
            metricConfigService.deleteMetricConfig(metricConfig.getName());
            Assert.fail("Expected MangleRuntimeException is not thrown.");
        } catch (MangleRuntimeException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
        }
    }

    @Test(priority = 10, description = "Validate delete metric config in Db when all valid Data")
    public void deleteMetricConfig() throws MangleException {
        ResiliencyScoreMetricConfig metricConfig = ResiliencyScoreMockData.getResiliencyScoreMetricConfig();

        Optional<ResiliencyScoreMetricConfig> optional = Optional.of(metricConfig);
        Optional<ResiliencyScoreMetricConfig> optionalEmpty = Optional.empty();
        when(repository.findByName(metricConfig.getName())).thenReturn(optional).thenReturn(optionalEmpty);
        Mockito.doNothing().when(repository).deleteByName(metricConfig.getName());

        boolean status = metricConfigService.deleteMetricConfig(metricConfig.getName());
        Assert.assertTrue(status, "Deleting was successful");
    }

    @Test(priority = 11, description = "Validate delete metric config in Db has failed")
    public void deleteMetricConfigFailure() throws MangleException {
        ResiliencyScoreMetricConfig metricConfig = ResiliencyScoreMockData.getResiliencyScoreMetricConfig();

        Optional<ResiliencyScoreMetricConfig> optional = Optional.of(metricConfig);
        when(repository.findByName(metricConfig.getName())).thenReturn(optional);
        Mockito.doNothing().when(repository).deleteByName(metricConfig.getName());

        boolean status = metricConfigService.deleteMetricConfig(metricConfig.getName());
        Assert.assertFalse(status, "Deleting was successful");
    }

    @Test(priority = 12, description = "Validate retrieval of metric config data")
    public void getMetricConfigData() {
        ResiliencyScoreMetricConfig metricConfig = ResiliencyScoreMockData.getResiliencyScoreMetricConfig();
        List<ResiliencyScoreMetricConfig> allConfig = new ArrayList<>();
        allConfig.add(metricConfig);
        when(repository.findAll()).thenReturn(allConfig);

        List<ResiliencyScoreMetricConfig> configsRetrieved = metricConfigService.getAllResiliencyScoreMetricConfigs();
        Assert.assertTrue(configsRetrieved.size() > 0, "Retrieval was failure");
    }

    @Test(priority = 13, description = "Validate retrieval of metric config data when no data in DB")
    public void getMetricConfigDataWhenNoDataInDb() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        List<ResiliencyScoreMetricConfig> configsRetrieved = metricConfigService.getAllResiliencyScoreMetricConfigs();
        Assert.assertTrue(configsRetrieved.isEmpty(), "Retrieval was failure");
    }
}
