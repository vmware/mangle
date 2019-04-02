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

import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.events.listener.ScheduleEventListener;
import com.vmware.mangle.services.events.schedule.ScheduleCreatedEvent;
import com.vmware.mangle.services.events.schedule.ScheduleUpdatedEvent;
import com.vmware.mangle.services.hazelcast.HazelcastTaskCache;

/**
 *
 *
 * @author chetanc
 */
public class ScheduledEventListenerTest {

    @Mock
    private HazelcastTaskCache taskCache;

    @InjectMocks
    private ScheduleEventListener scheduleEventListener;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandleSchedulerUpdatedEventCancelledSchedule() {
        String taskId = UUID.randomUUID().toString();
        ScheduleUpdatedEvent event = new ScheduleUpdatedEvent(taskId, SchedulerStatus.CANCELLED);

        when(taskCache.deleteFromTaskCache(taskId)).thenReturn(taskId);

        scheduleEventListener.handleSchedulerUpdatedEvent(event);

        verify(taskCache, times(1)).deleteFromTaskCache(anyString());
        verify(taskCache, times(0)).updateTaskCache(anyString(), any());
    }

    @Test
    public void testHandleSchedulerUpdatedEventActiveSchedule() {
        String taskId = UUID.randomUUID().toString();
        ScheduleUpdatedEvent event = new ScheduleUpdatedEvent(taskId, SchedulerStatus.SCHEDULED);

        doNothing().when(taskCache).updateTaskCache(eq(taskId), any());

        scheduleEventListener.handleSchedulerUpdatedEvent(event);

        verify(taskCache, times(0)).deleteFromTaskCache(anyString());
        verify(taskCache, times(1)).updateTaskCache(anyString(), any());
    }

    @Test
    public void testHandleSchedulerCreatedEventActiveSchedule() {
        String taskId = UUID.randomUUID().toString();
        ScheduleCreatedEvent event = new ScheduleCreatedEvent(taskId, SchedulerStatus.SCHEDULED);

        doNothing().when(taskCache).updateTaskCache(eq(taskId), any());

        scheduleEventListener.handleSchedulerCreatedEvent(event);

        verify(taskCache, times(1)).updateTaskCache(anyString(), any());
    }
}
