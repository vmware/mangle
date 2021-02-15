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

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.adapter.VMOperations;
import com.vmware.mangle.mockdata.VMOperationsFailedResponseMockData;
import com.vmware.mangle.model.VMDiskDetails;
import com.vmware.mangle.model.resource.VMOperationsRepsonse;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */

public class VMOperationsTest {

    public static String VM_NAME = "centos";
    public static String VM_DISK = "vmdisk";
    public static String VM_NIC = "vmnic";
    public static String vmId = "vmId";
    private ResponseEntity responseSuccess = new ResponseEntity(HttpStatus.OK);
    private ResponseEntity responseFailure = new ResponseEntity(HttpStatus.BAD_REQUEST);
    @Mock
    private VCenterClient VCenterClient;
    VMOperations vmOperations;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        vmOperations = new VMOperations();
    }

    @Test
    public void testPowerOffVMSuccess() throws Exception {
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        Assert.assertTrue(vmOperations.powerOffVM(VCenterClient, vmId));
    }

    @Test
    public void testPowerOnVMSuccess() throws MangleException {
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        Assert.assertTrue(vmOperations.powerOnVM(VCenterClient, vmId));
    }

    @Test
    public void testResetVMSuccess() throws MangleException {
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        Assert.assertTrue(vmOperations.resetVM(VCenterClient, vmId));
    }

    @Test
    public void testSuspendVMSuccess() throws MangleException {
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        Assert.assertTrue(vmOperations.resetVM(VCenterClient, vmId));
    }

    @Test
    public void testDeleteDiskVMSuccess() throws MangleException {
        when(VCenterClient.delete(anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        Assert.assertTrue(vmOperations.deleteDiskFromVM(VCenterClient, VM_DISK, vmId));
    }

    @Test
    public void testConnectDiskVMSuccess() throws MangleException, JsonProcessingException {
        VMDiskDetails vmDiskDetails = new VMDiskDetails();
        when(VCenterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseSuccess);
        Assert.assertTrue(vmOperations.addDiskToVM(VCenterClient, vmDiskDetails, vmId));
    }

    @Test
    public void testDisconnectNicVMSuccess() throws MangleException {
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        Assert.assertTrue(vmOperations.disconnectNicFromVM(VCenterClient, VM_NIC, vmId));
    }

    @Test
    public void testConnectNicVMSuccess() throws MangleException {
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        Assert.assertTrue(vmOperations.connectNicToVM(VCenterClient, VM_NIC, vmId));
    }

    @Test
    public void testIsDiskConnectedVMSuccess() throws MangleException {
        when(VCenterClient.get(anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        when(VCenterClient.delete(anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        Assert.assertTrue(vmOperations.isDiskConnected(VCenterClient, VM_DISK, VM_NAME));
    }

    @Test
    public void testIsDiskConnectedVMFail() throws MangleException {
        when(VCenterClient.get(anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        when(VCenterClient.delete(anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        Assert.assertFalse(vmOperations.isDiskConnected(VCenterClient, VM_DISK, VM_NAME));
    }

    @Test(expectedExceptions = MangleException.class)
    public void testPowerOffVMFailure() throws MangleException {
        ResponseEntity responseFailure = VMOperationsFailedResponseMockData.getVMOperationsFailedResponseObj();
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        vmOperations.powerOffVM(VCenterClient, vmId);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testPowerOnVMFailure() throws MangleException {
        ResponseEntity responseFailure = VMOperationsFailedResponseMockData.getVMOperationsFailedResponseObj();
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        vmOperations.powerOnVM(VCenterClient, vmId);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testResetVMFailure() throws MangleException {
        ResponseEntity responseFailure = VMOperationsFailedResponseMockData.getVMOperationsFailedResponseObj();
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        vmOperations.resetVM(VCenterClient, vmId);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testSuspendVMFailure() throws MangleException {
        ResponseEntity responseFailure = VMOperationsFailedResponseMockData.getVMOperationsFailedResponseObj();
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        vmOperations.resetVM(VCenterClient, vmId);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testDeleteDiskVMFailure() throws MangleException {
        ResponseEntity responseFailure = VMOperationsFailedResponseMockData.getVMOperationsFailedResponseObj();
        when(VCenterClient.delete(anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        vmOperations.deleteDiskFromVM(VCenterClient, VM_DISK, vmId);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testDisconnectNicVMFailure() throws MangleException {
        ResponseEntity responseFailure = VMOperationsFailedResponseMockData.getVMOperationsFailedResponseObj();
        when(VCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseFailure);
        vmOperations.disconnectNicFromVM(VCenterClient, VM_NIC, vmId);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testConnectDiskVMFailure() throws MangleException, JsonProcessingException {
        VMDiskDetails vmDiskDetails = new VMDiskDetails();
        ResponseEntity responseFailure = VMOperationsFailedResponseMockData.getVMOperationsFailedResponseObj();
        when(VCenterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseFailure);
        Assert.assertTrue(vmOperations.addDiskToVM(VCenterClient, vmDiskDetails, vmId));
    }
}
