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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.deletionutils.TaskDeletionService;
import com.vmware.mangle.services.events.web.CustomEventPublisher;
import com.vmware.mangle.services.mockdata.FaultsMockData;
import com.vmware.mangle.services.mockdata.SchedulerControllerMockData;
import com.vmware.mangle.services.mockdata.TasksMockData;
import com.vmware.mangle.services.repository.TaskRepository;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author chetanc
 */
@Log4j2
public class TaskDeletionServiceTest {

    private SchedulerControllerMockData schedulerMockData = new SchedulerControllerMockData();
    private FaultsMockData faultsMockData = new FaultsMockData();
    private TasksMockData tasksMockData;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private CustomEventPublisher eventPublisher;

    private TaskDeletionService taskDeletionService;

    @BeforeClass
    public void initTaskMockData() {
        tasksMockData = new TasksMockData(faultsMockData.getVMNicFaultSpec());
    }

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        taskDeletionService = new TaskDeletionService(taskRepository, schedulerService, eventPublisher);
    }

    @Test
    public void testDeleteTaskByIds() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        List<String> tasks = new ArrayList<>(Arrays.asList(taskId));
        SchedulerSpec schedulerSpec = schedulerMockData.getMangleSchedulerSpecScheduled();
        List<SchedulerSpec> schedulerSpecs = new ArrayList<>(Arrays.asList(schedulerSpec));

        when(schedulerService.getActiveSchedulesForIds(tasks)).thenReturn(schedulerSpecs);

        DeleteOperationResponse response = taskDeletionService.deleteTasksByIds(tasks);

        Assert.assertEquals(response.getResponseMessage(), ErrorConstants.TASK_DELETION_PRECHECK_FAIL);
        Assert.assertEquals(response.getAssociations().size(), 1);
        verify(schedulerService, times(1)).getActiveSchedulesForIds(any());
        verify(taskRepository, times(0)).deleteByIdIn(any());
    }

    @Test
    public void testDeleteTaskByIdsNoActiveSchedule() throws MangleException {
        List<String> tasks = new ArrayList<>();
        List<SchedulerSpec> schedulerSpecs = new ArrayList<>();
        Task<TaskSpec> task = tasksMockData.getDummy1Task();
        tasks.add(task.getId());

        when(schedulerService.getActiveSchedulesForIds(tasks)).thenReturn(schedulerSpecs);
        when(taskRepository.findByIds(tasks)).thenReturn(Collections.singletonList(task));
        doNothing().when(taskRepository).deleteByIdIn(any());

        DeleteOperationResponse response = taskDeletionService.deleteTasksByIds(tasks);

        Assert.assertEquals(response.getResponseMessage(), null);
        Assert.assertEquals(response.getAssociations().size(), 0);
        verify(schedulerService, times(1)).getActiveSchedulesForIds(any());
        verify(taskRepository, times(1)).deleteByIdIn(any());
    }

    @Test(expectedExceptions = { MangleException.class })
    public void testDeleteTaskByIdWithNull() throws MangleException {
        log.info("Executing test: deleteTaskByIdWithNull on TaskService#deleteTaskById");
        try {
            taskDeletionService.deleteTaskById(null);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            throw e;
        }
    }

    @Test(expectedExceptions = { MangleException.class })
    public void testDeleteTaskByIdWithTaskInProgress() throws MangleException {
        log.info("Executing test: deleteTaskByIdWithTaskInProgress on TaskService#deleteTaskById");
        Task<TaskSpec> task = tasksMockData.getDummy1Task();
        Optional<Task<TaskSpec>> taskOp = Optional.of(task);
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        Mockito.when(taskRepository.save(Mockito.any())).thenReturn(task);
        when(taskRepository.findById(task.getId())).thenReturn(taskOp);
        try {
            taskDeletionService.deleteTaskById(task.getId());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.INPROGRESS_TASK_DELETION_FAILURE);
            throw e;
        }
    }

    @Test
    public void testDeleteTaskById() throws MangleException {
        log.info("Executing test: deleteTaskByIdWithTaskCompleted on TaskService#deleteTaskById");
        Task<TaskSpec> task = tasksMockData.getDummy1Task();
        Optional<Task<TaskSpec>> taskOp = Optional.of(task);
        task.setTaskStatus(TaskStatus.COMPLETED);
        Mockito.when(taskRepository.save(Mockito.any())).thenReturn(task);
        when(taskRepository.findById(task.getId())).thenReturn(taskOp);
        boolean del = taskDeletionService.deleteTaskById(task.getId());
        Assert.assertTrue(del, "Testcase for TaskDeletionService.deleteTaskById(String) failed when the TaskStatus has completed");
    }

    @Test(enabled = false)
    public void deleteTasksByIdsTest() throws MangleException {
        log.info("Executing test: deleteTasksByIdsTest on TaskService#deleteTaskById");

        Mockito.doNothing().when(taskRepository).deleteByIdIn(Mockito.any());
        taskDeletionService.deleteTasksByIds(Arrays.asList("12345", "123456"));
        verify(taskRepository, Mockito.times(1)).deleteByIdIn(Mockito.any());
    }

    @Test(expectedExceptions = { MangleException.class })
    public void deleteTasksByIdsWithEmptyList() throws MangleException {
        log.info("Executing test: deleteTasksByIdsWithEmptyList on TaskService#deleteTasksByIds");
        try {
            taskDeletionService.deleteTasksByIds(Arrays.asList());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            throw e;
        }
    }

    @Test(expectedExceptions = { MangleException.class })
    public void deleteTasksByIdsWithNull() throws MangleException {
        log.info("Executing test: deleteTasksByIdsWithNull on TaskService#deleteTasksByIds");
        try {
            taskDeletionService.deleteTasksByIds(null);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            throw e;
        }
    }

    @Test(expectedExceptions = { MangleException.class })
    public void deleteTasksByIdsWithNonExistValue() throws MangleException {
        log.info("Executing test: deleteTasksByIdsWithNonExistValue on TaskService#deleteTasksByIds");
        try {
            taskDeletionService.deleteTasksByIds(Arrays.asList("417827481274821748247"));
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            throw e;
        }
    }
}
