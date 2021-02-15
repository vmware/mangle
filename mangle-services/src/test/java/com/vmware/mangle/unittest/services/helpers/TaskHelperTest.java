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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskFilter;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.deletionutils.TaskDeletionService;
import com.vmware.mangle.services.helpers.TaskHelper;
import com.vmware.mangle.services.mockdata.FaultsMockData;
import com.vmware.mangle.services.mockdata.ResiliencyScoreMockData;
import com.vmware.mangle.services.mockdata.TaskFilterMockData;
import com.vmware.mangle.services.mockdata.TasksMockData;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreService;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author dbhat
 */
public class TaskHelperTest {

    @Mock
    private ResiliencyScoreService resiliencyScoreService;
    @Mock
    private TaskService taskService;
    @Mock
    private TaskDeletionService taskDeletionService;
    @InjectMocks
    private TaskHelper taskHelper;

    private TasksMockData<TaskSpec> tasksMockData;

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public void initData() {
        this.tasksMockData = new TasksMockData<>(new CommandExecutionFaultSpec(), new CommandExecutionFaultSpec());
    }

    @Test
    public void validateResiliencyScoreTask() throws MangleException {
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        resiliencyScoreTask.setTaskType(TaskType.RESILIENCY_SCORE);
        when(taskService.getTaskById(anyString()))
                .thenThrow(new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.TASK_ID, null));
        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(resiliencyScoreTask);

