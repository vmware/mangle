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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.model.resource.VCenterOperationTask;
import com.vmware.mangle.service.VCenterOperationsTaskStore;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.status.VCenterOperationsTaskStatus;

/**
 * @author Chethan C(chetanc)
 */
public class VCenterOperationsTaskStoreTest {
    VCenterOperationsTaskStore vCenterOperationsTaskStore;

    @BeforeMethod
    public void initMock() {
        vCenterOperationsTaskStore = new VCenterOperationsTaskStore();
    }

    @Test
    public void testAddTask() throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.COMPLETED.toString());
        Assert.assertEquals(vCenterOperationsTaskStore.getTask(taskId).getTaskStatus(),
                VCenterOperationsTaskStatus.COMPLETED.toString());
    }

    @Test
    public void testGetTaskStatus() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.COMPLETED.toString());
        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testGetTaskStatusFail() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        vCenterOperationsTaskStore.getTaskStatus(taskId);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testGetTaskFail() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        vCenterOperationsTaskStore.getTask(taskId);
    }

    @Test
    public void testAddTaskComplete() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        VCenterOperationTask vCenterOperationTask = vCenterOperationsTaskStore.addTask(taskId,
                VCenterOperationsTaskStatus.COMPLETED.toString());
        Assert.assertEquals(vCenterOperationTask.getTaskStatus(), VCenterOperationsTaskStatus.COMPLETED.toString());

    }

    @Test(expectedExceptions = MangleException.class)
    public void testUpdateTaskFails() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.COMPLETED.toString(), "", null);
    }

    @Test
    public void testUpdateTask() throws MangleException {
        String taskId = UUID.randomUUID().toString();
        vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
        vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.COMPLETED.toString(), "", null);
    }

}
