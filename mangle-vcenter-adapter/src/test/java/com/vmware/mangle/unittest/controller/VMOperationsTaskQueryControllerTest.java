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
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.adapter.VMOperations;
import com.vmware.mangle.controller.VMOperationsTaskQueryController;
import com.vmware.mangle.inventory.helpers.VMInventoryHelper;
import com.vmware.mangle.mockdata.VCenterSpecMockData;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.service.VCenterClientInstantiationService;
import com.vmware.mangle.service.VCenterOperationsTaskStore;
import com.vmware.mangle.service.VMOperationsTaskService;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author Chethan C(chetanc)
 */
public class VMOperationsTaskQueryControllerTest {

    public static String VM_NAME = "vm_name";
    public static String VM_ID = "vmId";
    private static String taskId;

    VMOperationsTaskService vmOperationsTaskService;
    VMOperationsTaskQueryController vmOperationsTaskQueryController;

    @Mock
    VCenterClient vCenterClient;
    @Mock
    VCenterClientInstantiationService vcClientInstantiationService;
    @Mock
    VMOperations vmOperations;
    @Mock
    VMInventoryHelper vmInventoryHelper;
    @Mock
    VCenterOperationsTaskStore vCenterOperationsTaskStore;

    VCenterSpec vCenterSpec = VCenterSpecMockData.getVCenterSpec();

    @BeforeMethod
    public void initMock() throws MangleException {
        MockitoAnnotations.initMocks(this);
        vmOperationsTaskService = new VMOperationsTaskService(vcClientInstantiationService, vmInventoryHelper,
                vCenterOperationsTaskStore, vmOperations);
        vmOperationsTaskQueryController = new VMOperationsTaskQueryController(vCenterOperationsTaskStore);
        taskId = UUID.randomUUID().toString();
        when(vcClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
    }

    @Test
    public void testSuccessQueryTaskDetails() throws MangleException {
        when(vmOperations.powerOffVM(anyObject(), anyString())).thenReturn(true);
        when(vmOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(false);
        when(vmInventoryHelper.getVMId(any(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString())).thenReturn(VM_ID);
        vmOperationsTaskService.powerOffVM(VM_NAME, taskId, null, null, null, null, null, vCenterSpec);
        ResponseEntity responseEntity = vmOperationsTaskQueryController.queryTaskDetails(taskId);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testFailedQueryTaskDetails() throws MangleException {
        when(vmOperations.powerOffVM(anyObject(), anyString())).thenReturn(false);
        when(vmOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(false);
        when(vmInventoryHelper.getVMId(any(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString())).thenReturn(VM_ID);
        vmOperationsTaskService.powerOffVM(VM_NAME, taskId, null, null, null, null, null, vCenterSpec);
        ResponseEntity responseEntity = vmOperationsTaskQueryController.queryTaskDetails(UUID.randomUUID().toString());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testFailedQueryTaskDetails2() throws MangleException {
        when(vmOperations.powerOffVM(anyObject(), anyString())).thenThrow(new MangleException());
        when(vmOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(false);
        when(vmInventoryHelper.getVMId(any(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString())).thenReturn(VM_ID);
        vmOperationsTaskService.powerOffVM(VM_NAME, taskId, null, null, null, null, null, vCenterSpec);
        ResponseEntity responseEntity = vmOperationsTaskQueryController.queryTaskDetails(taskId);
    }

}