        TaskType taskType = taskHelper.getTaskType(ResiliencyScoreMockData.getRandomUUID());
        verify(resiliencyScoreService, times(1)).getTaskById(anyString());
        verify(taskService, times(1)).getTaskById(anyString());
        Assert.assertEquals(taskType, TaskType.RESILIENCY_SCORE);
    }

    @Test
    public void validateFaultTask() throws MangleException {
        FaultsMockData faultsMockData = new FaultsMockData();
        Task<TaskSpec> task = new Task<>();

        task.setTaskType(TaskType.INJECTION);
        when(taskService.getTaskById(anyString())).thenReturn(task);

        TaskType taskType = taskHelper.getTaskType(ResiliencyScoreMockData.getRandomUUID());
        verify(resiliencyScoreService, times(0)).getTaskById(anyString());
        verify(taskService, times(1)).getTaskById(anyString());
        Assert.assertEquals(taskType, TaskType.INJECTION);
    }


    @Test(description = "Validate Filter task based on Index when NO fault tasks are in the DB")
    public void validateTaskFilterDataWhenNoFaultTasksInDB() {
        List<ResiliencyScoreTask> resiliencyScoreTasks = new ArrayList<>();
        resiliencyScoreTasks.add(ResiliencyScoreMockData.getResiliencyScoreTask1());
        List<Task<TaskSpec>> faultTasks = new ArrayList<>();
        TaskFilter taskFilter = TaskFilterMockData.getTaskFilter();

        Map<String, Object> pageObject = taskHelper.getTaskBasedOnIndex(faultTasks, resiliencyScoreTasks, taskFilter);

        Assert.assertEquals(pageObject.get(Constants.TASK_SIZE), resiliencyScoreTasks.size());
        Assert.assertNotNull(pageObject.get(Constants.TASK_LIST));
    }

    @Test(description = "Validate Filter task based on Index when NO ResiliencyScore tasks are in the DB")
    public void validateTaskFilterDataWhenNoResiliencyScoreTasksInDB() {
        List<ResiliencyScoreTask> resiliencyScoreTasks = new ArrayList<>();
        List<Task<TaskSpec>> faultTasks = tasksMockData.getDummy1Tasks();
        TaskFilter taskFilter = TaskFilterMockData.getTaskFilter();

        Map<String, Object> pageObject = taskHelper.getTaskBasedOnIndex(faultTasks, resiliencyScoreTasks, taskFilter);

        Assert.assertEquals(pageObject.get(Constants.TASK_SIZE), faultTasks.size());
        Assert.assertNotNull(pageObject.get(Constants.TASK_LIST));
    }

    @Test(description = "Validate Task Filter when only Fault Tasks are in DB")
    public void validateTaskFilterDataForFaultTasksOnly() {
        List<ResiliencyScoreTask> resiliencyScoreTasks = new ArrayList<>();
        List<Task<TaskSpec>> faultTasks = tasksMockData.getDummy1Tasks();

        TaskFilter taskFilter = TaskFilterMockData.getTaskFilterWithData();
        Map<String, Object> pageObject = taskHelper.getTaskBasedOnIndex(faultTasks, resiliencyScoreTasks, taskFilter);

        Assert.assertEquals(pageObject.get(Constants.TASK_SIZE), faultTasks.size());
        Assert.assertNotNull(pageObject.get(Constants.TASK_LIST));
    }

    @Test(description = "Validate Task Filter when Resiliency and Fault Tasks are in DB")
    public void validateTaskFilterDataForFaultAndResiliencyTasks() {
        List<ResiliencyScoreTask> resiliencyScoreTasks = new ArrayList<>();
        resiliencyScoreTasks.add(ResiliencyScoreMockData.getResiliencyScoreTask1());
        List<Task<TaskSpec>> faultTasks = tasksMockData.getDummy1Tasks();

        TaskFilter taskFilter = TaskFilterMockData.getTaskFilterWithData();

        Map<String, Object> pageObject = taskHelper.getTaskBasedOnIndex(faultTasks, resiliencyScoreTasks, taskFilter);

        Assert.assertEquals(pageObject.get(Constants.TASK_SIZE), faultTasks.size() + resiliencyScoreTasks.size());
        Assert.assertNotNull(pageObject.get(Constants.TASK_LIST));
    }

    @Test(description = "Validate Task Filter when Resiliency and Fault Tasks are in DB")
    public void validateTaskFilterDataForResiliencyTasksOnly() {
        List<ResiliencyScoreTask> resiliencyScoreTasks = new ArrayList<>();
        resiliencyScoreTasks.add(ResiliencyScoreMockData.getResiliencyScoreTask1());
        List<Task<TaskSpec>> faultTasks = new ArrayList<>();

        TaskFilter taskFilter = TaskFilterMockData.getTaskFilterWithData();

        Map<String, Object> pageObject = taskHelper.getTaskBasedOnIndex(faultTasks, resiliencyScoreTasks, taskFilter);

        Assert.assertEquals(pageObject.get(Constants.TASK_SIZE), resiliencyScoreTasks.size());
        Assert.assertNotNull(pageObject.get(Constants.TASK_LIST));
    }

    @Test(description = "Validate partial Task Filter when only Fault Tasks are in DB")
    public void validateTaskFilterDataWithPartialFieldsForFaultTasksOnly() {
        List<ResiliencyScoreTask> resiliencyScoreTasks = new ArrayList<>();
        List<Task<TaskSpec>> faultTasks = tasksMockData.getDummy1Tasks();

        TaskFilter taskFilter = TaskFilterMockData.getTaskFilterWithPartialFields();

        Map<String, Object> pageObject = taskHelper.getTaskBasedOnIndex(faultTasks, resiliencyScoreTasks, taskFilter);

        Assert.assertEquals(pageObject.get(Constants.TASK_SIZE), faultTasks.size());
        Assert.assertNotNull(pageObject.get(Constants.TASK_LIST));
    }

    @Test(description = "Validate partial Task Filter when only Resiliency Tasks are in DB")
    public void validateTaskFilterDataWithPartialFieldsForResiliencyTasksOnly() {
        List<ResiliencyScoreTask> resiliencyScoreTasks = new ArrayList<>();
        resiliencyScoreTasks.add(ResiliencyScoreMockData.getResiliencyScoreTask1());
        List<Task<TaskSpec>> faultTasks = new ArrayList<>();

        TaskFilter taskFilter = TaskFilterMockData.getTaskFilterWithPartialFields();

        Map<String, Object> pageObject = taskHelper.getTaskBasedOnIndex(faultTasks, resiliencyScoreTasks, taskFilter);

        Assert.assertEquals(pageObject.get(Constants.TASK_SIZE), resiliencyScoreTasks.size());
        Assert.assertNotNull(pageObject.get(Constants.TASK_LIST));
    }

    @Test(description = "Validate partial Task Filter when only Fault Tasks are in DB")
    public void validateTaskFilterDataWithPartialFieldsForFaultAndResiliencyTasks() {
        List<ResiliencyScoreTask> resiliencyScoreTasks = new ArrayList<>();
        resiliencyScoreTasks.add(ResiliencyScoreMockData.getResiliencyScoreTask1());
        List<Task<TaskSpec>> faultTasks = tasksMockData.getDummy1Tasks();

        TaskFilter taskFilter = TaskFilterMockData.getTaskFilterWithPartialFields();

        Map<String, Object> pageObject = taskHelper.getTaskBasedOnIndex(faultTasks, resiliencyScoreTasks, taskFilter);

        Assert.assertEquals(pageObject.get(Constants.TASK_SIZE), faultTasks.size() + resiliencyScoreTasks.size());
        Assert.assertNotNull(pageObject.get(Constants.TASK_LIST));
    }

    @Test(description = "Validate partial Task Filter when only Fault Tasks are in DB")
    public void validateTaskFilterDataWithPartialMatchForFaultAndResiliencyTasks() {
        List<ResiliencyScoreTask> resiliencyScoreTasks = new ArrayList<>();
        List<ResiliencyScoreTask> dummy1ResiliencyScoreTasks = new ArrayList<>();
        List<ResiliencyScoreTask> dummy2ResiliencyScoreTasks = new ArrayList<>();
        dummy1ResiliencyScoreTasks.add(ResiliencyScoreMockData.getResiliencyScoreTask1());
        dummy2ResiliencyScoreTasks.add(ResiliencyScoreMockData.getResiliencyScoreTask2());
        resiliencyScoreTasks.addAll(dummy2ResiliencyScoreTasks);
        resiliencyScoreTasks.addAll(dummy2ResiliencyScoreTasks);
        List<Task<TaskSpec>> faultTasks = new ArrayList<>();
        List<Task<TaskSpec>> dummy1FaultTasks = tasksMockData.getDummy1Tasks();
        List<Task<TaskSpec>> dummy2FaultTasks = tasksMockData.getDummy2Tasks();
        faultTasks.addAll(dummy1FaultTasks);
        faultTasks.addAll(dummy2FaultTasks);

        TaskFilter taskFilter = TaskFilterMockData.getTaskFilter1WithPartialFields();

        Map<String, Object> pageObject = taskHelper.getTaskBasedOnIndex(faultTasks, resiliencyScoreTasks, taskFilter);

        Assert.assertEquals(pageObject.get(Constants.TASK_SIZE), dummy2FaultTasks.size());
        Assert.assertNotNull(pageObject.get(Constants.TASK_LIST));
    }

    @Test(description = "Validate Filter task based on Index when NO tasks are in the DB")
    public void validateTaskFilterDataWhenNoTasksInDB() {
        List<ResiliencyScoreTask> resiliencyScoreTasks = new ArrayList<>();
        List<Task<TaskSpec>> faultTasks = new ArrayList<>();
        TaskFilter taskFilter = TaskFilterMockData.getTaskFilter();

        Map<String, Object> pageObject = taskHelper.getTaskBasedOnIndex(faultTasks, resiliencyScoreTasks, taskFilter);

        Assert.assertEquals(pageObject.get(Constants.TASK_SIZE), 0);
        Assert.assertNotNull(pageObject.get(Constants.TASK_LIST));
    }

    @Test(description = "Validate Filter task based on Index when both ResiliencyScore tasks and Fault tasks are in the DB")
    public void validateTaskFilterDataWithAllTypesOfTasksInDB() {
        List<ResiliencyScoreTask> resiliencyScoreTasks = new ArrayList<>();
        resiliencyScoreTasks.add(ResiliencyScoreMockData.getResiliencyScoreTask1());
        List<Task<TaskSpec>> faultTasks = tasksMockData.getDummy1Tasks();
        TaskFilter taskFilter = TaskFilterMockData.getTaskFilter();
        int numberOfTasks = resiliencyScoreTasks.size() + faultTasks.size();

        Map<String, Object> pageObject = taskHelper.getTaskBasedOnIndex(faultTasks, resiliencyScoreTasks, taskFilter);

        Assert.assertEquals(pageObject.get(Constants.TASK_SIZE), numberOfTasks);
        Assert.assertNotNull(pageObject.get(Constants.TASK_LIST));
    }

    @Test(description = "Validating deleting of tasks having only resiliency score task IDs")
    public void deleteOnlyResiliencyScoreTasks() throws MangleException {
        List<ResiliencyScoreTask> tasks = Arrays.asList(ResiliencyScoreMockData.getResiliencyScoreTask1());
        List<String> taskIds = Arrays.asList(tasks.get(0).getId());
        DeleteOperationResponse deleteResponse = new DeleteOperationResponse();

        when(resiliencyScoreService.deleteTasksByIds(taskIds)).thenReturn(deleteResponse);
        when(taskService.getTaskById(anyString())).thenThrow(MangleRuntimeException.class);
        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(tasks.get(0));
        deleteResponse = taskHelper.deleteTasks(taskIds);

        Assert.assertTrue(deleteResponse.getAssociations().isEmpty());
        verify(resiliencyScoreService, times(1)).getTaskById(anyString());
        verify(taskService, times(1)).getTaskById(anyString());
        verify(resiliencyScoreService, times(1)).deleteTasksByIds(any());
        verify(taskDeletionService, times(0)).deleteTasksByIds(any());
    }

    @Test(description = "Validating deleting of tasks having only Fault tasks")
    public void deleteOnlyFaultTasks() throws MangleException {
        List<Task<TaskSpec>> tasks = tasksMockData.getDummy1Tasks();
        List<String> taskIds = Arrays.asList(tasks.get(0).getId());
        DeleteOperationResponse deleteResponse = new DeleteOperationResponse();

        when(taskDeletionService.deleteTasksByIds(taskIds)).thenReturn(deleteResponse);
        when(taskService.getTaskById(anyString())).thenReturn(tasks.get(0));
        deleteResponse = taskHelper.deleteTasks(taskIds);

        Assert.assertTrue(deleteResponse.getAssociations().isEmpty());
        verify(resiliencyScoreService, times(0)).getTaskById(anyString());
        verify(taskService, times(1)).getTaskById(anyString());
        verify(resiliencyScoreService, times(0)).deleteTasksByIds(any());
        verify(taskDeletionService, times(1)).deleteTasksByIds(any());
    }

    @Test(description = "Validating deleting of scheduled fault tasks")
    public void deleteOfScheduledFaultTask() {
        try {
            taskHelper.deleteTasks(new ArrayList<>());
            Assert.fail("Tasks list is empty and hence, expected exceptions to be received.");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
        }
    }

}
