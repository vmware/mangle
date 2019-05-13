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

package com.vmware.mangle.unittest.adapter;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.adapter.InventoryHelper;
import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.adapter.VMOperations;
import com.vmware.mangle.mockdata.InventoryHelperMockData;
import com.vmware.mangle.mockdata.VMOperationsFailedResponseMockData;
import com.vmware.mangle.model.ResourceObject;
import com.vmware.mangle.model.VCenterVMNic;
import com.vmware.mangle.model.VCenterVMState;
import com.vmware.mangle.model.VMDisk;
import com.vmware.mangle.model.response.VMOperationsRepsonse;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */

@PrepareForTest({ InventoryHelper.class })
public class VMOperationsTest extends PowerMockTestCase {

    public static String VM_NAME = "centos";
    public static String VM_DISK = "vmdisk";
    public static String VM_NIC = "vmnic";
    private InventoryHelperMockData inventoryHelperMockData = new InventoryHelperMockData();
    private ResponseEntity responseSuccess = new ResponseEntity(HttpStatus.OK);
    private ResponseEntity responseFailure = new ResponseEntity(HttpStatus.BAD_REQUEST);
    @Mock
    private VCenterClient VCenterClient;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(InventoryHelper.class);
    }

    @Test
    public void testPowerOffVMSuccess() throws Exception {
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        Assert.assertEquals(true, VMOperations.powerOffVM(VCenterClient, VM_NAME, null));
    }

    @Test
    public void testPowerOnVMSuccess() throws MangleException {
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        Assert.assertEquals(true, VMOperations.powerOnVM(VCenterClient, VM_NAME));
    }

    @Test
    public void testResetVMSuccess() throws MangleException {
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        Assert.assertEquals(true, VMOperations.resetVM(VCenterClient, VM_NAME));
    }

    @Test
    public void testSuspendVMSuccess() throws MangleException {
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        Assert.assertEquals(true, VMOperations.resetVM(VCenterClient, VM_NAME));
    }

    @Test
    public void testDeleteDiskVMSuccess() throws MangleException {
        when(VCenterClient.delete(anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        Assert.assertEquals(true, VMOperations.deleteDiskFromVM(VCenterClient, VM_DISK, VM_NAME));
    }

    @Test
    public void testConnectDiskVMSuccess() throws MangleException, JsonProcessingException {
        VMDisk vmDisk = new VMDisk();
        when(VCenterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseSuccess);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        Assert.assertEquals(true, VMOperations.addDiskToVM(VCenterClient, vmDisk, VM_NAME));
    }

    @Test
    public void testDisconnectNicVMSuccess() throws MangleException {
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        Assert.assertEquals(true, VMOperations.disconnectNicFromVM(VCenterClient, VM_NIC, VM_NAME));
    }

    @Test
    public void testConnectNicVMSuccess() throws MangleException {
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        Assert.assertEquals(true, VMOperations.connectNicToVM(VCenterClient, VM_NIC, VM_NAME));
    }

    @Test
    public void testIsDiskConnectedVMSuccess() throws MangleException {
        when(VCenterClient.get(anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        when(VCenterClient.delete(anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        Assert.assertEquals(true, VMOperations.isDiskConnected(VCenterClient, VM_DISK, VM_NAME));
    }

    @Test
    public void testIsDiskConnectedVMFail() throws MangleException {
        when(VCenterClient.get(anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        when(VCenterClient.delete(anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        Assert.assertEquals(false, VMOperations.isDiskConnected(VCenterClient, VM_DISK, VM_NAME));
    }

    @Test
    public void testIsGuestOSRunningSuccess() throws MangleException {
        when(VCenterClient.get(anyString(), eq(ResourceObject.class)))
                .thenReturn(inventoryHelperMockData.getSuccessResponseEntityWithDummyResponseObject());
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        when(InventoryHelper.convertLinkedHashMapToObject(anyObject(), eq(VCenterVMState.class)))
                .thenReturn(inventoryHelperMockData.getSuccessVCenterVMGuestOSState());
        Assert.assertEquals(true, VMOperations.isVMPoweredOn(VCenterClient, VM_NAME));
    }

    @Test
    public void testIsGuestOSRunningFailure() throws MangleException {
        when(VCenterClient.get(anyString(), eq(ResourceObject.class)))
                .thenReturn(inventoryHelperMockData.getSuccessResponseEntityWithDummyResponseObject());
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        when(InventoryHelper.convertLinkedHashMapToObject(anyObject(), eq(VCenterVMState.class)))
                .thenReturn(inventoryHelperMockData.getFailureVCenterVMGuestOSState());
        Assert.assertEquals(false, VMOperations.isVMPoweredOn(VCenterClient, VM_NAME));
    }

    @Test
    public void testIsNicConnected() throws MangleException {
        when(VCenterClient.get(anyString(), eq(ResourceObject.class)))
                .thenReturn(inventoryHelperMockData.getSuccessResponseEntityWithDummyResponseObject());
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        when(InventoryHelper.convertLinkedHashMapToObject(anyObject(), eq(VCenterVMNic.class)))
                .thenReturn(inventoryHelperMockData.getSuccessVCenterVMNicState());
        Assert.assertEquals(true, VMOperations.isNicConnected(VCenterClient, VM_NAME, VM_NIC));
    }

    @Test
    public void testIsNicDisConnected() throws MangleException {
        when(VCenterClient.get(anyString(), eq(ResourceObject.class)))
                .thenReturn(inventoryHelperMockData.getSuccessResponseEntityWithDummyResponseObject());
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        when(InventoryHelper.convertLinkedHashMapToObject(anyObject(), eq(VCenterVMNic.class)))
                .thenReturn(inventoryHelperMockData.getFailedVCenterVMNicState());
        Assert.assertEquals(false, VMOperations.isNicConnected(VCenterClient, VM_NAME, VM_NIC));
    }

    @Test(expectedExceptions = MangleException.class)
    public void testPowerOffVMFailure() throws MangleException {
        ResponseEntity responseFailure = VMOperationsFailedResponseMockData.getVMOperationsFailedResponseObj();
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        VMOperations.powerOffVM(VCenterClient, VM_NAME, null);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testPowerOnVMFailure() throws MangleException {
        ResponseEntity responseFailure = VMOperationsFailedResponseMockData.getVMOperationsFailedResponseObj();
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        VMOperations.powerOnVM(VCenterClient, VM_NAME);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testResetVMFailure() throws MangleException {
        ResponseEntity responseFailure = VMOperationsFailedResponseMockData.getVMOperationsFailedResponseObj();
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        VMOperations.resetVM(VCenterClient, VM_NAME);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testSuspendVMFailure() throws MangleException {
        ResponseEntity responseFailure = VMOperationsFailedResponseMockData.getVMOperationsFailedResponseObj();
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        VMOperations.resetVM(VCenterClient, VM_NAME);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testDeleteDiskVMFailure() throws MangleException {
        ResponseEntity responseFailure = VMOperationsFailedResponseMockData.getVMOperationsFailedResponseObj();
        when(VCenterClient.delete(anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        VMOperations.deleteDiskFromVM(VCenterClient, VM_DISK, VM_NAME);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testDisconnectNicVMFailure() throws MangleException {
        ResponseEntity responseFailure = VMOperationsFailedResponseMockData.getVMOperationsFailedResponseObj();
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        VMOperations.disconnectNicFromVM(VCenterClient, VM_NIC, VM_NAME);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testConnectDiskVMFailure() throws MangleException, JsonProcessingException {
        VMDisk vmDisk = new VMDisk();
        ResponseEntity responseFailure = VMOperationsFailedResponseMockData.getVMOperationsFailedResponseObj();
        when(VCenterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseFailure);
        when(InventoryHelper.getVMID(anyObject(), anyString(), eq(null))).thenReturn(VM_NAME);
        Assert.assertEquals(true, VMOperations.addDiskToVM(VCenterClient, vmDisk, VM_NAME));
    }
}
