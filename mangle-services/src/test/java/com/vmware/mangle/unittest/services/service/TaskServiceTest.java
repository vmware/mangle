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
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.datastax.driver.core.PagingState;
import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.mockdata.TasksMockData;
import com.vmware.mangle.services.repository.TaskRepository;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author chetanc
 */
@Log4j2
public class TaskServiceTest {


    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;

    private TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(new CommandExecutionFaultSpec());

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        taskService = new TaskService(taskRepository);
    }

    /**
     * Test method for {@link TaskService#getAllTasks()}.
     *
     */
    @Test
    public void getAllTasksTest() {
        log.info("Executing test: getAllTasksTest on TaskService#getAllTasks");
        List<Task<TaskSpec>> tasks = tasksMockData.getDummyTasks();
        Mockito.when(taskRepository.findAll()).thenReturn(tasks);
        List<Task<TaskSpec>> response = taskService.getAllTasks();
        verify(taskRepository, Mockito.times(1)).findAll();
        Assert.assertEquals(response.size(), tasks.size());
    }

    /**
     * Test method for {@link TaskService#getTaskById(String)}.
     *
     */
    @Test
    public void getTaskByIdTest() {
        log.info("Executing test: getTaskByIdTest on TaskService#getTaskById");
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        Mockito.when(taskRepository.findById(anyString())).thenReturn(Optional.of(task));
        Task<TaskSpec> persistedTask = null;
        try {
            persistedTask = taskService.getTaskById(task.getId());
        } catch (MangleException e) {
            log.error(e);
            Assert.assertTrue(false, "Failed due to unexpected exception" + e.getMessage());
        }
        verify(taskRepository, Mockito.atLeastOnce()).findById(anyString());
        Assert.assertEquals(persistedTask, task);
    }

    /**
     * Test method for {@link TaskService#getTaskById(String)}.
     *
     * @throws MangleException
     *
     */
    @Test(expectedExceptions = { MangleException.class })
    public void getTaskByIdFailureForNullIdTest() throws MangleException {
        log.info("Executing test: getTaskByIdFailureForNullIdTest on TaskService#getTaskById");
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        Mockito.when(taskRepository.findById(anyString())).thenReturn(Optional.of(task));
        try {
            taskService.getTaskById(null);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            throw e;
        }
    }

    /**
     * Test method for {@link TaskService#getTaskById(String)}.
     *
     * @throws MangleException
     *
     */
    @Test(expectedExceptions = { MangleRuntimeException.class })
    public void getTaskByIdFailureForNotPresentTaskId() throws MangleException {
        log.info("Executing test: getTaskByIdFailureForNotPresentTaskId on TaskService#getTaskById");
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        Mockito.when(taskRepository.findById(anyString())).thenReturn(Optional.empty());
        try {
            taskService.getTaskById(task.getId());
        } catch (MangleRuntimeException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            throw e;
        }
    }

    /**
     * Test method for {@link TaskService#getTaskById(String)}.
     *
     * @throws MangleException
     *
     */
    @Test(expectedExceptions = { MangleException.class })
    public void getTaskByIdFailureForEmptyIdTest() throws MangleException {
        log.info("Executing test: getTaskByIdFailureForEmptyIdTest on TaskService#getTaskById");
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        Mockito.when(taskRepository.findById(anyString())).thenReturn(Optional.of(task));
        taskService.getTaskById(null);
        try {
            taskService.addOrUpdateTask(null);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            throw e;
        }
    }

    /**
     * Test method for {@link TaskService#addOrUpdateTask(Task)}
     *
     */
    @Test
    public void addOrUpdateTaskTest() throws MangleException {
        log.info("Executing test: addOrUpdateTaskTest on method TaskService#addOrUpdateTask(Task)");
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        Mockito.when(taskRepository.save(Mockito.any())).thenReturn(task);
        Task<TaskSpec> persistedTest = taskService.addOrUpdateTask(task);
        verify(taskRepository, Mockito.times(1)).save(Mockito.any());
        Assert.assertEquals(persistedTest, task);
    }

    /**
     * Test method for {@link TaskService#addOrUpdateTask(Task)}
     *
     */
    @Test(expectedExceptions = MangleException.class)
    public void addOrUpdateTaskTestFailure() throws MangleException {
        log.info("Executing test: addOrUpdateTaskTestFailure on method TaskService#createTask(Task)");
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        Mockito.when(taskRepository.save(Mockito.any())).thenReturn(task);
        try {
            taskService.addOrUpdateTask(null);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            throw e;
        }
    }

    /**
     * Test method for {@link TaskService#addOrUpdateTask(Task)}
     *
     */
    @Test(expectedExceptions = MangleException.class)
    public void updateTaskTestFailureNoRecord() throws MangleException {
        log.info("Executing test: updateTaskTestFailureNoRecord on method TaskService#updateTask(Task)");
        tasksMockData.getDummyTask();
        Mockito.when(taskRepository.save(Mockito.any())).thenReturn(null);
        try {
            taskService.addOrUpdateTask(null);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            verify(taskRepository, Mockito.times(0)).save(Mockito.any());
            throw e;
        }
    }

    /**
     * Test method for {@link TaskService#addOrUpdateTask(Task)}
     *
     */
    @Test
    public void udpateTaskTest() throws MangleException {
        log.info("Executing test: udpateTaskTest on method TaskService#updateTask(Task)");
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        Mockito.when(taskRepository.save(task)).then(new ReturnsArgumentAt(0));
        Task<TaskSpec> persistedTask1 = taskService.addOrUpdateTask(task);
        persistedTask1.setTaskDescription(persistedTask1.getTaskDescription() + "Modified");
        Task<TaskSpec> persistedTask2 = taskService.addOrUpdateTask(task);

        verify(taskRepository, Mockito.times(2)).save(Mockito.any());
        Assert.assertEquals(persistedTask1, persistedTask2);
    }

    /**
     * Test method for {@link TaskService#updateRemediationFieldofTaskById(String, Boolean)}
     *
     */
    @SuppressWarnings("unchecked")
    @Test
    public void updateRemediationFieldofTaskByIdTest() throws MangleException {
        log.info(
                "Executing test: updateRemediationFieldofTaskByIdTest on method TaskService#updateRemediationFieldofTaskById(Task)");
        FaultTask<?> task = (FaultTask<?>) tasksMockData.getDummyTask();
        Mockito.when(taskRepository.save(Mockito.any())).thenReturn(task);
        Mockito.when(taskRepository.findById(task.getId())).thenReturn(Optional.of((Task<TaskSpec>) task));
        FaultTask<?> persistedTask1 = (FaultTask<?>) taskService.updateRemediationFieldofTaskById(task.getId(), true);
        verify(taskRepository, Mockito.times(1)).save(Mockito.any());
        verify(taskRepository, Mockito.times(1)).save(Mockito.any());
        Assert.assertEquals(persistedTask1.isRemediated(), true);
    }

    /**
     * Test method for {@link TaskService#updateRemediationFieldofTaskById(String, Boolean)}.
     *
     */
    @Test(expectedExceptions = { MangleException.class })
    public void updateRemediationFieldofTaskByIdWithEmptyStringAsId() throws MangleException {
        log.info(
                "Executing test: updateRemediationFieldofTaskByIdWithEmptyStringAsId on TaskService#updateRemediationFieldofTaskById");
        try {
            taskService.updateRemediationFieldofTaskById("", null);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            throw e;
        }
    }

    /**
     * Test method for {@link TaskService#updateRemediationFieldofTaskById(String, Boolean)}.
     *
     */
    @Test(expectedExceptions = { MangleException.class })
    public void updateRemediationFieldofTaskByIdWithNullAsId() throws MangleException {
        log.info(
                "Executing test: updateRemediationFieldofTaskByIdWithNullAsId on TaskService#updateRemediationFieldofTaskById");
        try {
            taskService.updateRemediationFieldofTaskById(null, true);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            throw e;
        }
    }

    /**
     * Test method for {@link TaskService#updateRemediationFieldofTaskById(String, Boolean)}.
     *
     */
    @Test(expectedExceptions = { MangleException.class })
    public void updateRemediationFieldofTaskByIdWithNullAsArgs() throws MangleException {
        log.info(
                "Executing test: updateRemediationFieldofTaskByIdWithNullAsArgs on TaskService#updateRemediationFieldofTaskById");
        try {
            taskService.updateRemediationFieldofTaskById(null, null);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            throw e;
        }
    }

    /**
     * Test method for {@link TaskService#updateRemediationFieldofTaskById(String, Boolean)}.
     *
     */
    @Test(expectedExceptions = { MangleException.class })
    public void updateRemediationFieldofTaskByIdWithNullValueAsIsRemediated() throws MangleException {
        log.info(
                "Executing test: updateRemediationFieldofTaskByIdWithNullValueAsIsRemediated on TaskService#updateRemediationFieldofTaskById");
        try {
            taskService.updateRemediationFieldofTaskById("1234", null);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            throw e;
        }
    }

    /**
     * Test method for {@link TaskService#getTaskBasedOnPage(int, int)}.
     *
     */
    @Test
    public void getAllTasksBasedOnPageTest() {
        log.info("Executing test: getAllTasksBy on TaskService#getAllTasksBasedOnPageTest");
        List<Task<TaskSpec>> tasks = tasksMockData.getDummyTasks();
        Mockito.when(taskRepository.findAll(Mockito.any(Pageable.class))).thenReturn(new PageImpl<>(tasks));
        Slice<Task<TaskSpec>> response = taskService.getTaskBasedOnPage(1, 2);
        verify(taskRepository, Mockito.times(1)).findAll(Mockito.any(Pageable.class));
        Assert.assertTrue(response.getContent() != null);
    }

    /**
     * Test method for {@link TaskService#getTaskBasedOnPage(int, int)}.
     *
     */
    @SuppressWarnings("unchecked")
    @Test
    public void getAllTasksFromSecondPageTest() {
        log.info("Executing test: getAllTasksBy on TaskService#getAllTasksFromSecondPageTest");
        Slice<Task<TaskSpec>> slice = Mockito.mock(Slice.class);
        List<Task<TaskSpec>> tasks = tasksMockData.getDummyTasks();
        Mockito.when(slice.getContent()).thenReturn(tasks);
        Mockito.when(taskRepository.findAll(Mockito.any(Pageable.class))).thenReturn(slice);
        CassandraPageRequest pageable = Mockito.mock(CassandraPageRequest.class);
        when(slice.getPageable()).thenReturn(pageable);
        when(pageable.getPagingState()).thenReturn(null);
        Slice<Task<TaskSpec>> response = taskService.getTaskBasedOnPage(2, 2);
        verify(taskRepository, Mockito.times(1)).findAll(Mockito.any(Pageable.class));
        verify(slice, times(1)).getPageable();
        verify(pageable, times(1)).getPagingState();
        Assert.assertTrue(response.getContent() != null);
    }

    /**
     * Test method for {@link TaskService#getTaskBasedOnPage(int, int)}.
     *
     */
    @SuppressWarnings("unchecked")
    @Test
    public void getAllTasksFromThirdPageTest() {
        log.info("Executing test: getAllTasksBy on TaskService#getAllTasksFromThirdPageTest");
        Slice<Task<TaskSpec>> slice = Mockito.mock(Slice.class);
        List<Task<TaskSpec>> tasks = tasksMockData.getDummyTasks();
        Mockito.when(slice.getContent()).thenReturn(tasks);
        Mockito.when(taskRepository.findAll(Mockito.any(Pageable.class))).thenReturn(slice);
        CassandraPageRequest pageable = Mockito.mock(CassandraPageRequest.class);
        when(slice.getPageable()).thenReturn(pageable);
        when(pageable.getPageNumber()).thenReturn(3);
        when(pageable.getPageSize()).thenReturn(2);
        PagingState pagingState = Mockito.mock(PagingState.class);
        when(pageable.getPagingState()).thenReturn(pagingState);
        Slice<Task<TaskSpec>> response = taskService.getTaskBasedOnPage(3, 2);
        verify(taskRepository, Mockito.times(3)).findAll(Mockito.any(Pageable.class));
        verify(slice, times(4)).getPageable();
        verify(pageable, times(2)).getPagingState();
        Assert.assertTrue(response.getContent() != null);
    }

    /**
     * Test method for {@link TaskService#getTaskByIsScheduledTask(boolean)}.
     *
     */
    @Test
    public void getTaskByIsScheduledTaskTest() {
        log.info("Executing test: getTaskByIdTest on TaskService#getTaskByIsScheduledTask");
        List<Task<TaskSpec>> tasks = new ArrayList<>();
        tasks.add(tasksMockData.getDummyTask());
        Mockito.when(taskRepository.findByIsScheduledTask(anyBoolean())).thenReturn(tasks);
        List<Task<TaskSpec>> persistedTask = null;
        try {
            persistedTask = taskService.getTaskByIsScheduledTask(true);
        } catch (MangleException e) {
            log.error(e);
            Assert.assertTrue(false, "Failed due to unexpected exception" + e.getMessage());
        }
        verify(taskRepository, Mockito.atLeastOnce()).findByIsScheduledTask(anyBoolean());
        Assert.assertEquals(persistedTask, tasks);
    }

    /**
     * Test method for {@link TaskService#getTaskByIsScheduledTask(boolean)}.
     *
     * @throws MangleException
     *
     */
    @Test(expectedExceptions = { MangleRuntimeException.class })
    public void getTaskByIsScheduledTaskWithEmptyListTest() throws MangleException {
        log.info("Executing test: getTaskByIdTest on TaskService#getTaskByIsScheduledTaskWithEmptyListTest");
        List<Task<TaskSpec>> tasks = new ArrayList<>();
        Mockito.when(taskRepository.findByIsScheduledTask(anyBoolean())).thenReturn(tasks);
        try {
            taskService.getTaskByIsScheduledTask(true);
        } catch (MangleException e) {
            assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(taskRepository, Mockito.atLeastOnce()).findByIsScheduledTask(anyBoolean());
            throw e;
        }
    }

    /**
     * Test method for {@link TaskService#getTaskByIsScheduledTask(boolean)}.
     *
     * @throws MangleException
     *
     */
    @Test(expectedExceptions = { MangleRuntimeException.class })
    public void getTaskByIsScheduledTaskWithNullListTest() throws MangleException {
        log.info("Executing test: getTaskByIdTest on TaskService#getTaskByIsScheduledTaskWithNullListTest");
        Mockito.when(taskRepository.findByIsScheduledTask(anyBoolean())).thenReturn(null);
        try {
            taskService.getTaskByIsScheduledTask(true);
        } catch (MangleException e) {
            assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(taskRepository, Mockito.atLeastOnce()).findByIsScheduledTask(anyBoolean());
            throw e;
        }
    }

    /**
     * Test method for {@link TaskService#getTotalPages(Slice)}.
     *
     */
    @SuppressWarnings("unchecked")
    @Test
    public void getTotalPagesTest() {
        log.info("Executing test: getAllTasksTest on TaskService#getTotalPagesTest");
        Mockito.when(taskRepository.count()).thenReturn(5L);
        Slice<Task<TaskSpec>> slice = Mockito.mock(Slice.class);
        when(slice.getSize()).thenReturn(2);
        int response = taskService.getTotalPages(slice);
        verify(taskRepository, Mockito.times(1)).count();
        Assert.assertEquals(response, 3);
    }

    /**
     * Test method for {@link TaskService#getTotalPages(Slice)}.
     *
     */
    @SuppressWarnings("unchecked")
    @Test
    public void getTotalPagesWithSliceSizeZeroTest() {
        log.info("Executing test: getAllTasksTest on TaskService#getTotalPagesTest");
        Mockito.when(taskRepository.count()).thenReturn(1L);
        Slice<Task<TaskSpec>> slice = Mockito.mock(Slice.class);
        when(slice.getSize()).thenReturn(0);
        int response = taskService.getTotalPages(slice);
        verify(taskRepository, Mockito.times(1)).count();
        Assert.assertEquals(response, 1);
    }


    @Test
    public void testGetTasksByIds() {
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        List<Task<TaskSpec>> tasks = new ArrayList<>();
        tasks.add(task);
        List<String> taskIds = new ArrayList<>(Arrays.asList(task.getId()));
        Mockito.when(taskRepository.findByIds(taskIds)).thenReturn(tasks);

        List<Task<TaskSpec>> retrievedTasks = taskService.getTasksByIds(taskIds);

        Assert.assertEquals(1, retrievedTasks.size());
        verify(taskRepository, times(1)).findByIds(any());
    }
}
