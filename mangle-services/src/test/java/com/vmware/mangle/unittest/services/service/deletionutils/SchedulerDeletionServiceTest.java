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

package com.vmware.mangle.unittest.services.service.deletionutils;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.deletionutils.SchedulerDeletionService;
import com.vmware.mangle.services.deletionutils.TaskDeletionService;
import com.vmware.mangle.services.mockdata.SchedulerControllerMockData;
import com.vmware.mangle.services.repository.SchedulerRepository;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author chetanc
 */
public class SchedulerDeletionServiceTest {

    @Mock
    private SchedulerRepository schedulerRepository;
    @Mock
    private TaskDeletionService taskDeletionService;

    private SchedulerDeletionService schedulerDeletionService;
    private SchedulerControllerMockData mockData = new SchedulerControllerMockData();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        schedulerDeletionService = new SchedulerDeletionService(schedulerRepository, taskDeletionService);
    }

    @Test
    public void testDeleteSchedulerDetailsByJobIds() throws MangleException {
        List<String> tasks = new ArrayList<>();
        Set<SchedulerSpec> schedulerSpecSet = new HashSet<>();
        schedulerSpecSet.add(mockData.getMangleSchedulerSpec());
        tasks.add(mockData.getMangleSchedulerSpec().getId());

        doNothing().when(schedulerRepository).deleteByIdIn(any());
        when(schedulerRepository.findByIds(any())).thenReturn(schedulerSpecSet);
        schedulerDeletionService.deleteSchedulerDetailsByJobIds(tasks, false);
        verify(schedulerRepository, times(1)).deleteByIdIn(any());
    }

    @Test
    public void testDeleteSchedulerDetailsByJobIdsEmptyTasks() throws MangleException {
        List<String> tasks = new ArrayList<>();
        doNothing().when(schedulerRepository).deleteByIdIn(any());

        schedulerDeletionService.deleteSchedulerDetailsByJobIds(tasks, false);
        verify(schedulerRepository, times(0)).deleteByIdIn(any());
    }

    @Test
    public void testDeleteSchedulerDetailsByJobId() throws MangleException {
        List<String> tasks = new ArrayList<>();
        Set<SchedulerSpec> schedulerSpecSet = new HashSet<>();
        schedulerSpecSet.add(mockData.getMangleSchedulerSpec());
        tasks.add(mockData.getMangleSchedulerSpec().getId());
        List<String> tasksBeforeTrigger = new ArrayList<>(tasks);
        DeleteOperationResponse deleteOperationResponse = new DeleteOperationResponse();

        doNothing().when(schedulerRepository).deleteByIdIn(tasksBeforeTrigger);
        when(taskDeletionService.deleteTasksByIds(tasksBeforeTrigger)).thenReturn(deleteOperationResponse);
        when(schedulerRepository.findByIds(any())).thenReturn(schedulerSpecSet);

        schedulerDeletionService.deleteSchedulerDetailsByJobIds(tasks, true);

        verify(schedulerRepository, times(1)).deleteByIdIn(tasksBeforeTrigger);
        verify(taskDeletionService, times(1)).deleteTasksByIds(tasksBeforeTrigger);
    }

    @Test
    public void testDeleteSchedulerDetailsByJobIdNoTasks() throws MangleException {
        List<String> tasks = new ArrayList<>();
        Set<SchedulerSpec> schedulerSpecSet = new HashSet<>();
        schedulerSpecSet.add(mockData.getMangleSchedulerSpec());
        tasks.add(mockData.getMangleSchedulerSpec().getId());
        List<String> tasksBeforeTrigger = new ArrayList<>(tasks);
        DeleteOperationResponse deleteOperationResponse = new DeleteOperationResponse();

        doNothing().when(schedulerRepository).deleteByIdIn(tasksBeforeTrigger);
        when(taskDeletionService.deleteTasksByIds(tasksBeforeTrigger)).thenReturn(deleteOperationResponse);
        when(schedulerRepository.findByIds(any())).thenReturn(schedulerSpecSet);

        schedulerDeletionService.deleteSchedulerDetailsByJobIds(tasks, false);

        verify(schedulerRepository, times(1)).deleteByIdIn(tasksBeforeTrigger);
        verify(taskDeletionService, times(0)).deleteTasksByIds(tasksBeforeTrigger);
    }
}
