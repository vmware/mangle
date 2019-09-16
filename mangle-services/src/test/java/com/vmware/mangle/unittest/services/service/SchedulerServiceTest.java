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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.extern.log4j.Log4j2;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.mockdata.SchedulerControllerMockData;
import com.vmware.mangle.services.repository.SchedulerRepository;

/**
 *
 *
 * @author chetanc
 */
@Log4j2
public class SchedulerServiceTest {

    @Mock
    private SchedulerRepository schedulerRepository;

    @InjectMocks
    private SchedulerService schedulerService;

    private SchedulerControllerMockData mockData = new SchedulerControllerMockData();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetActiveSchedulesForIds() {
        log.debug("Exeucting testGetActiveSchedulesForIds on method SchedulerService#getActiveSchedulesForIds");
        String taskId = UUID.randomUUID().toString();
        SchedulerSpec spec = mockData.getMangleSchedulerSpecScheduled();
        Set<SchedulerSpec> specs = new HashSet<>(Arrays.asList(spec));
        List<String> tasks = new ArrayList<>(Arrays.asList(taskId));
        when(schedulerRepository.findByIds(tasks)).thenReturn(specs);

        List<SchedulerSpec> persistedSchedules = schedulerService.getActiveSchedulesForIds(tasks);

        Assert.assertEquals(1, persistedSchedules.size());
        Assert.assertEquals(spec.getId(), persistedSchedules.get(0).getId());
        Assert.assertEquals(spec.getStatus(), persistedSchedules.get(0).getStatus());
        verify(schedulerRepository, times(1)).findByIds(any());
    }

    @Test
    public void testGetActiveSchedulesForIdsPausedJob() {
        log.debug(
                "Exeucting testGetActiveSchedulesForIdsPausedJob on method SchedulerService#getActiveSchedulesForIds");
        String taskId = UUID.randomUUID().toString();
        SchedulerSpec spec = mockData.getMangleSchedulerSpecScheduled();
        spec.setStatus(SchedulerStatus.PAUSED);
        Set<SchedulerSpec> specs = new HashSet<>(Arrays.asList(spec));
        List<String> tasks = new ArrayList<>(Arrays.asList(taskId));
        when(schedulerRepository.findByIds(tasks)).thenReturn(specs);

        List<SchedulerSpec> persistedSchedules = schedulerService.getActiveSchedulesForIds(tasks);

        Assert.assertEquals(1, persistedSchedules.size());
        Assert.assertEquals(spec.getId(), persistedSchedules.get(0).getId());
        Assert.assertEquals(spec.getStatus(), persistedSchedules.get(0).getStatus());
        verify(schedulerRepository, times(1)).findByIds(any());
    }

    @Test
    public void testGetActiveSchedulesForIdsInitializingJob() {
        log.debug(
                "Exeucting testGetActiveSchedulesForIdsInitializingJob on method SchedulerService#getActiveSchedulesForIds");
        String taskId = UUID.randomUUID().toString();
        SchedulerSpec spec = mockData.getMangleSchedulerSpecScheduled();
        spec.setStatus(SchedulerStatus.INITIALIZING);
        Set<SchedulerSpec> specs = new HashSet<>(Arrays.asList(spec));
        List<String> tasks = new ArrayList<>(Arrays.asList(taskId));
        when(schedulerRepository.findByIds(tasks)).thenReturn(specs);

        List<SchedulerSpec> persistedSchedules = schedulerService.getActiveSchedulesForIds(tasks);

        Assert.assertEquals(1, persistedSchedules.size());
        Assert.assertEquals(spec.getId(), persistedSchedules.get(0).getId());
        Assert.assertEquals(spec.getStatus(), persistedSchedules.get(0).getStatus());
        verify(schedulerRepository, times(1)).findByIds(any());
    }

