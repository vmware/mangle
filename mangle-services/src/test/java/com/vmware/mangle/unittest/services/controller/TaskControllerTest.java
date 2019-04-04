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
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.enums.OperationStatus;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.controller.TaskController;
import com.vmware.mangle.services.deletionutils.TaskDeletionService;
import com.vmware.mangle.services.mockdata.TasksMockData;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit Test case for TaskController.
 *
 * @author kumargautam
 */
public class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private TaskDeletionService taskDeletionService;

    private TaskController taskController;
    private TasksMockData<TaskSpec> tasksMockData;

    @BeforeMethod
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        this.tasksMockData = new TasksMockData<>(new CommandExecutionFaultSpec());
        taskController = new TaskController(taskService, taskDeletionService);
    }


    @AfterClass
    public void tearDownAfterClass() {
        this.taskService = null;
        this.taskController = null;
        this.tasksMockData = null;
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.TaskController.TaskController#getAllTasks(java.lang.Boolean)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetAllScheduledTasks() throws MangleException {
        List<Task<TaskSpec>> tasks = tasksMockData.getDummyTasks();
        when(taskService.getTaskByIsScheduledTask(anyBoolean())).thenReturn(tasks);
        ResponseEntity<List<Task<TaskSpec>>> response = taskController.getAllTasks(true);
        verify(taskService, times(1)).getTaskByIsScheduledTask(anyBoolean());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody().size(), tasks.size());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.TaskController.TaskController#getAllTasks(java.lang.Boolean)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetAllTasks() throws MangleException {
        List<Task<TaskSpec>> tasks = tasksMockData.getDummyTasks();
        when(taskService.getAllTasks()).thenReturn(tasks);
        ResponseEntity<List<Task<TaskSpec>>> response = taskController.getAllTasks(null);
        verify(taskService, times(1)).getAllTasks();
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody().size(), tasks.size());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.TaskController.TaskController#getTask(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetTaskById() throws MangleException {
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        when(taskService.getTaskById(anyString())).thenReturn(task);
        ResponseEntity<Task<TaskSpec>> response = taskController.getTask(task.getId());
        verify(taskService, times(1)).getTaskById(anyString());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody(), task);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.TaskController.TaskController#getTask(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = { MangleException.class })
    public void testGetTaskByNullId() throws MangleException {
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        when(taskService.getTaskById(anyString())).thenReturn(task);
        try {
            taskController.getTask(null);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_TASK_FOUND);
            throw e;
        }
    }

    @Test
    public void testDeleteTasksSuccess() throws MangleException {
        DeleteOperationResponse operationResponse = new DeleteOperationResponse();
        String taskId = UUID.randomUUID().toString();
        List<String> tasks = new ArrayList<>(Arrays.asList(taskId));
        Map<String, List<String>> association = new HashMap<>();
        association.put(taskId, Collections.emptyList());
        operationResponse.setAssociations(association);

        when(taskDeletionService.deleteTasksByIds(tasks)).thenReturn(operationResponse);

        ResponseEntity<DeleteOperationResponse> response = taskController.deleteTasks(tasks);
        Assert.assertEquals(HttpStatus.PRECONDITION_FAILED, response.getStatusCode());
        Assert.assertEquals(response.getBody().getAssociations().size(), 1);
        Assert.assertEquals(response.getBody().getResult(), OperationStatus.FAILED);
        verify(taskDeletionService, times(1)).deleteTasksByIds(any());
    }

    @Test
    public void testDeleteTasksFail() throws MangleException {
        DeleteOperationResponse operationResponse = new DeleteOperationResponse();
        String taskId = UUID.randomUUID().toString();
        List<String> tasks = new ArrayList<>(Arrays.asList(taskId));

        when(taskDeletionService.deleteTasksByIds(tasks)).thenReturn(operationResponse);

        ResponseEntity<DeleteOperationResponse> response = taskController.deleteTasks(tasks);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(response.getBody().getAssociations().size(), 0);
        Assert.assertEquals(response.getBody().getResult(), OperationStatus.SUCCESS);
        verify(taskDeletionService, times(1)).deleteTasksByIds(any());
    }
}
