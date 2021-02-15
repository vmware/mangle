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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.QueryDto;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.resiliencyscore.Service;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerInfo;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.model.ResiliencyScoreVO;
import com.vmware.mangle.model.resiliencyscore.ResiliencyScoreProperties;
import com.vmware.mangle.services.helpers.ResiliencyScoreHelper;
import com.vmware.mangle.services.helpers.ResiliencyScoreTaskHelper;
import com.vmware.mangle.services.mockdata.ResiliencyScoreMockData;
import com.vmware.mangle.services.mockdata.SchedulerControllerMockData;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreService;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreTaskExecutor;
import com.vmware.mangle.services.scheduler.Scheduler;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author dbhat
 */
public class ResiliencyScoreTaskExecutorTest {
    @Mock
    private ResiliencyScoreService resiliencyScoreService;
    @Mock
    private ResiliencyScoreHelper resiliencyScoreHelper;
    private ResiliencyScoreTaskExecutor taskExecutor1;
    private ResiliencyScoreProperties resiliencyScoreProperties;
    private Task<? extends TaskSpec> task;
    private Service service;
    private QueryDto query;
    @Mock
    private Scheduler scheduler;
    @InjectMocks
    private ResiliencyScoreTaskExecutor taskExecutor;

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
        resiliencyScoreProperties = ResiliencyScoreMockData.getResiliencyScoreProperties();
        task = ResiliencyScoreMockData.getTask();
        service = ResiliencyScoreMockData.getServiceProperties();
        query = ResiliencyScoreMockData.getQueryProperties();
    }

    @Test(description = "Validating the task execution when Task ID is null")
    public void runTaskWhenTaskIsNull() {
        Task<? extends TaskSpec> task1 = ResiliencyScoreMockData.getTask();
        task1.setId(null);
        ResiliencyScoreTaskExecutor executor = new ResiliencyScoreTaskExecutor();
        try {
            executor.runTask(task1);
            Assert.fail("Expected exception MangleRunTimeException is not thrown.");
        } catch (MangleRuntimeException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.NO_TASK_FOUND);
        }
    }

    @Test(dependsOnMethods = { "runTaskWhenTaskIsNull" })
    public void runResiliencyCalculation() throws MangleException {
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreService.addOrUpdateTask(resiliencyScoreTask)).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreHelper.getResiliencyScoreTaskSpec()).thenReturn(resiliencyScoreProperties);

        taskExecutor.runTask(task);
        Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getId(), resiliencyScoreTask.getId());
        Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getStartTime(), resiliencyScoreTask.getStartTime());
    }

    @Test(dependsOnMethods = {
            "runResiliencyCalculation" }, description = "Run resiliency score calculation when NO active metric providers are present in the system")
    public void runTaskWhenNoMetricProviderAvailable() throws MangleException {
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        resiliencyScoreProperties.setMetricProviderSpec(null);
        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreService.addOrUpdateTask(resiliencyScoreTask)).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreHelper.getResiliencyScoreTaskSpec()).thenReturn(resiliencyScoreProperties);

        taskExecutor.runTask(task);
        Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getId(), resiliencyScoreTask.getId());
        Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getStartTime(), resiliencyScoreTask.getStartTime());
        Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getTaskStatus(), TaskStatus.FAILED);
    }

    @Test(dependsOnMethods = {
            "runTaskWhenNoMetricProviderAvailable" }, description = "Run resiliency score calculation when NO resiliency score metric configurations are defined")
    public void runTaskWhenNoMetricConfigAvailable() throws MangleException {
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        resiliencyScoreProperties.setResiliencyScoreMetricConfig(null);
        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreService.addOrUpdateTask(resiliencyScoreTask)).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreHelper.getResiliencyScoreTaskSpec()).thenReturn(resiliencyScoreProperties);

        taskExecutor.runTask(task);
        Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getId(), resiliencyScoreTask.getId());
        Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getStartTime(), resiliencyScoreTask.getStartTime());
        Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getTaskStatus(), TaskStatus.FAILED);
    }

    @Test(dependsOnMethods = {
            "runTaskWhenNoMetricConfigAvailable" }, description = "Run resiliency score calculation when NO service configurations are defined")
    public void runTaskWhenNoServiceDefined() throws MangleException {
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        resiliencyScoreProperties = ResiliencyScoreMockData.getResiliencyScoreProperties();

        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreService.addOrUpdateTask(resiliencyScoreTask)).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreService.getServiceByName(anyString())).thenReturn(null);
        when(resiliencyScoreHelper.getResiliencyScoreTaskSpec()).thenReturn(resiliencyScoreProperties);

        taskExecutor.runTask(task);
        verify(resiliencyScoreService, times(2)).addOrUpdateTask(resiliencyScoreTask);
        Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getId(), resiliencyScoreTask.getId());
        Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getStartTime(), resiliencyScoreTask.getStartTime());
        Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getTaskStatus(), TaskStatus.FAILED);
    }

    @Test(dependsOnMethods = {
            "runTaskWhenNoServiceDefined" }, description = "Validate exception while persisting the data")
    public void validateFailureInPersistingData() throws MangleException {
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        resiliencyScoreProperties = ResiliencyScoreMockData.getResiliencyScoreProperties();
        Service service = ResiliencyScoreMockData.getServiceProperties();

        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreService.addOrUpdateTask(resiliencyScoreTask)).thenReturn(resiliencyScoreTask)
                .thenReturn(resiliencyScoreTask)
                .thenThrow(new MangleRuntimeException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.TASK));
        when(resiliencyScoreService.getServiceByName(anyString())).thenReturn(service);
        when(resiliencyScoreHelper.getResiliencyScoreTaskSpec()).thenReturn(resiliencyScoreProperties);
        when(resiliencyScoreService.getLastUpdatedValueOfQuery(anyString())).thenReturn(query);

        try {
            taskExecutor.runTask(task);
            Assert.fail("Expected exception: MangleRunTimeException is not thrown");
        } catch (MangleRuntimeException mangleException) {
            verify(resiliencyScoreService, times(3)).addOrUpdateTask(resiliencyScoreTask);
            verify(resiliencyScoreService, times(1)).getLastUpdatedValueOfQuery(anyString());
            Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getId(), resiliencyScoreTask.getId());
            Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getStartTime(),
                    resiliencyScoreTask.getStartTime());
            Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getTaskStatus(), TaskStatus.FAILED);
        }
    }

    @Test(dependsOnMethods = { "validateFailureInPersistingData" }, description = "Validate Send metric data")
    public void validateSendMetricWhenRScoreIsInvalid() throws MangleException {
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        Task<? extends TaskSpec> task = ResiliencyScoreMockData.getTask();
        resiliencyScoreProperties = ResiliencyScoreMockData.getResiliencyScoreProperties();
        Service service = ResiliencyScoreMockData.getServiceProperties();

        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreService.addOrUpdateTask(resiliencyScoreTask)).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreService.getServiceByName(anyString())).thenReturn(service);
        when(resiliencyScoreHelper.getResiliencyScoreTaskSpec()).thenReturn(resiliencyScoreProperties);
        when(resiliencyScoreService.getLastUpdatedValueOfQuery(anyString())).thenReturn(query);

        taskExecutor.submitTask(task);
        verify(resiliencyScoreService, times(3)).addOrUpdateTask(resiliencyScoreTask);
        verify(resiliencyScoreService, times(1)).getLastUpdatedValueOfQuery(anyString());
    }

    @Test(dependsOnMethods = { "validateSendMetricWhenRScoreIsInvalid" }, description = "Validate Schedule Task")
    public void validateScheduleCronTask() throws MangleException {
        SchedulerControllerMockData schedulerMockData = new SchedulerControllerMockData();
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        resiliencyScoreTask.getTaskData().setSchedule(schedulerMockData.getSchedulerInfo());
        ResiliencyScoreTaskHelper helper = new ResiliencyScoreTaskHelper();
        Task<? extends TaskSpec> task = helper.init(resiliencyScoreTask);
        task.setScheduledTask(true);

        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(resiliencyScoreTask);
        when(scheduler.scheduleCronTask(any(), any())).thenReturn(null);
        when(resiliencyScoreService.addOrUpdateTask(resiliencyScoreTask)).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreHelper.getResiliencyScoreTaskSpec()).thenReturn(resiliencyScoreProperties);

        taskExecutor.submitTask(task);
        verify(scheduler, times(1)).scheduleCronTask(any(), any());
        verify(scheduler, times(0)).scheduleSimpleTask(any(), any());
    }

    @Test(dependsOnMethods = { "validateScheduleCronTask" }, description = "Validate Schedule Task")
    public void validateScheduleSimpleTask() throws MangleException {
        SchedulerControllerMockData schedulerMockData = new SchedulerControllerMockData();
        resiliencyScoreProperties = ResiliencyScoreMockData.getResiliencyScoreProperties();
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        SchedulerInfo schedule = schedulerMockData.getSchedulerInfo();
        schedule.setCronExpression(null);
        schedule.setTimeInMilliseconds(System.currentTimeMillis());
        resiliencyScoreTask.getTaskData().setSchedule(schedule);
        ResiliencyScoreTaskHelper helper = new ResiliencyScoreTaskHelper();
        Task<? extends TaskSpec> task = helper.init(resiliencyScoreTask);
        task.setScheduledTask(true);

        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(resiliencyScoreTask);
        when(scheduler.scheduleCronTask(any(), any())).thenReturn(null);
        when(resiliencyScoreService.addOrUpdateTask(resiliencyScoreTask)).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreHelper.getResiliencyScoreTaskSpec()).thenReturn(resiliencyScoreProperties);

        taskExecutor.submitTask(task);
        verify(scheduler, times(0)).scheduleCronTask(any(), any());
        verify(scheduler, times(1)).scheduleSimpleTask(any(), any());
    }

    @Test(dependsOnMethods = { "validateScheduleSimpleTask" })
    public void validateSendMetric() {
        ResiliencyScoreVO resiliencyScoreVO = ResiliencyScoreMockData.getResiliencyScoreVO();
        Service service = ResiliencyScoreMockData.getServiceProperties();
        taskExecutor.setService(service);
        taskExecutor.setProperties(resiliencyScoreProperties);

        boolean status = taskExecutor.sendResiliencyScoreMetric(resiliencyScoreVO);
        Assert.assertTrue(status);
    }

    @Test(dependsOnMethods = {
            "validateSendMetric" }, description = "Run resiliency score calculation when NO Query definitions are defined")
    public void runTaskWhenNoQueriesDefined() throws MangleException {
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        resiliencyScoreProperties = ResiliencyScoreMockData.getResiliencyScoreProperties();

        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreService.addOrUpdateTask(resiliencyScoreTask)).thenReturn(resiliencyScoreTask);
        when(resiliencyScoreService.getServiceByName(anyString())).thenReturn(service);
        when(resiliencyScoreHelper.getResiliencyScoreTaskSpec()).thenReturn(resiliencyScoreProperties);
        when(resiliencyScoreService.getLastUpdatedValueOfQuery(anyString())).thenReturn(query);

        taskExecutor.runTask(task);
        verify(resiliencyScoreService, times(3)).addOrUpdateTask(resiliencyScoreTask);
        verify(resiliencyScoreService, times(1)).getLastUpdatedValueOfQuery(anyString());
        Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getId(), resiliencyScoreTask.getId());
        Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getStartTime(), resiliencyScoreTask.getStartTime());
        Assert.assertEquals(taskExecutor.getResiliencyScoreTask().getTaskStatus(), TaskStatus.FAILED);
    }

}
