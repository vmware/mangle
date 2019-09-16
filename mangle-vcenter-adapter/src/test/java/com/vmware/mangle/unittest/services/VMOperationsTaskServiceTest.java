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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.adapter.VMOperations;
import com.vmware.mangle.mockdata.VCenterSpecMockData;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.model.VMDisk;
import com.vmware.mangle.service.VCenterClientInstantiationService;
import com.vmware.mangle.service.VMOperationsTaskService;
import com.vmware.mangle.service.VMOperationsTaskStore;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.status.VMOperationsTaskStatusEnum;

/**
 * @author chetanc
 */

@PrepareForTest(VMOperations.class)
public class VMOperationsTaskServiceTest extends PowerMockTestCase {

    private static String taskId;
    private final String VM_NAME = "dummy_vm";
    private final String VM_ID = "VM-08";
    private final String DISK_ID = "2001";
    private final String NIC_ID = "1000";
    @InjectMocks
    VMOperationsTaskService vmOperationsTaskService;

    @Mock
    VCenterClientInstantiationService vCenterClientInstantiationService;

    @Mock
    VCenterClient vCenterClient;

    VCenterSpec vCenterSpec = VCenterSpecMockData.getVCenterSpec();

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeMethod(alwaysRun = true)
    public void initMocks() throws MangleException {
        MockitoAnnotations.initMocks(this);
        mockStatic(VMOperations.class);
        taskId = UUID.randomUUID().toString();
        doNothing().when(vCenterClient).terminateConnection();
        when(vCenterClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        // vmOperationsTaskService.setWaitCycleCount(1);
    }

    @Test
    public void testVMPowerOffSuccessfull() throws MangleException {
        when(VMOperations.powerOffVM(anyObject(), anyString(), eq(null))).thenReturn(true);
        when(VMOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(false);
        vmOperationsTaskService.powerOffVM(VM_NAME, taskId, null, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId),
                VMOperationsTaskStatusEnum.COMPLETED.toString());
    }

    @Test
    public void testVMPowerOffFailAlreadyPowerOff() throws MangleException {
        when(VMOperations.powerOffVM(anyObject(), anyString(), eq(null))).thenThrow(new MangleException());
        vmOperationsTaskService.powerOffVM(VM_NAME, taskId, null, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId), VMOperationsTaskStatusEnum.FAILED.toString());
    }

    @Test
    public void testVMPowerOnSuccessful() throws MangleException {
        when(VMOperations.powerOnVM(anyObject(), anyString())).thenReturn(true);
        when(VMOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(true);
        vmOperationsTaskService.powerOnVM(VM_NAME, taskId, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId),
                VMOperationsTaskStatusEnum.COMPLETED.toString());
    }

