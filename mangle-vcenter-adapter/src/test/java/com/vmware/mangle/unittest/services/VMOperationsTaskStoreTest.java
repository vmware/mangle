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

package com.vmware.mangle.unittest.services;

import java.util.UUID;

import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.service.VMOperationsTaskStore;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.status.VMOperationsTaskStatusEnum;

/**
 * @author Chethan C(chetanc)
 */
@PrepareForTest(VMOperationsTaskStore.class)
public class VMOperationsTaskStoreTest extends PowerMockTestCase {

    @BeforeMethod
    public void initMock() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddTask() throws MangleException {
        String taskId = VMOperationsTaskStore.generateTaskId();
        VMOperationsTaskStore.addTask(taskId, VMOperationsTaskStatusEnum.COMPLETED.toString());
        Assert.assertEquals(VMOperationsTaskStore.getTask(taskId).getTaskStatus(),
                VMOperationsTaskStatusEnum.COMPLETED.toString());
    }

    @Test
    public void testGetTaskStatus() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        VMOperationsTaskStore.addTask(taskId, VMOperationsTaskStatusEnum.COMPLETED.toString());
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId),
                VMOperationsTaskStatusEnum.COMPLETED.toString());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testGetTaskStatusFail() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        VMOperationsTaskStore.getTaskStatus(taskId);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testGetTaskFail() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        VMOperationsTaskStore.getTask(taskId);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testAddTaskFails() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        VMOperationsTaskStore.addTask(taskId, VMOperationsTaskStatusEnum.COMPLETED.toString());
        VMOperationsTaskStore.addTask(taskId, VMOperationsTaskStatusEnum.COMPLETED.toString());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testUpdateTaskFails() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.COMPLETED.toString(), "", null);
    }

    @Test
    public void testUpdateTask() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        VMOperationsTaskStore.addTask(taskId, VMOperationsTaskStatusEnum.TRIGGERED.toString());
        VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.COMPLETED.toString(), "", null);
    }

}
