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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.adapter.VMOperations;
import com.vmware.mangle.inventory.helpers.VMInventoryHelper;
import com.vmware.mangle.mockdata.VCenterSpecMockData;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.model.VMDiskDetails;
import com.vmware.mangle.service.VCenterClientInstantiationService;
import com.vmware.mangle.service.VCenterOperationsTaskStore;
import com.vmware.mangle.service.VMOperationsTaskService;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.status.VCenterOperationsTaskStatus;

/**
 * @author chetanc
 */

public class VMOperationsTaskServiceTest {

    private static String taskId;
    private final String VM_NAME = "dummy_vm";
    private final String VM_ID = "VM-08";
    private final String DISK_ID = "2001";
    private final String NIC_ID = "1000";
    public static String CLUSTER_NAME = "cluster_name";
    public static String DATACENTER_NAME = "datacenter_name";
    public static String HOST_NAME = "host_name";
    public static String FOLDER_NAME = "folder_name";
    public static String RESOURCE_POOL_NAME = "resource_pool_name";

    private VMOperationsTaskService vmOperationsTaskService;

    @Mock
    private VCenterClientInstantiationService vcClientInstantiationService;
    @Mock
    private VCenterClient vCenterClient;
    @Mock
    private VMInventoryHelper vmInventoryHelper;
    @Mock
    private VMOperations vmOperations;
    private VCenterOperationsTaskStore vCenterOperationsTaskStore = new VCenterOperationsTaskStore();

    VCenterSpec vCenterSpec = VCenterSpecMockData.getVCenterSpec();

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeMethod(alwaysRun = true)
    public void initMocks() throws MangleException {
        MockitoAnnotations.initMocks(this);
        vmOperationsTaskService = new VMOperationsTaskService(vcClientInstantiationService, vmInventoryHelper,
                vCenterOperationsTaskStore, vmOperations);
        taskId = UUID.randomUUID().toString();
        doNothing().when(vCenterClient).terminateConnection();
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
    }