    @Test
    public void testVMPowerOnFail() throws MangleException {
        when(VMOperations.powerOnVM(anyObject(), anyString())).thenThrow(new MangleException());
        vmOperationsTaskService.powerOnVM(VM_NAME, taskId, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId), VMOperationsTaskStatusEnum.FAILED.toString());
    }

    @Test
    public void testVMResetSuccessful() throws MangleException {
        when(VMOperations.resetVM(anyObject(), anyString())).thenReturn(true);
        when(VMOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(true);
        vmOperationsTaskService.resetVM(VM_NAME, taskId, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId),
                VMOperationsTaskStatusEnum.COMPLETED.toString());
    }

    @Test
    public void testVMResetFailed() throws MangleException {
        when(VMOperations.resetVM(anyObject(), anyString())).thenThrow(new MangleException());
        vmOperationsTaskService.resetVM(VM_NAME, taskId, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId), VMOperationsTaskStatusEnum.FAILED.toString());
    }

    @Test
    public void testVMSuspendSuccessful() throws MangleException {
        when(VMOperations.suspendVM(anyObject(), anyString())).thenReturn(true);
        when(VMOperations.isVMSuspended(anyObject(), anyString())).thenReturn(true);
        vmOperationsTaskService.suspendVM(VM_NAME, taskId, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId),
                VMOperationsTaskStatusEnum.COMPLETED.toString());
    }

    @Test
    public void testVMSuspendFailed() throws MangleException {
        when(VMOperations.suspendVM(anyObject(), anyString())).thenThrow(new MangleException());
        vmOperationsTaskService.suspendVM(VM_NAME, taskId, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId), VMOperationsTaskStatusEnum.FAILED.toString());
    }

    @Test
    public void testVMDiskDisconnectSuccessful() throws MangleException {
        when(VMOperations.deleteDiskFromVM(anyObject(), anyString(), anyString())).thenReturn(true);
        when(VMOperations.isDiskConnected(anyObject(), anyString(), anyString())).thenReturn(false);
        vmOperationsTaskService.deleteDiskFromVM(VM_NAME, DISK_ID, taskId, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId),
                VMOperationsTaskStatusEnum.COMPLETED.toString());
    }

    @Test
    public void testVMDiskDisconnectFailed() throws MangleException {
        when(VMOperations.deleteDiskFromVM(anyObject(), anyString(), anyString())).thenThrow(new MangleException());
        vmOperationsTaskService.deleteDiskFromVM(VM_NAME, DISK_ID, taskId, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId), VMOperationsTaskStatusEnum.FAILED.toString());
    }

    @Test
    public void testVMDiskConnectSuccessful() throws MangleException, JsonProcessingException {
        VMDisk vmDisk = new VMDisk();
        when(VMOperations.addDiskToVM(vCenterClient, vmDisk, VM_NAME)).thenReturn(true);
        when(VMOperations.isDiskConnected(vCenterClient, VM_NAME, DISK_ID)).thenReturn(true);
        vmOperationsTaskService.addDiskToVM(VM_NAME, vmDisk, DISK_ID, taskId, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId),
                VMOperationsTaskStatusEnum.COMPLETED.toString());
    }

    @Test
    public void testVMDiskConnectFailed() throws MangleException, JsonProcessingException {
        VMDisk vmDisk = new VMDisk();
        when(VMOperations.addDiskToVM(vCenterClient, vmDisk, VM_NAME)).thenThrow(new MangleException("NIC Not Found"));
        vmOperationsTaskService.addDiskToVM(VM_NAME, vmDisk, DISK_ID, taskId, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId), VMOperationsTaskStatusEnum.FAILED.toString());
    }

    @Test
    public void testVMNicDisconnectSuccessful() throws MangleException {
        when(VMOperations.disconnectNicFromVM(anyObject(), anyString(), anyString())).thenReturn(true);
        when(VMOperations.isNicConnected(anyObject(), anyString(), anyString())).thenReturn(false);
        vmOperationsTaskService.disonnectNicFromVM(VM_NAME, NIC_ID, taskId, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId),
                VMOperationsTaskStatusEnum.COMPLETED.toString());
    }

    @Test
    public void testVMNICDisconnectFailed() throws MangleException {
        when(VMOperations.disconnectNicFromVM(anyObject(), anyString(), anyString())).thenThrow(new MangleException());
        vmOperationsTaskService.disonnectNicFromVM(VM_NAME, NIC_ID, taskId, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId), VMOperationsTaskStatusEnum.FAILED.toString());
    }

    @Test
    public void testVMNicConnectSuccessful() throws MangleException {
        when(VMOperations.connectNicToVM(anyObject(), anyString(), anyString())).thenReturn(true);
        when(VMOperations.isNicConnected(anyObject(), anyString(), anyString())).thenReturn(true);
        vmOperationsTaskService.connectNicToVM(VM_NAME, NIC_ID, taskId, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId),
                VMOperationsTaskStatusEnum.COMPLETED.toString());
    }

    @Test
    public void testVMNICConnectFailed() throws MangleException {
        when(VMOperations.connectNicToVM(anyObject(), anyString(), anyString())).thenThrow(new MangleException());
        vmOperationsTaskService.connectNicToVM(VM_NAME, NIC_ID, taskId, vCenterSpec);
        Assert.assertEquals(VMOperationsTaskStore.getTaskStatus(taskId), VMOperationsTaskStatusEnum.FAILED.toString());
    }
}
