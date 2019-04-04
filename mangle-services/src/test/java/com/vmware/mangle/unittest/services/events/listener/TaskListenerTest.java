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

package com.vmware.mangle.unittest.services.events.listener;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Stack;
import java.util.UUID;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.services.events.listener.MangleTaskListener;
import com.vmware.mangle.services.events.task.TaskCompletedEvent;
import com.vmware.mangle.services.events.task.TaskCreatedEvent;
import com.vmware.mangle.services.events.task.TaskModifiedEvent;
import com.vmware.mangle.services.hazelcast.HazelcastTaskCache;
import com.vmware.mangle.services.helpers.MetricProviderHelper;
import com.vmware.mangle.utils.PopulateFaultEventData;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.helpers.notifiers.WavefrontNotifier;

/**
 *
 *
 * @author chetanc
 */
public class TaskListenerTest {

    @Mock
    private Task task;

    @Mock
    private TaskService taskService;

    @Mock
    private HazelcastTaskCache mapService;

    @Mock
    MetricProviderHelper metricProvider;

    @Mock
    CommandExecutionFaultSpec commandExecutionFaultSpec;

    @Mock
    K8SFaultTriggerSpec k8sFaultTriggerSpec;

    @Mock
    TaskTrigger trigger;

    @Mock
    PopulateFaultEventData populateFaultEventData;

    @Mock
    FaultEventSpec faultEventInfo;

    @Mock
    EndpointSpec endpoint;

    @Mock
    TaskCompletedEvent mockTaskCompletedEvent;

    @InjectMocks
    private MangleTaskListener listener;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandleTaskModifiedEvent() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        TaskModifiedEvent event = new TaskModifiedEvent(task);

        when(task.getId()).thenReturn(taskId);
        when(task.isScheduledTask()).thenReturn(false);
        when(task.getTaskStatus()).thenReturn(TaskStatus.IN_PROGRESS);
        doNothing().when(mapService).updateTaskCache(anyString(), anyString());

        listener.handleTaskModifiedEvent(event);

        verify(task, times(1)).isScheduledTask();
        verify(task, times(4)).getTaskStatus();
        verify(mapService, times(1)).updateTaskCache(anyString(), anyString());
    }

    @Test
    public void testHandleTaskModifiedEventForScheduledTask() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        TaskModifiedEvent event = new TaskModifiedEvent(task);
        SchedulerSpec spec = mock(SchedulerSpec.class);

        when(task.getId()).thenReturn(taskId);
        when(task.isScheduledTask()).thenReturn(true);
        when(task.getTaskStatus()).thenReturn(TaskStatus.IN_PROGRESS);
        doNothing().when(mapService).updateTaskCache(anyString(), anyString());
        when(spec.getStatus()).thenReturn(SchedulerStatus.SCHEDULED);

        listener.handleTaskModifiedEvent(event);

        verify(task, times(1)).isScheduledTask();
        verify(task, times(1)).getTaskStatus();
        verify(mapService, times(0)).deleteFromTaskCache(anyString());
        verify(mapService, times(0)).updateTaskCache(anyString(), anyString());
    }

    @Test
    public void testHandleTaskCreatedEvent() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        TaskCreatedEvent event = new TaskCreatedEvent(task);

        when(task.getId()).thenReturn(taskId);
        when(taskService.getTaskById(taskId)).thenReturn(task);
        when(task.isScheduledTask()).thenReturn(false);
        when(task.getTaskStatus()).thenReturn(TaskStatus.IN_PROGRESS);
        when(mapService.addTaskToCache(anyString(), anyString())).thenReturn(taskId);

        listener.handleTaskCreatedEvent(event);

        verify(task, times(2)).getTaskStatus();
        verify(mapService, times(1)).addTaskToCache(anyString(), anyString());

    }

    @Test
    public void testHandletestHandleTaskCreatedEventForScheduledTask() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        TaskCreatedEvent event = new TaskCreatedEvent(task);
        SchedulerSpec spec = mock(SchedulerSpec.class);

        when(task.getId()).thenReturn(taskId);
        when(task.isScheduledTask()).thenReturn(true);
        when(task.getTaskStatus()).thenReturn(TaskStatus.IN_PROGRESS);
        when(mapService.addTaskToCache(anyString(), anyString())).thenReturn(taskId);
        when(spec.getStatus()).thenReturn(SchedulerStatus.SCHEDULED);

        listener.handleTaskCreatedEvent(event);

        verify(task, times(2)).getTaskStatus();
        verify(mapService, times(1)).addTaskToCache(anyString(), anyString());
    }


    @Test(description = "Test to validate the case when FaultEventData is NULL ")
    public void testTaskCompletedEventHandlerForNull() {
        initDefForMockObjectsForTaskCompletedEvent();
        when(commandExecutionFaultSpec.getEndpoint()).thenReturn(null);

        TaskCompletedEvent event = new TaskCompletedEvent(task);
        listener.handleTaskCompletedEvent(event);

        verify(task, times(1)).getId();
        verify(task, times(2)).getTaskData();
    }

    @Test(description = "Test to validate the case when FaultEventData constructed is not null")
    public void testTaskCompletedEventHandler() {
        WavefrontNotifier notifier = mock(WavefrontNotifier.class);
        initDefForMockObjectsForTaskCompletedEvent();
        when(mockTaskCompletedEvent.getTask()).thenReturn(task);
        when(task.getTaskData()).thenReturn(commandExecutionFaultSpec);
        when(commandExecutionFaultSpec.getEndpoint()).thenReturn(endpoint);
        when(metricProvider.getActiveNotificationProvider()).thenReturn(notifier);
        doNothing().when(notifier).sendEvent(faultEventInfo);

        listener.handleTaskCompletedEvent(mockTaskCompletedEvent);
        verify(mockTaskCompletedEvent, times(1)).getTask();
        verify(task, times(3)).getId();
        verify(task, times(2)).getTaskData();
        verify(metricProvider, times(1)).getActiveNotificationProvider();
    }

    private void initDefForMockObjectsForTaskCompletedEvent() {
        Stack<TaskTrigger> triggers = mock(Stack.class);
        when(task.getTriggers()).thenReturn(triggers);
        when(task.getTriggers().peek()).thenReturn(trigger);
        when(k8sFaultTriggerSpec.getFaultSpec()).thenReturn(commandExecutionFaultSpec);
        when(populateFaultEventData.getFaultEventSpec()).thenReturn(faultEventInfo);
        when(trigger.getStartTime()).thenReturn((new Date()).toGMTString());
        when(trigger.getEndTime()).thenReturn((new Date()).toGMTString());
        when(trigger.getTaskStatus()).thenReturn(TaskStatus.COMPLETED);
        when(task.getTaskStatus()).thenReturn(TaskStatus.COMPLETED);
        when(commandExecutionFaultSpec.getEndpointName()).thenReturn("dummyEpName");
    }
}
