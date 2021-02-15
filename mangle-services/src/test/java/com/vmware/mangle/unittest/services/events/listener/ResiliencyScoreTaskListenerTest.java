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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.services.events.listener.ResiliencyScoreTaskListener;
import com.vmware.mangle.services.events.task.ResiliencyScoreTaskCreatedEvent;
import com.vmware.mangle.services.hazelcast.resiliencyscore.HazelcastResiliencyScoreTaskCache;
import com.vmware.mangle.services.mockdata.ResiliencyScoreMockData;

/**
 * @author dbhat
 */
public class ResiliencyScoreTaskListenerTest {
    @Mock
    private HazelcastResiliencyScoreTaskCache mapService;
    @Mock
    private ResiliencyScoreTask task;
    @InjectMocks
    private ResiliencyScoreTaskListener listener;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(priority = 1, description = "Validate Resiliency score task created event listener")
    public void validateTaskListener() {
        String taskId = ResiliencyScoreMockData.getRandomUUID();
        ResiliencyScoreTaskCreatedEvent event = new ResiliencyScoreTaskCreatedEvent(task);

        when(mapService.addTaskToCache(anyString(), anyString())).thenReturn(taskId);
        when(task.getId()).thenReturn(taskId);
        when(task.getTaskStatus()).thenReturn(TaskStatus.INITIALIZING);

        verify(task, times(1)).getId();
    }
}
