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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.events.listener.ScheduleEventListener;
import com.vmware.mangle.services.events.schedule.ScheduleCreatedEvent;
import com.vmware.mangle.services.events.schedule.ScheduleUpdatedEvent;
import com.vmware.mangle.services.hazelcast.HazelcastTaskCache;
import com.vmware.mangle.services.hazelcast.resiliencyscore.HazelcastResiliencyScoreTaskCache;
import com.vmware.mangle.services.helpers.TaskHelper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author chetanc
 */
public class ScheduledEventListenerTest {

    @Mock
    private HazelcastTaskCache taskCache;
    @Mock
    private HazelcastResiliencyScoreTaskCache resiliencyScoreTaskCache;
    @Mock
    private TaskHelper taskHelper;

    @InjectMocks
    private ScheduleEventListener scheduleEventListener;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandleSchedulerUpdatedEventCancelledSchedule() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        ScheduleUpdatedEvent event = new ScheduleUpdatedEvent(taskId, SchedulerStatus.CANCELLED.name());

        when(taskCache.deleteFromTaskCache(taskId)).thenReturn(taskId);
        when(taskHelper.getTaskType(any())).thenReturn(TaskType.INJECTION);

        scheduleEventListener.handleSchedulerUpdatedEvent(event);

        verify(taskCache, times(1)).deleteFromTaskCache(anyString());
        verify(taskCache, times(0)).updateTaskCache(anyString(), any());
        verify(taskHelper, times(1)).getTaskType(any());
    }

    @Test
    public void testHandleSchedulerUpdatedEventActiveSchedule() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        ScheduleUpdatedEvent event = new ScheduleUpdatedEvent(taskId, SchedulerStatus.SCHEDULED.name());

        doNothing().when(taskCache).updateTaskCache(eq(taskId), any());
        when(taskHelper.getTaskType(any())).thenReturn(TaskType.INJECTION);

        scheduleEventListener.handleSchedulerUpdatedEvent(event);

        verify(taskCache, times(0)).deleteFromTaskCache(anyString());
        verify(taskCache, times(1)).updateTaskCache(anyString(), any());
        verify(taskHelper, times(1)).getTaskType(any());
    }

    @Test
    public void testHandleSchedulerCreatedEventActiveSchedule() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        ScheduleCreatedEvent event = new ScheduleCreatedEvent(taskId, SchedulerStatus.SCHEDULED);

        when(taskCache.addTaskToCache(eq(taskId), any())).thenReturn(taskId);
        when(taskHelper.getTaskType(any())).thenReturn(TaskType.INJECTION);

        scheduleEventListener.handleSchedulerCreatedEvent(event);

        verify(taskCache, times(1)).addTaskToCache(anyString(), any());
        verify(taskHelper, times(1)).getTaskType(any());
    }


    @Test
    public void validateCancelledScheduleForResiliencyScoreTask() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        ScheduleUpdatedEvent event = new ScheduleUpdatedEvent(taskId, SchedulerStatus.CANCELLED.name());
        when(resiliencyScoreTaskCache.deleteFromTaskCache(taskId)).thenReturn(taskId);
        when(taskHelper.getTaskType(any())).thenReturn(TaskType.RESILIENCY_SCORE);

        scheduleEventListener.handleSchedulerUpdatedEvent(event);

        verify(resiliencyScoreTaskCache, times(1)).deleteFromTaskCache(anyString());
        verify(resiliencyScoreTaskCache, times(0)).updateHazelcastTaskCache(anyString(), any());
        verify(taskHelper, times(1)).getTaskType(any());
    }

    @Test
    public void testSchedulerUpdatedEventActiveScheduleForResiliencyScoreTask() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        ScheduleUpdatedEvent event = new ScheduleUpdatedEvent(taskId, SchedulerStatus.SCHEDULED.name());
        doNothing().when(resiliencyScoreTaskCache).updateHazelcastTaskCache(eq(taskId), any());
        when(taskHelper.getTaskType(any())).thenReturn(TaskType.RESILIENCY_SCORE);

        scheduleEventListener.handleSchedulerUpdatedEvent(event);

        verify(resiliencyScoreTaskCache, times(0)).deleteFromTaskCache(anyString());
        verify(resiliencyScoreTaskCache, times(1)).updateHazelcastTaskCache(anyString(), any());
        verify(taskHelper, times(1)).getTaskType(any());
    }

    @Test
    public void testSchedulerCreatedEventActiveScheduleForResiliencyScoreTask() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        ScheduleCreatedEvent event = new ScheduleCreatedEvent(taskId, SchedulerStatus.SCHEDULED);
        when(resiliencyScoreTaskCache.addTaskToCache(eq(taskId), any())).thenReturn(taskId);
        when(taskHelper.getTaskType(any())).thenReturn(TaskType.RESILIENCY_SCORE);

        scheduleEventListener.handleSchedulerCreatedEvent(event);

        verify(resiliencyScoreTaskCache, times(1)).addTaskToCache(anyString(), any());
        verify(taskHelper, times(1)).getTaskType(any());
    }

}
