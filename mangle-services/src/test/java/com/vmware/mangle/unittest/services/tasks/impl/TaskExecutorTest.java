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

package com.vmware.mangle.unittest.services.tasks.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import lombok.extern.log4j.Log4j2;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerInfo;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.services.PluginService;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.enums.MangleNodeStatus;
import com.vmware.mangle.services.scheduler.Scheduler;
import com.vmware.mangle.services.tasks.executor.TaskExecutor;
import com.vmware.mangle.services.tasks.helper.MockTaskHelper;
import com.vmware.mangle.task.framework.helpers.AbstractTaskHelper;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.constants.URLConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.CustomErrorMessage;

/**
 * TestNG class to test class UserServiceTest
 *
 * @author chetanc
 */
@Log4j2
public class TaskExecutorTest extends PowerMockTestCase {

    @InjectMocks
    private TaskExecutor<Task<? extends TaskSpec>> concurrentTaskRunner;

    @Mock
    private TaskService taskService;

    @Mock
    private CustomErrorMessage customErrorMessage;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private Scheduler scheduler;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private PluginService pluginService;

    private String injectionTaskId = "12345";

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(priority = 1)
    public void testMockTaskInjectionExecution() {
        MockTaskHelper<CommandExecutionFaultSpec> mockTask = null;
        Task<CommandExecutionFaultSpec> task = null;
        try {
            mockTask = new MockTaskHelper<>();
            task = mockTask.init(new CommandExecutionFaultSpec());
            mockTask.setCommandInfoExecutionHelper(new CommandInfoExecutionHelper());
            when(taskService.addOrUpdateTask(task)).thenReturn(null);
            when(pluginService.getExtension(task.getExtensionName())).thenReturn(getMockTaskHelper());
            doNothing().when(publisher).publishEvent(any());
            concurrentTaskRunner.submitTask(task);
        } catch (MangleException e) {
            log.error("testMockTaskInjectionExecution failed with exception: ", e);
            Assert.assertTrue(false);
        }
        while (task.getTaskStatus() != TaskStatus.COMPLETED && task.getTaskStatus() != TaskStatus.FAILED) {
            Assert.assertFalse(concurrentTaskRunner.getRunningTasks().isEmpty());
            CommonUtils.delayInMilliSeconds(1000);
        }
        Assert.assertEquals(task.getTaskDescription(), "Executing Fault: null on endpoint: null");
        Assert.assertEquals(task.getTriggers().size(), 1);
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertTrue(!concurrentTaskRunner.isExecuting(task));
        //        Assert.assertTrue(concurrentTaskRunner.isComplete(mockTask));
        Assert.assertTrue(concurrentTaskRunner.hasStarted(task));
    }

    @SuppressWarnings("unchecked")
    private <N extends TaskSpec> AbstractTaskHelper<N> getMockTaskHelper() {
        MockTaskHelper<CommandExecutionFaultSpec> mockTask = new MockTaskHelper<>();
        mockTask.setCommandInfoExecutionHelper(new CommandInfoExecutionHelper());
        return (AbstractTaskHelper<N>) mockTask;
    }

    @Test(priority = 2)
    public void testMockTaskInjectionRemediation() {
        MockTaskHelper<CommandExecutionFaultSpec> mockTask = null;
        Task<CommandExecutionFaultSpec> task = null;
        try {
            mockTask = new MockTaskHelper<>();
            task = mockTask.init(new CommandExecutionFaultSpec(), injectionTaskId);
            task.setId("12346");
            mockTask.setCommandInfoExecutionHelper(new CommandInfoExecutionHelper());
            doNothing().when(publisher).publishEvent(any());
            when(taskService.addOrUpdateTask(task)).thenReturn(null);
            when(pluginService.getExtension(MockTaskHelper.class.getName()))
                    .thenReturn((AbstractTaskHelper<TaskSpec>) getMockTaskHelper());
            concurrentTaskRunner.submitTask(task);
        } catch (MangleException e) {
            log.error("testMockTaskInjectionExecution failed with exception: ", e);
            Assert.assertTrue(false);
        }
        while (task.getTaskStatus() != TaskStatus.COMPLETED && task.getTaskStatus() != TaskStatus.FAILED) {
            CommonUtils.delayInMilliSeconds(1000);
        }
        Assert.assertEquals(task.getTaskStatus(), TaskStatus.COMPLETED);
        Assert.assertEquals(task.getTaskDescription(), "Executing Fault: null on endpoint: null");
        Assert.assertEquals(task.getTriggers().size(), 1);
        Assert.assertEquals(task.getTaskType(), TaskType.REMEDIATION);
    }

