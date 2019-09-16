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

import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;

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

import com.vmware.mangle.cassandra.model.scheduler.SchedulerRequestStatus;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.model.enums.SchedulerStatus;
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

    private SchedulerControllerMockData schedulerControllerMockData = new SchedulerControllerMockData();


    /**
     * Before Method to initialize mocks
     */
    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for {@link SchedulerController#cancelScheduledJobs(List)}
     *
     * @throws MangleException
     */
    @Test
    public void cancelScheduledJobs() throws MangleException {
        List<String> jobIds = schedulerControllerMockData.getJobIds();
        when(scheduler.cancelScheduledJobs(jobIds)).thenReturn(new HashSet<>(jobIds));
        ResponseEntity<SchedulerRequestStatus> response = schedulerController.cancelScheduledJobs(jobIds);
        Mockito.verify(scheduler, Mockito.atLeastOnce()).cancelScheduledJobs(Mockito.any());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    /**
     * Test method for {@link SchedulerController#pauseScheduledJobs(List)}
     *
     * @throws MangleException
     */
    @Test
    public void pauseScheduledJobs() throws MangleException {
        List<String> jobIds = schedulerControllerMockData.getJobIds();
        when(scheduler.pauseScheduledJobs(jobIds)).thenReturn(new HashSet<>(jobIds));
        ResponseEntity<SchedulerRequestStatus> response = schedulerController.pauseScheduledJobs(jobIds);
        Mockito.verify(scheduler, Mockito.atLeastOnce()).pauseScheduledJobs(Mockito.any());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    /**
     * Test method for {@link SchedulerController#resumeScheduledJobs(List)}
     *
     * @throws MangleException
     */
    @Test
    public void resumeScheduledJobs() throws MangleException {
        List<String> jobIds = schedulerControllerMockData.getJobIds();
        when(scheduler.resumeJobs(jobIds)).thenReturn(new HashSet<>(jobIds));
        ResponseEntity<SchedulerRequestStatus> response = schedulerController.resumeScheduledJobs(jobIds);
        Mockito.verify(scheduler, Mockito.atLeastOnce()).resumeJobs(Mockito.any());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    /**
     * Test method for {@link SchedulerController#resumeScheduledJobs(List)}
     */
    @Test
    public void resumeScheduledJobsFailureEmptyStatusMap() throws MangleException {
        List<String> jobIds = schedulerControllerMockData.getJobIds();
        when(scheduler.resumeJobs(jobIds)).thenReturn(new HashSet<>(jobIds));
        ResponseEntity<SchedulerRequestStatus> response = schedulerController.resumeScheduledJobs(jobIds);
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
        List<String> jobIds = schedulerControllerMockData.getJobIds();
        when(scheduler.cancelAllScheduledJobs()).thenReturn(new HashSet<>(jobIds));
        ResponseEntity<SchedulerRequestStatus> response = schedulerController.cancelAllScheduledJobs();
        Mockito.verify(scheduler, Mockito.times(1)).cancelAllScheduledJobs();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    /**
     * Test method for {@link SchedulerController#getAllScheduledJobs(SchedulerStatus)}
     */
    @Test
    public void getAllScheduledJobsWithFilter() {
        Mockito.when(scheduler.getAllScheduledJobs(SchedulerStatus.CANCELLED))
                .thenReturn(schedulerControllerMockData.getListOfSchedulerSpec());
        ResponseEntity<List<SchedulerSpec>> response =
                schedulerController.getAllScheduledJobs(SchedulerStatus.CANCELLED);
        Mockito.verify(scheduler, Mockito.atLeastOnce()).getAllScheduledJobs(Mockito.any());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    /**
     * Test method for {@link SchedulerController#getAllScheduledJobs(SchedulerStatus)}
     */
    @Test
    public void getAllScheduledJobsWithNoFilter() {
        Mockito.when(scheduler.getAllScheduledJobs()).thenReturn(schedulerControllerMockData.getListOfSchedulerSpec());
        ResponseEntity<List<SchedulerSpec>> response = schedulerController.getAllScheduledJobs(null);
        Mockito.verify(scheduler, Mockito.atLeastOnce()).getAllScheduledJobs();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

}
