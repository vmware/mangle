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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.controller.VCenterVMController;
import com.vmware.mangle.mockdata.VCenterSpecMockData;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.model.VM;
import com.vmware.mangle.model.VMDiskRequestBody;
import com.vmware.mangle.model.resource.VCenterTaskTriggeredResponse;
import com.vmware.mangle.service.VCenterClientInstantiationService;
import com.vmware.mangle.service.VCenterOperationsTaskStore;
import com.vmware.mangle.service.VMOperationsTaskService;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 ** @author Chethan C(chetanc)
 *
 */
public class VCenterVMControllerTest {

    public static String VM_NAME = "vm_name";
    public static String DISK_NAME = "disk_name";
    public static String NIC_NAME = "nic_name";
    public static String CLUSTER_NAME = "cluster_name";
    public static String DATACENTER_NAME = "datacenter_name";
    public static String HOST_NAME = "host_name";
    public static String FOLDER_NAME = "folder_name";
    public static String RESOURCE_NAME = "folder_name";

    VCenterVMController vCenterAPIController;

    VCenterSpec vCenterSpec = VCenterSpecMockData.getVCenterSpec();
    @Mock
    private VCenterClient VCenterClient;
    @Mock
    private VMOperationsTaskService vmOperationsTaskService;
    @Mock
    private VCenterClientInstantiationService vCenterClientInstantiationService;
    private VCenterOperationsTaskStore vCenterOperationsTaskStore = new VCenterOperationsTaskStore();

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        vCenterAPIController = new VCenterVMController(vCenterOperationsTaskStore, vmOperationsTaskService);
    }

    @Test
    public void testGetVM() throws MangleException {
        List<VM> vms = VCenterSpecMockData.getVMs();
        when(vmOperationsTaskService.getVMs(HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME, RESOURCE_NAME,
                vCenterSpec)).thenReturn(vms);
        ResponseEntity<List<VM>> responseEntity = vCenterAPIController.getVM(vCenterSpec, DATACENTER_NAME, CLUSTER_NAME,
                HOST_NAME, FOLDER_NAME, RESOURCE_NAME);
        Assert.assertEquals(responseEntity.getBody(), vms);
        verify(vmOperationsTaskService, times(1)).getVMs(HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                RESOURCE_NAME, vCenterSpec);
    }

    @Test
    public void testSuccessShutDownByVMName() throws MangleException {
        doNothing().when(vmOperationsTaskService).powerOffVM(eq(VM_NAME), anyString(), eq(HOST_NAME), eq(CLUSTER_NAME),
                eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity = vCenterAPIController.shutDownVMByName(VM_NAME,
                vCenterSpec, DATACENTER_NAME, CLUSTER_NAME, HOST_NAME, FOLDER_NAME, RESOURCE_NAME);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertNotNull(responseEntity.getBody().getTaskId());

        verify(vmOperationsTaskService, times(1)).powerOffVM(eq(VM_NAME), anyString(), eq(HOST_NAME), eq(CLUSTER_NAME),
                eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));

    }

    @Test
    public void testSuccessShutDownVMById() throws MangleException {
        doNothing().when(vmOperationsTaskService).powerOffVMById(eq(VM_NAME), anyString(), eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity =
                vCenterAPIController.shutDownVMById(VM_NAME, vCenterSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertNotNull(responseEntity.getBody().getTaskId());

        verify(vmOperationsTaskService, times(1)).powerOffVMById(eq(VM_NAME), anyString(), eq(vCenterSpec));

    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorShutDownByVMName() throws MangleException {
        when(vCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doThrow(MangleException.class).when(vmOperationsTaskService).powerOffVM(eq(VM_NAME), anyString(), eq(HOST_NAME),
                eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));
        vCenterAPIController.shutDownVMByName(VM_NAME, vCenterSpec, DATACENTER_NAME, CLUSTER_NAME, HOST_NAME,
                FOLDER_NAME, RESOURCE_NAME);

    }

    @Test
    public void testSuccessTunOnVMByName() throws MangleException {
        doNothing().when(vmOperationsTaskService).powerOnVM(eq(VM_NAME), anyString(), eq(HOST_NAME), eq(CLUSTER_NAME),
                eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity = vCenterAPIController.turnOnVMByName(VM_NAME,
                vCenterSpec, DATACENTER_NAME, CLUSTER_NAME, HOST_NAME, FOLDER_NAME, RESOURCE_NAME);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(vmOperationsTaskService, times(1)).powerOnVM(eq(VM_NAME), anyString(), eq(HOST_NAME), eq(CLUSTER_NAME),
                eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));
    }

    @Test
    public void testSuccessTunOnVMById() throws MangleException {
        doNothing().when(vmOperationsTaskService).powerOnVMById(eq(VM_NAME), anyString(), eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity =
                vCenterAPIController.turnOnVMById(VM_NAME, vCenterSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(vmOperationsTaskService, times(1)).powerOnVMById(eq(VM_NAME), anyString(), eq(vCenterSpec));
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorTunOnVMByName() throws MangleException {
        doThrow(MangleException.class).when(vmOperationsTaskService).powerOnVM(eq(VM_NAME), anyString(), eq(HOST_NAME),
                eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));

        try {
            vCenterAPIController.turnOnVMByName(VM_NAME, vCenterSpec, DATACENTER_NAME, CLUSTER_NAME, HOST_NAME,
                    FOLDER_NAME, RESOURCE_NAME);
        } catch (MangleException e) {
            verify(vmOperationsTaskService, times(1)).powerOnVM(eq(VM_NAME), anyString(), eq(HOST_NAME),
                    eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));
            throw e;
        }
    }

    @Test
    public void testSuccessResetVMByName() throws MangleException {
        doNothing().when(vmOperationsTaskService).resetVM(eq(VM_NAME), anyString(), eq(HOST_NAME), eq(CLUSTER_NAME),
                eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity = vCenterAPIController.resetVMByName(VM_NAME,
                vCenterSpec, DATACENTER_NAME, CLUSTER_NAME, HOST_NAME, FOLDER_NAME, RESOURCE_NAME);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(vmOperationsTaskService, times(1)).resetVM(eq(VM_NAME), anyString(), eq(HOST_NAME), eq(CLUSTER_NAME),
                eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));
    }

    @Test
    public void testSuccessResetVMById() throws MangleException {
        doNothing().when(vmOperationsTaskService).resetVMById(eq(VM_NAME), anyString(), eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity =
                vCenterAPIController.resetVMById(VM_NAME, vCenterSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(vmOperationsTaskService, times(1)).resetVMById(eq(VM_NAME), anyString(), eq(vCenterSpec));
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorResetVMByName() throws MangleException {
        doThrow(MangleException.class).when(vmOperationsTaskService).resetVM(eq(VM_NAME), anyString(), eq(HOST_NAME),
                eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));

        try {
            vCenterAPIController.resetVMByName(VM_NAME, vCenterSpec, DATACENTER_NAME, CLUSTER_NAME, HOST_NAME,
                    FOLDER_NAME, RESOURCE_NAME);
        } catch (MangleException e) {
            verify(vmOperationsTaskService, times(1)).resetVM(eq(VM_NAME), anyString(), eq(HOST_NAME), eq(CLUSTER_NAME),
                    eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));
            throw e;
        }

    }

    @Test
    public void testSuccessSuspendVMByName() throws MangleException {
        doNothing().when(vmOperationsTaskService).suspendVM(eq(VM_NAME), anyString(), eq(HOST_NAME), eq(CLUSTER_NAME),
                eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity = vCenterAPIController.suspendVMByName(VM_NAME,
                vCenterSpec, DATACENTER_NAME, CLUSTER_NAME, HOST_NAME, FOLDER_NAME, RESOURCE_NAME);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(vmOperationsTaskService, times(1)).suspendVM(eq(VM_NAME), anyString(), eq(HOST_NAME), eq(CLUSTER_NAME),
                eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));
    }

    @Test
    public void testSuccessSuspendVMById() throws MangleException {
        doNothing().when(vmOperationsTaskService).suspendVMById(eq(VM_NAME), anyString(), eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity =
                vCenterAPIController.suspendVMById(VM_NAME, vCenterSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(vmOperationsTaskService, times(1)).suspendVMById(eq(VM_NAME), anyString(), eq(vCenterSpec));
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorSuspendVMByName() throws MangleException {
        doThrow(MangleException.class).when(vmOperationsTaskService).suspendVM(eq(VM_NAME), anyString(), eq(HOST_NAME),
                eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));

        vCenterAPIController.suspendVMByName(VM_NAME, vCenterSpec, DATACENTER_NAME, CLUSTER_NAME, HOST_NAME,
                FOLDER_NAME, RESOURCE_NAME);
    }

    @Test
    public void testSuccessConnectDiskToVM() throws MangleException {
        doNothing().when(vmOperationsTaskService).addDiskToVM(eq(VM_NAME), any(), eq(DISK_NAME), anyString(),
                eq(HOST_NAME), eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(null));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity =
                vCenterAPIController.connectDiskToVM(VM_NAME, DISK_NAME, new VMDiskRequestBody(), DATACENTER_NAME,
                        CLUSTER_NAME, HOST_NAME, FOLDER_NAME, RESOURCE_NAME);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(vmOperationsTaskService, times(1)).addDiskToVM(eq(VM_NAME), any(), eq(DISK_NAME), anyString(),
                eq(HOST_NAME), eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(null));
    }

    @Test
    public void testSuccessConnectDiskToVMById() throws MangleException {
        doNothing().when(vmOperationsTaskService).addDiskToVMById(eq(VM_NAME), any(), eq(DISK_NAME), anyString(),
                eq(null));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity =
                vCenterAPIController.connectDiskToVM(VM_NAME, DISK_NAME, new VMDiskRequestBody());

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(vmOperationsTaskService, times(1)).addDiskToVMById(eq(VM_NAME), any(), eq(DISK_NAME), anyString(),
                eq(null));
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorConnectDiskToVM() throws MangleException {
        doThrow(MangleException.class).when(vmOperationsTaskService).addDiskToVM(eq(VM_NAME), any(), eq(DISK_NAME),
                anyString(), eq(HOST_NAME), eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME),
                eq(null));

        try {
            vCenterAPIController.connectDiskToVM(VM_NAME, DISK_NAME, new VMDiskRequestBody(), DATACENTER_NAME,
                    CLUSTER_NAME, HOST_NAME, FOLDER_NAME, RESOURCE_NAME);
        } catch (MangleException e) {
            verify(vmOperationsTaskService, times(1)).addDiskToVM(eq(VM_NAME), any(), eq(DISK_NAME), anyString(),
                    eq(HOST_NAME), eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(null));
            throw e;
        }
    }

    @Test
    public void testSuccessDisconnectDiskToVM() throws MangleException {
        doNothing().when(vmOperationsTaskService).deleteDiskFromVM(eq(VM_NAME), eq(DISK_NAME), anyString(),
                eq(HOST_NAME), eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME),
                eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity = vCenterAPIController.deleteDiskFromVM(VM_NAME,
                DISK_NAME, vCenterSpec, DATACENTER_NAME, CLUSTER_NAME, HOST_NAME, FOLDER_NAME, RESOURCE_NAME);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(vmOperationsTaskService, times(1)).deleteDiskFromVM(eq(VM_NAME), eq(DISK_NAME), anyString(),
                eq(HOST_NAME), eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME),
                eq(vCenterSpec));
    }

    @Test
    public void testSuccessDisconnectDiskToVMById() throws MangleException {
        doNothing().when(vmOperationsTaskService).deleteDiskFromVMById(eq(VM_NAME), eq(DISK_NAME), anyString(),
                eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity =
                vCenterAPIController.deleteDiskFromVM(VM_NAME, DISK_NAME, vCenterSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(vmOperationsTaskService, times(1)).deleteDiskFromVMById(eq(VM_NAME), eq(DISK_NAME), anyString(),
                eq(vCenterSpec));
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorDisconnectDiskToVM() throws MangleException {
        when(vCenterClientInstantiationService.getVCenterClient(anyObject())).thenReturn(VCenterClient);
        doThrow(MangleException.class).when(vmOperationsTaskService).deleteDiskFromVM(eq(VM_NAME), eq(DISK_NAME),
                anyString(), eq(HOST_NAME), eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME),
                eq(vCenterSpec));
        try {
            vCenterAPIController.deleteDiskFromVM(VM_NAME, DISK_NAME, vCenterSpec, DATACENTER_NAME, CLUSTER_NAME,
                    HOST_NAME, FOLDER_NAME, RESOURCE_NAME);
        } catch (MangleException e) {
            verify(vmOperationsTaskService, times(1)).deleteDiskFromVM(eq(VM_NAME), eq(DISK_NAME), anyString(),
                    eq(HOST_NAME), eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME),
                    eq(vCenterSpec));
            throw e;
        }
    }

    @Test
    public void testSuccessConnectNicToVM() throws MangleException {
        doNothing().when(vmOperationsTaskService).connectNicToVM(eq(VM_NAME), eq(NIC_NAME), anyString(), eq(HOST_NAME),
                eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity = vCenterAPIController.connectNicToVM(VM_NAME,
                NIC_NAME, vCenterSpec, DATACENTER_NAME, CLUSTER_NAME, HOST_NAME, FOLDER_NAME, RESOURCE_NAME);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(vmOperationsTaskService, times(1)).connectNicToVM(eq(VM_NAME), eq(NIC_NAME), anyString(), eq(HOST_NAME),
                eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME), eq(vCenterSpec));
    }

    @Test
    public void testSuccessConnectNicToVMById() throws MangleException {
        doNothing().when(vmOperationsTaskService).connectNicToVM(eq(VM_NAME), eq(NIC_NAME), anyString(),
                eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity =
                vCenterAPIController.connectNicToVM(VM_NAME, NIC_NAME, vCenterSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(vmOperationsTaskService, times(1)).connectNicToVM(eq(VM_NAME), eq(NIC_NAME), anyString(),
                eq(vCenterSpec));
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorConnectNicToVM() throws MangleException {
        doThrow(MangleException.class).when(vmOperationsTaskService).connectNicToVM(eq(VM_NAME), eq(NIC_NAME),
                anyString(), eq(HOST_NAME), eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME),
                eq(vCenterSpec));

        try {
            vCenterAPIController.connectNicToVM(VM_NAME, NIC_NAME, vCenterSpec, DATACENTER_NAME, CLUSTER_NAME,
                    HOST_NAME, FOLDER_NAME, RESOURCE_NAME);
        } catch (MangleException e) {
            verify(vmOperationsTaskService, times(1)).connectNicToVM(eq(VM_NAME), eq(NIC_NAME), anyString(),
                    eq(HOST_NAME), eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME),
                    eq(vCenterSpec));
            throw e;
        }
    }

    @Test
    public void testSuccessDisconnectNicToVM() throws MangleException {
        doNothing().when(vmOperationsTaskService).deleteDiskFromVM(eq(VM_NAME), eq(NIC_NAME), anyString(),
                eq(HOST_NAME), eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME),
                eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity = vCenterAPIController.disconnectNicFromVM(VM_NAME,
                NIC_NAME, vCenterSpec, DATACENTER_NAME, CLUSTER_NAME, HOST_NAME, FOLDER_NAME, RESOURCE_NAME);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(vmOperationsTaskService, times(1)).disconnectNicFromVM(eq(VM_NAME), eq(NIC_NAME), anyString(),
                eq(HOST_NAME), eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME),
                eq(vCenterSpec));
    }

    @Test
    public void testSuccessDisconnectNicToVMById() throws MangleException {
        doNothing().when(vmOperationsTaskService).disconnectNicFromVMById(eq(VM_NAME), eq(NIC_NAME), anyString(),
                eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity =
                vCenterAPIController.disconnectNicFromVM(VM_NAME, NIC_NAME, vCenterSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(vmOperationsTaskService, times(1)).disconnectNicFromVMById(eq(VM_NAME), eq(NIC_NAME), anyString(),
                eq(vCenterSpec));
    }

    @Test(expectedExceptions = MangleException.class)
    public void testErrorDisconnectNicToVM() throws MangleException {
        doThrow(MangleException.class).when(vmOperationsTaskService).disconnectNicFromVM(eq(VM_NAME), eq(NIC_NAME),
                anyString(), eq(HOST_NAME), eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME),
                eq(vCenterSpec));
        try {
            vCenterAPIController.disconnectNicFromVM(VM_NAME, NIC_NAME, vCenterSpec, DATACENTER_NAME, CLUSTER_NAME,
                    HOST_NAME, FOLDER_NAME, RESOURCE_NAME);
        } catch (MangleException e) {
            verify(vmOperationsTaskService, times(1)).disconnectNicFromVM(eq(VM_NAME), eq(NIC_NAME), anyString(),
                    eq(HOST_NAME), eq(CLUSTER_NAME), eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(RESOURCE_NAME),
                    eq(vCenterSpec));
            throw e;
        }
    }
}
