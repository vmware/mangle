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

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.scheduler.ScheduledTaskStatus;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.model.enums.OperationStatus;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.model.response.DeleteSchedulerResponse;
import com.vmware.mangle.services.controller.SchedulerController;
import com.vmware.mangle.services.mockdata.SchedulerControllerMockData;
import com.vmware.mangle.services.scheduler.Scheduler;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author ashrimali
 *
 */

@PowerMockIgnore(value = { "org.apache.logging.log4j.*" })
public class SchedulerControllerTest {

    @Mock
    private Scheduler scheduler;


    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @InjectMocks
    private SchedulerController schedulerController;

    private SchedulerControllerMockData schedulerControllerMockData =
            new SchedulerControllerMockData();


    /**
     * Before Method to initialize mocks
     */
    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for {@link SchedulerController#cancelScheduledJobs(List)}
     */
    @Test
    public void cancelScheduledJobs() {
        List<String> jobIds = schedulerControllerMockData.getJobIds();
        Map<String, ScheduledTaskStatus> scheduledStatusMap =
                schedulerControllerMockData.getCancelledScheduledStatusMap();
        when(scheduler.cancelScheduledJobs(jobIds)).thenReturn(scheduledStatusMap);
        ResponseEntity<Map<String, ScheduledTaskStatus>> response =
                schedulerController.cancelScheduledJobs(jobIds);
        Mockito.verify(scheduler, Mockito.atLeastOnce()).cancelScheduledJobs(Mockito.any());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    /**
     * Test method for {@link SchedulerController#pauseScheduledJobs(List)}
     */
    @Test
    public void pauseScheduledJobs() {
        List<String> jobIds = schedulerControllerMockData.getJobIds();
        Map<String, ScheduledTaskStatus> scheduledStatusMap =
                schedulerControllerMockData.getPausedScheduledStatusMap();
        when(scheduler.pauseScheduledJobs(jobIds)).thenReturn(scheduledStatusMap);
        ResponseEntity<Map<String, ScheduledTaskStatus>> response =
                schedulerController.pauseScheduledJobs(jobIds);
        Mockito.verify(scheduler, Mockito.atLeastOnce()).pauseScheduledJobs(Mockito.any());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    /**
     * Test method for {@link SchedulerController#resumeScheduledJobs(List)}
     */
    @Test
    public void resumeScheduledJobs() {
        List<String> jobIds = schedulerControllerMockData.getJobIds();
        Map<String, ScheduledTaskStatus> scheduledStatusMap =
                schedulerControllerMockData.getResumedScheduledStatusMap();
        when(scheduler.resumeJobs(jobIds)).thenReturn(scheduledStatusMap);
        ResponseEntity<Map<String, ScheduledTaskStatus>> response =
                schedulerController.resumeScheduledJobs(jobIds);
        Mockito.verify(scheduler, Mockito.atLeastOnce()).resumeJobs(Mockito.any());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    /**
     * Test method for {@link SchedulerController#cancelAllScheduledJobs()}
     *
     * @throws MangleException
     */
    @Test
    public void cancelAllScheduledJobs() throws MangleException {
        Map<String, ScheduledTaskStatus> scheduledStatusMap =
                schedulerControllerMockData.getCancelledScheduledStatusMap();
        when(scheduler.cancelAllScheduledJobs()).thenReturn(scheduledStatusMap);
        ResponseEntity<Map<String, ScheduledTaskStatus>> response = schedulerController.cancelAllScheduledJobs();
        Mockito.verify(scheduler, Mockito.times(1)).cancelAllScheduledJobs();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    /**
     * Test method for {@link SchedulerController#deleteScheduledJob(List, boolean)}
     */
    @Test
    public void deleteScheduledJob() throws MangleException {
        List<String> jobIds = schedulerControllerMockData.getJobIds();
        when(scheduler.deletScheduledJob(jobIds)).thenReturn(OperationStatus.SUCCESS);
        ResponseEntity<Map<String, DeleteSchedulerResponse>> response =
                schedulerController.deleteScheduledJob(jobIds, false);
        Mockito.verify(scheduler, Mockito.atLeastOnce()).deleteScheduledJobs(Mockito.any(), anyBoolean());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testDeleteSchedulerJobDeleteTasks() throws MangleException {
        List<String> jobIds = schedulerControllerMockData.getJobIds();
        when(scheduler.deletScheduledJob(jobIds)).thenReturn(OperationStatus.SUCCESS);

        ResponseEntity<Map<String, DeleteSchedulerResponse>> response =
                schedulerController.deleteScheduledJob(jobIds, true);

        Mockito.verify(scheduler, Mockito.atLeastOnce()).deleteScheduledJobs(Mockito.any(), anyBoolean());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testDeleteSchedulerJobWrongTask() throws MangleException {
        List<String> jobIds = new ArrayList<>();

        ResponseEntity<Map<String, DeleteSchedulerResponse>> response =
                schedulerController.deleteScheduledJob(jobIds, true);

        Mockito.verify(scheduler, times(0)).getScheduledJob(Mockito.any());
        Mockito.verify(scheduler, times(0)).deletScheduledJob(Mockito.any());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    /**
     * Test method for {@link SchedulerController#getAllScheduledJobs(SchedulerStatus)}
     */
    @Test
    public void getAllScheduledJobs() {
        Mockito.when(scheduler.getAllScheduledJobs(SchedulerStatus.CANCELLED))
                .thenReturn(schedulerControllerMockData.getListOfSchedulerSpec());
        ResponseEntity<List<SchedulerSpec>> response =
                schedulerController.getAllScheduledJobs(SchedulerStatus.CANCELLED);
        Mockito.verify(scheduler, Mockito.atLeastOnce()).getAllScheduledJobs(Mockito.any());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }
}
