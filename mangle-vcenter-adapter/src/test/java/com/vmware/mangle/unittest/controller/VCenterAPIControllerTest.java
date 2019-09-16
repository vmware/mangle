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

package com.vmware.mangle.unittest.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.controller.VCenterAPIController;
import com.vmware.mangle.mockdata.VCenterSpecMockData;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.model.VMDiskRequestBody;
import com.vmware.mangle.model.response.VCenterTaskTriggeredResource;
import com.vmware.mangle.service.VCenterClientInstantiationService;
import com.vmware.mangle.service.VMOperationsTaskService;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 ** @author Chethan C(chetanc)
 *
 */
@PrepareForTest({ VCenterAPIController.class, VCenterTaskTriggeredResource.class })
public class VCenterAPIControllerTest extends PowerMockTestCase {

    public static String TASK_ID = "8ae7a287-d39d-42e8-a60b-dcaae4b9046e";
    public static String VM_NAME = "vm_name";
    public static String DISK_NAME = "disk_name";
    public static String NIC_NAME = "nic_name";
    @InjectMocks
    VCenterAPIController vCenterAPIController;

    VCenterSpec vCenterSpec = VCenterSpecMockData.getVCenterSpec();
    @Mock
    private VCenterClient VCenterClient;
    @Mock
    private VMOperationsTaskService vmOperationsTaskService;
    @Mock
    private VCenterClientInstantiationService VCenterClientInstantiationService;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSuccessfulTestConnection() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        when(vmOperationsTaskService.testConnection(vCenterSpec)).thenReturn(true);
        ResponseEntity response = vCenterAPIController.testConnection(vCenterSpec);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testFailureTestConnection() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        when(VCenterClient.testConnection()).thenReturn(false);
        ResponseEntity response = vCenterAPIController.testConnection(vCenterSpec);
        Assert.assertNotEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testSuccessShutDownByVMName() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doNothing().when(vmOperationsTaskService).powerOffVM(anyString(), anyString(), anyString(), eq(vCenterSpec));
        ResponseEntity responseEntity = vCenterAPIController.shutDownVMByName(VM_NAME, vCenterSpec, null);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorShutDownByVMName() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doThrow(MangleException.class).when(vmOperationsTaskService).powerOffVM(anyString(), anyString(), anyString(),
                eq(vCenterSpec));
        ResponseEntity responseEntity = vCenterAPIController.shutDownVMByName(VM_NAME, vCenterSpec, null);
    }

    @Test
    public void testSuccessTunOnVMByName() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doNothing().when(vmOperationsTaskService).powerOnVM(anyString(), anyString(), eq(vCenterSpec));
        ResponseEntity responseEntity = vCenterAPIController.turnOnVMByName(VM_NAME, vCenterSpec);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorTunOnVMByName() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doThrow(MangleException.class).when(vmOperationsTaskService).powerOnVM(anyString(), anyString(),
                eq(vCenterSpec));
        ResponseEntity responseEntity = vCenterAPIController.turnOnVMByName(VM_NAME, vCenterSpec);
    }

    @Test
    public void testSuccessResetVMByName() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doNothing().when(vmOperationsTaskService).resetVM(anyString(), anyString(), eq(vCenterSpec));
        ResponseEntity responseEntity = vCenterAPIController.resetVMByName(VM_NAME, vCenterSpec);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorResetVMByName() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doThrow(MangleException.class).when(vmOperationsTaskService).resetVM(anyString(), anyString(), eq(vCenterSpec));
        ResponseEntity responseEntity = vCenterAPIController.resetVMByName(VM_NAME, vCenterSpec);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testSuccessSuspendVMByName() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doNothing().when(vmOperationsTaskService).suspendVM(anyString(), anyString(), eq(vCenterSpec));
        ResponseEntity responseEntity = vCenterAPIController.suspendVMByName(VM_NAME, vCenterSpec);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorSuspendVMByName() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doThrow(MangleException.class).when(vmOperationsTaskService).suspendVM(anyString(), anyString(),
                eq(vCenterSpec));
        ResponseEntity responseEntity = vCenterAPIController.suspendVMByName(VM_NAME, vCenterSpec);
    }

    @Test
    public void testSuccessConnectDiskToVM() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doNothing().when(vmOperationsTaskService).addDiskToVM(anyString(), anyObject(), anyString(), anyString(),
                eq(vCenterSpec));
        ResponseEntity responseEntity =
                vCenterAPIController.connectDiskToVM(VM_NAME, DISK_NAME, new VMDiskRequestBody());
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorConnectDiskToVM() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doThrow(MangleException.class).when(vmOperationsTaskService).addDiskToVM(anyString(), anyObject(), anyString(),
                anyString(), any());
        ResponseEntity responseEntity =
                vCenterAPIController.connectDiskToVM(VM_NAME, DISK_NAME, new VMDiskRequestBody());
    }

    @Test
    public void testSuccessDisconnectDiskToVM() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doNothing().when(vmOperationsTaskService).deleteDiskFromVM(anyString(), anyString(), anyString(),
                eq(vCenterSpec));
        ResponseEntity responseEntity = vCenterAPIController.deleteDiskFromVM(VM_NAME, DISK_NAME, vCenterSpec);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorDisconnectDiskToVM() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doThrow(MangleException.class).when(vmOperationsTaskService).deleteDiskFromVM(anyString(), anyString(),
                anyString(), eq(vCenterSpec));
        ResponseEntity responseEntity = vCenterAPIController.deleteDiskFromVM(VM_NAME, DISK_NAME, vCenterSpec);
    }

    @Test
    public void testSuccessConnectNicToVM() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doNothing().when(vmOperationsTaskService).connectNicToVM(anyString(), anyString(), anyString(),
                eq(vCenterSpec));
        ResponseEntity responseEntity = vCenterAPIController.connectNicToVM(VM_NAME, NIC_NAME, vCenterSpec);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorConnectNicToVM() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doThrow(MangleException.class).when(vmOperationsTaskService).connectNicToVM(anyString(), anyString(),
                anyString(), eq(vCenterSpec));
        ResponseEntity responseEntity = vCenterAPIController.connectNicToVM(VM_NAME, NIC_NAME, vCenterSpec);
    }

    @Test
    public void testSuccessDisconnectNicToVM() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doNothing().when(vmOperationsTaskService).deleteDiskFromVM(anyString(), anyString(), anyString(),
                eq(vCenterSpec));
        ResponseEntity responseEntity = vCenterAPIController.deleteDiskFromVM(VM_NAME, NIC_NAME, vCenterSpec);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorDisconnectNicToVM() throws MangleException {
        when(VCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doThrow(MangleException.class).when(vmOperationsTaskService).disonnectNicFromVM(anyString(), anyString(),
                anyString(), eq(vCenterSpec));
        ResponseEntity responseEntity = vCenterAPIController.disconnectNicFromVM(VM_NAME, NIC_NAME, vCenterSpec);
    }
}