    @Test(priority = 3)
    public void testMockTaskInjectionSchedule() {
        MockTaskHelper<CommandExecutionFaultSpec> mockTask = null;
        Task<CommandExecutionFaultSpec> task = null;
        try {
            CommandExecutionFaultSpec faultSpec = new CommandExecutionFaultSpec();
            SchedulerInfo schedule = new SchedulerInfo();
            schedule.setTimeInMilliseconds(10L);
            schedule.setDescription("Simple schedule to test TaskExecutor");
            faultSpec.setSchedule(schedule);
            mockTask = new MockTaskHelper<>();
            task = mockTask.init(faultSpec);
            task.setId(injectionTaskId);
            task.setScheduledTask(true);
            task.getTaskData().getSchedule();
            when(taskService.addOrUpdateTask(task)).thenReturn(null);
            concurrentTaskRunner.submitTask(task);
        } catch (MangleException e) {
            log.error("testMockTaskInjectionExecution failed with exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertTrue(concurrentTaskRunner.getRunningTasks().isEmpty());
    }

    @Test(priority = 4)
    public void testMockTaskInjectionExecutionWithNodeStatusPause() {
        MockTaskHelper<CommandExecutionFaultSpec> mockTask = null;
        Task<CommandExecutionFaultSpec> task = null;
        try {
            mockTask = new MockTaskHelper<>();
            task = mockTask.init(new CommandExecutionFaultSpec());
            mockTask.setCommandInfoExecutionHelper(new CommandInfoExecutionHelper());
            URLConstants.setMangleNodeStatus(MangleNodeStatus.PAUSE);
            when(taskService.addOrUpdateTask(task)).thenReturn(null);
            when(pluginService.getExtension(task.getExtensionName())).thenReturn(getMockTaskHelper());
            doNothing().when(publisher).publishEvent(any());
            concurrentTaskRunner.submitTask(task);
        } catch (MangleException e) {
            log.error("testMockTaskInjectionExecution failed with exception: ", e);
            Assert.assertTrue(false);
        }
        while (task.getTaskStatus() != TaskStatus.TASK_SKIPPED) {
            Assert.assertTrue(concurrentTaskRunner.getRunningTasks().isEmpty());
            CommonUtils.delayInMilliSeconds(1000);
        }
        Assert.assertTrue(!concurrentTaskRunner.isExecuting(task));
        Assert.assertTrue(concurrentTaskRunner.hasStarted(task));
    }

    @Test(priority = 5)
    public void testMockTaskInjectionExecutionWithNodeStatusMaintenanceMode() {
        MockTaskHelper<CommandExecutionFaultSpec> mockTask = null;
        Task<CommandExecutionFaultSpec> task = null;
        try {
            mockTask = new MockTaskHelper<>();
            task = mockTask.init(new CommandExecutionFaultSpec());
            mockTask.setCommandInfoExecutionHelper(new CommandInfoExecutionHelper());
            URLConstants.setMangleNodeStatus(MangleNodeStatus.MAINTENANCE_MODE);
            when(taskService.addOrUpdateTask(task)).thenReturn(null);
            when(pluginService.getExtension(task.getExtensionName())).thenReturn(getMockTaskHelper());
            doNothing().when(publisher).publishEvent(any());
            concurrentTaskRunner.submitTask(task);
        } catch (MangleException e) {
            log.error("testMockTaskInjectionExecution failed with exception: ", e);
            Assert.assertTrue(false);
        }
        while (task.getTaskStatus() != TaskStatus.TASK_SKIPPED) {
            Assert.assertTrue(concurrentTaskRunner.getRunningTasks().isEmpty());
            CommonUtils.delayInMilliSeconds(1000);
        }
        Assert.assertTrue(!concurrentTaskRunner.isExecuting(task));
        Assert.assertTrue(concurrentTaskRunner.hasStarted(task));
    }

    @Test(priority = 6)
    public void testMockTaskInjectionExecutionWithMangleNodeStatusUpdateTask() {
        MockTaskHelper<CommandExecutionFaultSpec> mockTask = null;
        Task<CommandExecutionFaultSpec> task = null;
        try {
            mockTask = new MockTaskHelper<>();
            task = mockTask.init(new CommandExecutionFaultSpec());
            mockTask.setCommandInfoExecutionHelper(new CommandInfoExecutionHelper());
            URLConstants.setMangleNodeStatus(MangleNodeStatus.PAUSE);
            task.setTaskName("NodeStatusTask");
            when(taskService.addOrUpdateTask(task)).thenReturn(null);
            when(pluginService.getExtension(task.getExtensionName())).thenReturn(getMockTaskHelper());
            doNothing().when(publisher).publishEvent(any());
            concurrentTaskRunner.submitTask(task);
        } catch (MangleException e) {
            log.error("testMockTaskInjectionExecution failed with exception: ", e);
            Assert.assertTrue(false);
        }
        while (task.getTaskStatus() != TaskStatus.COMPLETED && task.getTaskStatus() != TaskStatus.FAILED) {
            Assert.assertFalse(concurrentTaskRunner.getRunningTasks().isEmpty());
            CommonUtils.delayInMilliSeconds(1000);
        }
        Assert.assertTrue(!concurrentTaskRunner.isExecuting(task));
        Assert.assertTrue(concurrentTaskRunner.hasStarted(task));
    }

}
