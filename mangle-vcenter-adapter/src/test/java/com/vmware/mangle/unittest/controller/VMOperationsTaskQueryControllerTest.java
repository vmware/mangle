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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
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
import com.vmware.mangle.adapter.VMOperations;
import com.vmware.mangle.controller.VMOperationsTaskQueryController;
import com.vmware.mangle.mockdata.VCenterSpecMockData;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.service.VCenterClientInstantiationService;
import com.vmware.mangle.service.VMOperationsTaskService;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author Chethan C(chetanc)
 */
@PrepareForTest(VMOperations.class)
public class VMOperationsTaskQueryControllerTest extends PowerMockTestCase {

    public static String VM_NAME = "vm_name";
    private static String taskId;
    @InjectMocks
    VMOperationsTaskService vmOperationsTaskService;
    @InjectMocks
    VMOperationsTaskQueryController vmOperationsTaskQueryController;
    @Mock
    VCenterClient vCenterClient;
    @Mock
    VCenterClientInstantiationService vCenterClientInstantiationService;

    VCenterSpec vCenterSpec = VCenterSpecMockData.getVCenterSpec();

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeMethod
    public void initMock() throws MangleException {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(VMOperations.class);
        taskId = UUID.randomUUID().toString();
        when(vCenterClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
    }

    @Test
    public void testSuccessQueryTaskDetails() throws MangleException {
        when(VMOperations.powerOffVM(anyObject(), anyString(), eq(null))).thenReturn(true);
        when(VMOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(false);
        vmOperationsTaskService.powerOffVM(VM_NAME, taskId, null, vCenterSpec);
        ResponseEntity responseEntity = vmOperationsTaskQueryController.queryTaskDetails(taskId);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testFailedQueryTaskDetails() throws MangleException {
        when(VMOperations.powerOffVM(anyObject(), anyString(), eq(null))).thenReturn(true);
        when(VMOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(false);
        vmOperationsTaskService.powerOffVM(VM_NAME, taskId, null, vCenterSpec);
        ResponseEntity responseEntity = vmOperationsTaskQueryController.queryTaskDetails(UUID.randomUUID().toString());
        Assert.assertNotEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testFailedQueryTaskDetails2() throws MangleException {
        when(VMOperations.powerOffVM(anyObject(), anyString(), eq(null))).thenThrow(new MangleException());
        when(VMOperations.isVMPoweredOn(anyObject(), anyString())).thenReturn(false);
        vmOperationsTaskService.powerOffVM(VM_NAME, taskId, null, vCenterSpec);
        ResponseEntity responseEntity = vmOperationsTaskQueryController.queryTaskDetails(taskId);
    }

}
