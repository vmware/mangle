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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.model.enums.OperationStatus;
import com.vmware.mangle.services.deletionutils.SchedulerDeletionService;
import com.vmware.mangle.services.repository.SchedulerRepository;

/**
 *
 *
 * @author chetanc
 */
public class SchedulerDeletionServiceTest {

    @Mock
    private SchedulerRepository schedulerRepository;

    private SchedulerDeletionService schedulerDeletionService;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        schedulerDeletionService = new SchedulerDeletionService(schedulerRepository);
    }

    @Test
    public void testDeleteSchedulerDetailsByJobIds() {
        String taskId = UUID.randomUUID().toString();
        List<String> tasks = new ArrayList<>(Arrays.asList(taskId));
        doNothing().when(schedulerRepository).deleteByIdIn(any());

        OperationStatus status = schedulerDeletionService.deleteSchedulerDetailsByJobIds(tasks);
        Assert.assertEquals(status, OperationStatus.SUCCESS);
        verify(schedulerRepository, times(1)).deleteByIdIn(any());
    }

    @Test
    public void testDeleteSchedulerDetailsByJobIdsEmptyTasks() {
        List<String> tasks = new ArrayList<>();
        doNothing().when(schedulerRepository).deleteByIdIn(any());

        OperationStatus status = schedulerDeletionService.deleteSchedulerDetailsByJobIds(tasks);
        Assert.assertEquals(status, OperationStatus.FAILED);
        verify(schedulerRepository, times(0)).deleteByIdIn(any());
    }
}