    @Test
    public void testVMPowerOffSuccessful() throws MangleException {
        when(vmOperations.powerOffVM(anyObject(), anyString())).thenReturn(true);
        when(vmOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(true);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmInventoryHelper.getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME)).thenReturn(VM_ID);
        when(vmOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(false);

        vmOperationsTaskService.powerOffVM(VM_NAME, taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());

        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmOperations, times(1)).powerOffVM(anyObject(), anyString());
        verify(vmOperations, times(1)).isVMPoweredOn(anyObject(), anyString());
        verify(vmInventoryHelper, times(1)).getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME);
        verify(vmOperations, times(1)).isVMPoweredOn(anyObject(), anyString());
    }

    @Test
    public void testVMPowerOffSuccessfulById() throws MangleException {
        when(vmOperations.powerOffVM(anyObject(), anyString())).thenReturn(true);
        when(vmOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(true);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(false);

        vmOperationsTaskService.powerOffVMById(VM_ID, taskId, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());

        verify(vmOperations, times(1)).powerOffVM(anyObject(), anyString());
        verify(vmOperations, times(1)).isVMPoweredOn(anyObject(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmOperations, times(1)).isVMPoweredOn(anyObject(), anyString());
    }

    @Test
    public void testVMPowerOffFailAlreadyPowerOff() throws MangleException {
        when(vmOperations.powerOffVM(anyObject(), anyString())).thenThrow(new MangleException());
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmInventoryHelper.getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME)).thenReturn(VM_ID);

        vmOperationsTaskService.powerOffVM(VM_NAME, taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.FAILED.toString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmOperations, times(1)).powerOffVM(anyObject(), anyString());
        verify(vmInventoryHelper, times(1)).getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME);
    }

    @Test
    public void testVMPowerOnSuccessful() throws MangleException {
        when(vmOperations.powerOnVM(anyObject(), anyString())).thenReturn(true);
        when(vmOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(true);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmInventoryHelper.getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME)).thenReturn(VM_ID);

        vmOperationsTaskService.powerOnVM(VM_NAME, taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(vmOperations, times(1)).powerOnVM(anyObject(), anyString());
        verify(vmOperations, times(1)).isVMPoweredOn(anyObject(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmInventoryHelper, times(1)).getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME);
    }

    @Test
    public void testVMPowerOnSuccessfulById() throws MangleException {
        when(vmOperations.powerOnVM(anyObject(), anyString())).thenReturn(true);
        when(vmOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(true);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);

        vmOperationsTaskService.powerOnVMById(VM_ID, taskId, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmOperations, times(1)).powerOnVM(anyObject(), anyString());
        verify(vmOperations, times(1)).isVMPoweredOn(anyObject(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
    }

    @Test
    public void testVMPowerOnFail() throws MangleException {
        when(vmOperations.powerOnVM(anyObject(), anyString())).thenThrow(new MangleException());
        when(vmOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(true);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmInventoryHelper.getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME)).thenReturn(VM_ID);

        vmOperationsTaskService.powerOnVM(VM_NAME, taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.FAILED.toString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmOperations, times(1)).powerOnVM(anyObject(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmInventoryHelper, times(1)).getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME);
    }

    @Test
    public void testVMResetSuccessful() throws MangleException {
        when(vmOperations.resetVM(anyObject(), anyString())).thenReturn(true);
        when(vmOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(true);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmInventoryHelper.getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME)).thenReturn(VM_ID);

        vmOperationsTaskService.resetVM(VM_NAME, taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(vmOperations, times(1)).resetVM(anyObject(), anyString());
        verify(vmOperations, times(1)).isVMPoweredOn(anyObject(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmInventoryHelper, times(1)).getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME);
    }

    @Test
    public void testVMResetSuccessfulById() throws MangleException {
        when(vmOperations.resetVM(anyObject(), anyString())).thenReturn(true);
        when(vmOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(true);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);

        vmOperationsTaskService.resetVMById(VM_ID, taskId, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmOperations, times(1)).resetVM(anyObject(), anyString());
        verify(vmOperations, times(1)).isVMPoweredOn(anyObject(), anyString());
    }

    @Test
    public void testVMResetFailed() throws MangleException {
        when(vmOperations.resetVM(anyObject(), anyString())).thenThrow(new MangleException());
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmInventoryHelper.getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME)).thenReturn(VM_ID);

        vmOperationsTaskService.resetVM(VM_NAME, taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.FAILED.toString());
        verify(vmOperations, times(1)).resetVM(anyObject(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmInventoryHelper, times(1)).getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME);
    }

    @Test
    public void testVMSuspendSuccessful() throws MangleException {
        when(vmOperations.suspendVM(anyObject(), anyString())).thenReturn(true);
        when(vmOperations.isVMSuspended(anyObject(), anyString())).thenReturn(true);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmInventoryHelper.getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME)).thenReturn(VM_ID);

        vmOperationsTaskService.suspendVM(VM_NAME, taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME, vCenterSpec);
        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());

        verify(vmOperations, times(1)).suspendVM(anyObject(), anyString());
        verify(vmOperations, times(1)).isVMSuspended(anyObject(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmInventoryHelper, times(1)).getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME);
    }

    @Test
    public void testVMSuspendSuccessfulById() throws MangleException {
        when(vmOperations.suspendVM(anyObject(), anyString())).thenReturn(true);
        when(vmOperations.isVMSuspended(anyObject(), anyString())).thenReturn(true);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);

        vmOperationsTaskService.suspendVMById(VM_ID, taskId, vCenterSpec);
        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(vmOperations, times(1)).suspendVM(anyObject(), anyString());
        verify(vmOperations, times(1)).isVMSuspended(anyObject(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
    }

    @Test
    public void testVMSuspendFailed() throws MangleException {
        when(vmOperations.suspendVM(anyObject(), anyString())).thenThrow(new MangleException());
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmInventoryHelper.getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME)).thenReturn(VM_ID);

        vmOperationsTaskService.suspendVM(VM_NAME, taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME, vCenterSpec);
        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.FAILED.toString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmOperations, times(1)).suspendVM(anyObject(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmInventoryHelper, times(1)).getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME);
    }

    @Test
    public void testVMDiskDisconnectSuccessful() throws MangleException {
        when(vmOperations.deleteDiskFromVM(anyObject(), anyString(), anyString())).thenReturn(true);
        when(vmOperations.isDiskConnected(anyObject(), anyString(), anyString())).thenReturn(false);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmInventoryHelper.getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME)).thenReturn(VM_ID);

        vmOperationsTaskService.deleteDiskFromVM(VM_NAME, DISK_ID, taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(vmOperations, times(1)).deleteDiskFromVM(anyObject(), anyString(), anyString());
        verify(vmOperations, times(1)).isDiskConnected(anyObject(), anyString(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmInventoryHelper, times(1)).getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME);
    }

    @Test
    public void testVMDiskDisconnectSuccessfulById() throws MangleException {
        when(vmOperations.deleteDiskFromVM(anyObject(), anyString(), anyString())).thenReturn(true);
        when(vmOperations.isDiskConnected(anyObject(), anyString(), anyString())).thenReturn(false);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);

        vmOperationsTaskService.deleteDiskFromVMById(VM_ID, DISK_ID, taskId, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(vmOperations, times(1)).deleteDiskFromVM(anyObject(), anyString(), anyString());
        verify(vmOperations, times(1)).isDiskConnected(anyObject(), anyString(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
    }

    @Test
    public void testVMDiskDisconnectFailed() throws MangleException {
        when(vmOperations.deleteDiskFromVM(anyObject(), anyString(), anyString())).thenThrow(new MangleException());
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmInventoryHelper.getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME)).thenReturn(VM_ID);

        vmOperationsTaskService.deleteDiskFromVM(VM_NAME, DISK_ID, taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.FAILED.toString());
        verify(vmOperations, times(1)).deleteDiskFromVM(anyObject(), anyString(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmInventoryHelper, times(1)).getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME);
    }

    @Test
    public void testVMDiskConnectSuccessful() throws MangleException, JsonProcessingException {
        VMDiskDetails vmDiskDetails = new VMDiskDetails();
        when(vmOperations.addDiskToVM(eq(vCenterClient), any(), eq(VM_ID))).thenReturn(true);
        when(vmOperations.isDiskConnected(vCenterClient, VM_ID, DISK_ID)).thenReturn(true);
        when(vmOperations.resetVM(anyObject(), anyString())).thenReturn(true);
        when(vmOperations.isVMPoweredOn(eq(vCenterClient), eq(VM_ID))).thenReturn(true);

        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmInventoryHelper.getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME)).thenReturn(VM_ID);

        vmOperationsTaskService.addDiskToVM(VM_NAME, vmDiskDetails, DISK_ID, taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(vmOperations, times(1)).addDiskToVM(anyObject(), any(), anyString());
        verify(vmOperations, times(1)).isDiskConnected(anyObject(), anyString(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmInventoryHelper, times(1)).getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME);
        verify(vmOperations, times(1)).resetVM(anyObject(), anyString());
        verify(vmOperations, times(1)).isVMPoweredOn(eq(vCenterClient), eq(VM_ID));
    }

    @Test
    public void testVMDiskConnectSuccessfulById() throws MangleException, JsonProcessingException {
        VMDiskDetails vmDiskDetails = new VMDiskDetails();
        when(vmOperations.addDiskToVM(eq(vCenterClient), any(), eq(VM_ID))).thenReturn(true);
        when(vmOperations.isDiskConnected(vCenterClient, VM_ID, DISK_ID)).thenReturn(true);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmOperations.resetVM(anyObject(), anyString())).thenReturn(true);
        when(vmOperations.isVMPoweredOn(eq(vCenterClient), eq(VM_ID))).thenReturn(true);

        vmOperationsTaskService.addDiskToVMById(VM_ID, vmDiskDetails, DISK_ID, taskId, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(vmOperations, times(1)).addDiskToVM(eq(vCenterClient), any(), eq(VM_ID));
        verify(vmOperations, times(1)).isDiskConnected(vCenterClient, VM_ID, DISK_ID);
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmOperations, times(1)).resetVM(anyObject(), anyString());
        verify(vmOperations, times(1)).isVMPoweredOn(eq(vCenterClient), eq(VM_ID));
    }

    @Test
    public void testVMDiskConnectFailed() throws MangleException, JsonProcessingException {
        VMDiskDetails vmDiskDetails = new VMDiskDetails();
        when(vmOperations.addDiskToVM(eq(vCenterClient), any(), eq(VM_ID)))
                .thenThrow(new MangleException("NIC Not Found"));
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmInventoryHelper.getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME)).thenReturn(VM_ID);

        vmOperationsTaskService.addDiskToVM(VM_NAME, vmDiskDetails, DISK_ID, taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.FAILED.toString());
        verify(vmOperations, times(1)).addDiskToVM(eq(vCenterClient), any(), eq(VM_ID));
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmInventoryHelper, times(1)).getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME);
    }

    @Test
    public void testVMNicDisconnectSuccessful() throws MangleException {
        when(vmOperations.disconnectNicFromVM(anyObject(), anyString(), anyString())).thenReturn(true);
        when(vmOperations.isNicConnected(anyObject(), anyString(), anyString())).thenReturn(false);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmInventoryHelper.getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME)).thenReturn(VM_ID);

        vmOperationsTaskService.disconnectNicFromVM(VM_NAME, NIC_ID, taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(vmOperations, times(1)).disconnectNicFromVM(anyObject(), anyString(), anyString());
        verify(vmOperations, times(1)).isNicConnected(anyObject(), anyString(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmInventoryHelper, times(1)).getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME);
    }

    @Test
    public void testVMNicDisconnectSuccessfulById() throws MangleException {
        when(vmOperations.disconnectNicFromVM(anyObject(), anyString(), anyString())).thenReturn(true);
        when(vmOperations.isNicConnected(anyObject(), anyString(), anyString())).thenReturn(false);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);

        vmOperationsTaskService.disconnectNicFromVMById(VM_ID, NIC_ID, taskId, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmOperations, times(1)).disconnectNicFromVM(anyObject(), anyString(), anyString());
        verify(vmOperations, times(1)).isNicConnected(anyObject(), anyString(), anyString());
    }

    @Test
    public void testVMNICDisconnectFailed() throws MangleException {
        when(vmOperations.disconnectNicFromVM(anyObject(), anyString(), anyString())).thenThrow(new MangleException());
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        when(vmInventoryHelper.getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_POOL_NAME)).thenReturn(VM_ID);

        vmOperationsTaskService.disconnectNicFromVM(VM_NAME, NIC_ID, taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME, vCenterSpec);
        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.FAILED.toString());

        verify(vmOperations, times(1)).disconnectNicFromVM(anyObject(), anyString(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
        verify(vmInventoryHelper, times(1)).getVMId(vCenterClient, VM_NAME, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME, RESOURCE_POOL_NAME);
    }

    @Test
    public void testVMNicConnectSuccessful() throws MangleException {
        when(vmOperations.connectNicToVM(anyObject(), anyString(), anyString())).thenReturn(true);
        when(vmOperations.isNicConnected(anyObject(), anyString(), anyString())).thenReturn(true);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);

        vmOperationsTaskService.connectNicToVM(VM_ID, NIC_ID, taskId, vCenterSpec);
        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(vmOperations, times(1)).connectNicToVM(anyObject(), anyString(), anyString());
        verify(vmOperations, times(1)).isNicConnected(anyObject(), anyString(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
    }

    @Test
    public void testVMNicConnectSuccessfulById() throws MangleException {
        when(vmOperations.connectNicToVM(anyObject(), anyString(), anyString())).thenReturn(true);
        when(vmOperations.isNicConnected(anyObject(), anyString(), anyString())).thenReturn(true);
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);

        vmOperationsTaskService.connectNicToVM(VM_NAME, NIC_ID, taskId, vCenterSpec);
        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(vmOperations, times(1)).connectNicToVM(anyObject(), anyString(), anyString());
        verify(vmOperations, times(1)).isNicConnected(anyObject(), anyString(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
    }

    @Test
    public void testVMNICConnectFailedByVMId() throws MangleException {
        when(vmOperations.connectNicToVM(anyObject(), anyString(), anyString())).thenThrow(new MangleException());
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);

        vmOperationsTaskService.connectNicToVM(VM_ID, NIC_ID, taskId, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.FAILED.toString());
        verify(vmOperations, times(1)).connectNicToVM(anyObject(), anyString(), anyString());
        verify(vcClientInstantiationService, times(1)).getVCenterClient(vCenterSpec);
    }
}