    @Test
    public void testGetActiveSchedulesForIdsCancelledJob() {
        log.debug(
                "Exeucting testGetActiveSchedulesForIdsCancelledJob on method SchedulerService#getActiveSchedulesForIds");
        String taskId = UUID.randomUUID().toString();
        SchedulerSpec spec = mockData.getMangleSchedulerSpecScheduled();
        spec.setStatus(SchedulerStatus.CANCELLED);
        Set<SchedulerSpec> specs = new HashSet<>(Arrays.asList(spec));
        List<String> tasks = new ArrayList<>(Arrays.asList(taskId));
        when(schedulerRepository.findByIds(tasks)).thenReturn(specs);

        List<SchedulerSpec> persistedSchedules = schedulerService.getActiveSchedulesForIds(tasks);

        Assert.assertEquals(0, persistedSchedules.size());
        verify(schedulerRepository, times(1)).findByIds(any());
    }

    @Test
    public void testGetActiveScheduleJobsPausedJob() {
        log.debug("Exeucting testGetActiveScheduleJobsPausedJob on method SchedulerService#getActiveScheduleJobs");
        SchedulerSpec spec = mockData.getMangleSchedulerSpecScheduled();
        spec.setStatus(SchedulerStatus.PAUSED);
        List<SchedulerSpec> specs = new ArrayList<>(Arrays.asList(spec));
        when(schedulerRepository.findAll()).thenReturn(specs);

        List<String> persistedSchedules = schedulerService.getActiveScheduleJobs();

        Assert.assertEquals(1, persistedSchedules.size());
        verify(schedulerRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllScheduledJobByStatusNullSchedule() {
        log.debug(
                "Exeucting testGetAllScheduledJobByStatusNullSchedule on method SchedulerService#getAllScheduledJobByStatus");
        List<SchedulerSpec> schedulerSpecs = schedulerService.getAllScheduledJobByStatus(null);
        Assert.assertEquals(schedulerSpecs.size(), 0);
    }

    @Test
    public void testGetScheduledJobByIdandStatusEmptySchedule() {
        log.debug(
                "Exeucting testGetScheduledJobByIdandStatusEmptySchedule on method SchedulerService#getScheduledJobByIdandStatus");
        String jobId = UUID.randomUUID().toString();
        Optional<SchedulerSpec> optional = Optional.empty();
        when(schedulerRepository.findByIdAndStatus(jobId, SchedulerStatus.SCHEDULED)).thenReturn(optional);

        SchedulerSpec schedulerSpec = schedulerService.getScheduledJobByIdandStatus(jobId, SchedulerStatus.SCHEDULED);
        Assert.assertNull(schedulerSpec);
    }

    @Test
    public void testGetScheduledJobByIdandStatus() {
        log.debug("Exeucting testGetScheduledJobByIdandStatus on method SchedulerService#getScheduledJobByIdandStatus");
        String jobId = UUID.randomUUID().toString();
        Optional<SchedulerSpec> optional = Optional.of(new SchedulerSpec());
        when(schedulerRepository.findByIdAndStatus(jobId, SchedulerStatus.SCHEDULED)).thenReturn(optional);

        SchedulerSpec schedulerSpec = schedulerService.getScheduledJobByIdandStatus(jobId, SchedulerStatus.SCHEDULED);
        Assert.assertNotNull(schedulerSpec);
        verify(schedulerRepository, times(1)).findByIdAndStatus(anyString(), any());
    }

    @Test
    public void testGetScheduledJobByIdandStatusNullJobId() {
        log.debug(
                "Exeucting testGetScheduledJobByIdandStatusNullJobId on method SchedulerService#getScheduledJobByIdandStatus");
        SchedulerSpec schedulerSpec = schedulerService.getScheduledJobByIdandStatus(null, SchedulerStatus.SCHEDULED);
        Assert.assertNull(schedulerSpec);
    }

    @Test
    public void testGetSchedulerDetailsByIdEmptySchedule() {
        log.debug(
                "Exeucting testGetSchedulerDetailsByIdEmptySchedule on method SchedulerService#getSchedulerDetailsById");
        String jobId = UUID.randomUUID().toString();
        Optional<SchedulerSpec> optional = Optional.empty();
        when(schedulerRepository.findById(jobId)).thenReturn(optional);

        SchedulerSpec schedulerSpec = schedulerService.getSchedulerDetailsById(jobId);
        Assert.assertNull(schedulerSpec);
        verify(schedulerRepository, times(1)).findById(anyString());
    }

    @Test
    public void testGetSchedulerDetailsById() {
        log.debug("Exeucting testGetSchedulerDetailsById on method SchedulerService#getScheduledJobByIdandStatus");
        String jobId = UUID.randomUUID().toString();
        Optional<SchedulerSpec> optional = Optional.of(new SchedulerSpec());
        when(schedulerRepository.findById(jobId)).thenReturn(optional);

        SchedulerSpec schedulerSpec = schedulerService.getSchedulerDetailsById(jobId);
        Assert.assertNotNull(schedulerSpec);
        verify(schedulerRepository, times(1)).findById(anyString());
    }

    @Test
    public void testGetSchedulerDetailsByIdNullJobId() {
        log.info(
                "Exeucting testGetSchedulerDetailsByIdNullJobId on method SchedulerService#getScheduledJobByIdandStatus");
        SchedulerSpec schedulerSpec = schedulerService.getSchedulerDetailsById(null);
        Assert.assertNull(schedulerSpec);
    }

    @Test
    public void testGetAllSchedulerDetails() {
        log.info("Exeucting testGetAllSchedulerDetails on method SchedulerService#getAllSchedulerDetails");
        SchedulerSpec schedulerSpec = new SchedulerSpec();
        when(schedulerService.getAllSchedulerDetails()).thenReturn(Collections.singletonList(schedulerSpec));
        List<SchedulerSpec> schedulerSpecs = schedulerService.getAllSchedulerDetails();
        Assert.assertNotNull(schedulerSpecs);
        Assert.assertEquals(schedulerSpecs.size(), 1);
        Assert.assertEquals(schedulerSpecs.get(0), schedulerSpec);
    }

    @Test
    public void testAddOrUpdateSchedulerDetailsNullSpec() {
        log.info(
                "Exeucting testAddOrUpdateSchedulerDetailsNullSpec on method SchedulerService#addOrUpdateSchedulerDetails");
        SchedulerSpec schedulerSpec = new SchedulerSpec();
        schedulerSpec.setId(null);
        when(schedulerRepository.save(any())).thenReturn(schedulerSpec);

        SchedulerSpec persistedSpec = schedulerService.addOrUpdateSchedulerDetails(schedulerSpec);
        Assert.assertNull(persistedSpec);
        verify(schedulerRepository, times(1)).save(any());
        verify(schedulerRepository, times(0)).findById(anyString());
    }

    @Test
    public void testAddOrUpdateSchedulerDetails() {
        log.info("Executing testAddOrUpdateSchedulerDetails on method SchedulerService#addOrUpdateSchedulerDetails");
        SchedulerSpec schedulerSpec = new SchedulerSpec();
        Optional<SchedulerSpec> optional = Optional.of(schedulerSpec);
        when(schedulerRepository.save(any())).thenReturn(schedulerSpec);
        when(schedulerRepository.findById(any())).thenReturn(optional);

        SchedulerSpec persistedSpec = schedulerService.addOrUpdateSchedulerDetails(schedulerSpec);
        Assert.assertEquals(persistedSpec, schedulerSpec);
        verify(schedulerRepository, times(1)).save(any());
        verify(schedulerRepository, times(1)).findById(anyString());
    }

    @Test
    public void testUpdateSchedulerStatus() {
        log.info("Exeucting testUpdateSchedulerStatus on method SchedulerService#updateSchedulerStatus");
        SchedulerSpec schedulerSpec = new SchedulerSpec();
        Optional<SchedulerSpec> optional = Optional.of(schedulerSpec);
        when(schedulerRepository.save(any())).thenReturn(schedulerSpec);
        when(schedulerRepository.findById(any())).thenReturn(optional);

        SchedulerSpec persistedSpec =
                schedulerService.updateSchedulerStatus(schedulerSpec.getId(), SchedulerStatus.SCHEDULED);

        Assert.assertNotNull(persistedSpec);
        Assert.assertEquals(persistedSpec.getStatus(), SchedulerStatus.SCHEDULED);
    }

}
