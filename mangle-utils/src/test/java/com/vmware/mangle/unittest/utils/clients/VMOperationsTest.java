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

package com.vmware.mangle.unittest.utils.clients;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.model.response.VCenterAdapterResponse;
import com.vmware.mangle.model.response.VCenterOperationTaskQueryResponse;
import com.vmware.mangle.model.vcenter.VCenterSpec;
import com.vmware.mangle.model.vcenter.VMDiskObj;
import com.vmware.mangle.utils.clients.vcenter.VCenterAdapterClient;
import com.vmware.mangle.utils.clients.vcenter.VCenterClient;
import com.vmware.mangle.utils.clients.vcenter.VMOperations;
import com.vmware.mangle.utils.constants.VCenterConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.mockdata.VMOperationsMockData;

/**
 *
 *
 * @author chetanc
 */
public class VMOperationsTest extends PowerMockTestCase {
    @Mock
    private VCenterAdapterClient vCenterAdapterClient;

    @Mock
    private VCenterClient vCenterClient;
    private VMOperationsMockData vmOperationsMockData = new VMOperationsMockData();
    private String VM_NAME = "vmName";
    private String VM_ID = "vmID";

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConnectionTestSuccessful() throws MangleException {
        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.OK);
        VCenterSpec vCenterSpec = new VCenterSpec("", "", "");
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        boolean result = VMOperations.testConnection(vCenterAdapterClient, vCenterSpec);
        Assert.assertTrue(result);

    }

    @Test
    public void testConnectionTestFailure() throws MangleException {
        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.NO_CONTENT);
        VCenterSpec vCenterSpec = new VCenterSpec("", "", "");
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        boolean result = VMOperations.testConnection(vCenterAdapterClient, vCenterSpec);
        Assert.assertFalse(result);

    }

    @Test(expectedExceptions = MangleException.class)
    public void testConnectionTestFailureInternalServerError() throws MangleException {
        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        VCenterSpec vCenterSpec = new VCenterSpec("", "", "");
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        VMOperations.testConnection(vCenterAdapterClient, vCenterSpec);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testConnectionTestFailureIOException() throws MangleException {
        VCenterSpec vCenterSpec = new VCenterSpec("", "", "");
        doThrow(IOException.class).when(vCenterAdapterClient).post(anyString(), anyString(), eq(Object.class));
        try {
            VMOperations.testConnection(vCenterAdapterClient, vCenterSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.GENERIC_ERROR);
            verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(Object.class));
            throw e;
        }
    }

    @Test
    public void testGetVMsListRandomFalse() throws MangleException {
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(vmOperationsMockData.getVMList(), HttpStatus.OK);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);

        List<String> vms = VMOperations.getVMsList(vCenterClient, false, new HashMap<>());

        Assert.assertEquals(vms.size(), 2);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(Object.class));
        verify(vCenterClient, times(1)).getVCenterAdapterClient();
    }

    @Test
    public void testGetVMsListRandomTrue() throws MangleException {
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(vmOperationsMockData.getVMList(), HttpStatus.OK);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);

        List<String> vms = VMOperations.getVMsList(vCenterClient, true, new HashMap<>());

        Assert.assertEquals(vms.size(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(Object.class));
        verify(vCenterClient, times(1)).getVCenterAdapterClient();
    }

    @Test
    public void testGetVMsListByNameRandomFalse() throws MangleException {
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(vmOperationsMockData.getVMList(), HttpStatus.OK);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);

        List<String> vms = VMOperations.getVMsList(vCenterClient, VM_NAME, vmOperationsMockData.getFilters());

        Assert.assertEquals(vms.size(), 2);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(Object.class));
        verify(vCenterClient, times(1)).getVCenterAdapterClient();
    }

    @Test
    public void testGetVMNicListRandomFalse() throws MangleException {
        ResponseEntity<Object> responseEntity =
                new ResponseEntity<>(vmOperationsMockData.getVMNicList(), HttpStatus.OK);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);

        List<String> vmNics = VMOperations.getVMNicList(vCenterClient, VM_ID, false);

        Assert.assertEquals(vmNics.size(), 2);
        verify(vCenterAdapterClient, times(1)).testConnection();
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(Object.class));
        verify(vCenterClient, times(1)).getVCenterAdapterClient();
    }

    @Test
    public void testGetVMNicListRandomTrue() throws MangleException {
        ResponseEntity<Object> responseEntity =
                new ResponseEntity<>(vmOperationsMockData.getVMNicList(), HttpStatus.OK);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);

        List<String> vmNics = VMOperations.getVMNicList(vCenterClient, VM_ID, true);

        Assert.assertEquals(vmNics.size(), 1);
        verify(vCenterAdapterClient, times(1)).testConnection();
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(Object.class));
        verify(vCenterClient, times(1)).getVCenterAdapterClient();
    }

    @Test
    public void testGetVMDiskListRandomFalse() throws MangleException {
        ResponseEntity<Object> responseEntity =
                new ResponseEntity<>(vmOperationsMockData.getVMDiskList(), HttpStatus.OK);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);

        List<String> vmDisks = VMOperations.getVMDiskList(vCenterClient, VM_ID, false);

        Assert.assertEquals(vmDisks.size(), 2);
        verify(vCenterAdapterClient, times(1)).testConnection();
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(Object.class));
        verify(vCenterClient, times(1)).getVCenterAdapterClient();
    }

    @Test
    public void testGetVMDiskListRandomTrue() throws MangleException {
        ResponseEntity<Object> responseEntity =
                new ResponseEntity<>(vmOperationsMockData.getVMDiskList(), HttpStatus.OK);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);

        List<String> vmDisks = VMOperations.getVMDiskList(vCenterClient, VM_ID, true);

        Assert.assertEquals(vmDisks.size(), 1);
        verify(vCenterAdapterClient, times(1)).testConnection();
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(Object.class));
        verify(vCenterClient, times(1)).getVCenterAdapterClient();
    }

    @Test(expectedExceptions = MangleException.class)
    public void testGetVMsListFailure() throws MangleException {

        ResponseEntity<Object> responseEntity =
                new ResponseEntity<>(vmOperationsMockData.getErrorMockDetails(), HttpStatus.INTERNAL_SERVER_ERROR);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        try {
            VMOperations.getVMsList(vCenterClient, false, new HashMap<>());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.VCENTER_VM_NOT_FOUND);
            throw e;
        }
    }

    @Test(expectedExceptions = MangleException.class)
    public void testGetVMsListFailureVMNotFound() throws MangleException {

        ResponseEntity<Object> responseEntity = null;
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        try {
            VMOperations.getVMsList(vCenterClient, false, new HashMap<>());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.VCENTER_VM_NOT_FOUND);
            throw e;
        }
    }

    @Test(expectedExceptions = MangleException.class)
    public void testGetVMsListByNameFailure() throws MangleException {

        ResponseEntity<Object> responseEntity =
                new ResponseEntity<>(vmOperationsMockData.getErrorMockDetails(), HttpStatus.INTERNAL_SERVER_ERROR);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        try {
            VMOperations.getVMsList(vCenterClient, VM_NAME, vmOperationsMockData.getFilters());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.VCENTER_VM_NOT_FOUND);
            throw e;
        }
    }

    @Test(expectedExceptions = MangleException.class)
    public void testGetVMsListFailureByNameVMNotFound() throws MangleException {

        ResponseEntity<Object> responseEntity = null;
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        try {
            VMOperations.getVMsList(vCenterClient, VM_NAME, vmOperationsMockData.getFilters());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.VCENTER_VM_NOT_FOUND);
            throw e;
        }
    }

    @Test(expectedExceptions = MangleException.class)
    public void testGetVMNicsListFailure() throws MangleException {

        ResponseEntity<Object> responseEntity =
                new ResponseEntity<>(vmOperationsMockData.getErrorMockDetails(), HttpStatus.INTERNAL_SERVER_ERROR);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        try {
            VMOperations.getVMNicList(vCenterClient, VM_ID, true);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.VCENTER_VM_NIC_NOT_FOUND);
            throw e;
        }
    }

    @Test(expectedExceptions = MangleException.class)
    public void testGetVMDiskListFailureVMNotFound() throws MangleException {

        ResponseEntity<Object> responseEntity =
                new ResponseEntity<>(vmOperationsMockData.getErrorMockDetails(), HttpStatus.INTERNAL_SERVER_ERROR);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(Object.class))).thenReturn(responseEntity);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        try {
            VMOperations.getVMDiskList(vCenterClient, VM_ID, true);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.VCENTER_VM_DISK_NOT_FOUND);
            throw e;
        }
    }


    @Test
    public void powerOffVMTest() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_COMPLETED, "", null);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.OK);

        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.powerOffVM(vCenterClient, "VM_NAME");
        Assert.assertEquals(result.getExitCode(), 0);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void powerOffVMTestFailure() throws MangleException {
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        doThrow(IOException.class).when(vCenterAdapterClient).post(anyString(), anyString(),
                eq(VCenterAdapterResponse.class));

        CommandExecutionResult result = VMOperations.powerOffVM(vCenterClient, "VM_NAME");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(0)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void powerOffVMTestFailureTaskFail() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_FAILED, "", null);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.BAD_REQUEST);

        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.powerOffVM(vCenterClient, "VM_NAME");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void powerOnVMTest() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_COMPLETED, "", null);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.OK);

        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.powerOnVM(vCenterClient, "VM_NAME");
        Assert.assertEquals(result.getExitCode(), 0);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void powerOnVMTestFailure() throws MangleException {
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        doThrow(IOException.class).when(vCenterAdapterClient).post(anyString(), anyString(),
                eq(VCenterAdapterResponse.class));
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        CommandExecutionResult result = VMOperations.powerOnVM(vCenterClient, "VM_NAME");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(0)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void powerOnVMTestFailureTaskFail() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_FAILED, "", null);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.BAD_REQUEST);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.powerOnVM(vCenterClient, "VM_NAME");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void suspendVMTest() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_COMPLETED, "", null);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.OK);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.suspendVM(vCenterClient, "VM_NAME");
        Assert.assertEquals(result.getExitCode(), 0);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void suspendVMTestFailure() throws MangleException {
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        doThrow(IOException.class).when(vCenterAdapterClient).post(anyString(), anyString(),
                eq(VCenterAdapterResponse.class));
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        CommandExecutionResult result = VMOperations.suspendVM(vCenterClient, "VM_NAME");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(0)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void suspendVMTestFailureTaskFail() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_FAILED, "", null);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.BAD_REQUEST);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.suspendVM(vCenterClient, "VM_NAME");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void guestRebootVMTest() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_COMPLETED, "", null);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.OK);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.resetVM(vCenterClient, "VM_NAME");
        Assert.assertEquals(result.getExitCode(), 0);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void guestRebootVMTestFailure() throws MangleException {
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        doThrow(IOException.class).when(vCenterAdapterClient).post(anyString(), anyString(),
                eq(VCenterAdapterResponse.class));
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        CommandExecutionResult result = VMOperations.resetVM(vCenterClient, "VM_NAME");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(0)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void guestRebootVMTestFailureTaskFail() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_FAILED, "", null);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.BAD_REQUEST);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.resetVM(vCenterClient, "VM_NAME");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void disconnectNicFromVMTest() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_COMPLETED, "", null);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.OK);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.disconnectNicFromVM(vCenterClient, "VM_NAME", "NIC_ID");
        Assert.assertEquals(result.getExitCode(), 0);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void disconnectNicFromVMTestFailure() throws MangleException {
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        doThrow(IOException.class).when(vCenterAdapterClient).post(anyString(), anyString(),
                eq(VCenterAdapterResponse.class));
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        CommandExecutionResult result = VMOperations.disconnectNicFromVM(vCenterClient, "VM_NAME", "NIC_ID");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(0)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void disconnectNicFromVMTestFailureTaskFail() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_FAILED, "", null);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.BAD_REQUEST);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.disconnectNicFromVM(vCenterClient, "VM_NAME", "NIC_ID");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void addNicFromVMTest() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_COMPLETED, "", null);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.OK);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.addNicFromVM(vCenterClient, "VM_NAME", "NIC_ID");
        Assert.assertEquals(result.getExitCode(), 0);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void addNicFromVMTestFailure() throws MangleException {
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        doThrow(IOException.class).when(vCenterAdapterClient).post(anyString(), anyString(),
                eq(VCenterAdapterResponse.class));
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        CommandExecutionResult result = VMOperations.addNicFromVM(vCenterClient, "VM_NAME", "NIC_ID");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(0)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void addNicFromVMTestFailureTaskFail() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_FAILED, "", null);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.BAD_REQUEST);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.addNicFromVM(vCenterClient, "VM_NAME", "NIC_ID");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void disconnectDiskFromVMTest() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VMDiskObj vmDiskObj = new VMDiskObj();
        VMDiskObj.Backing backing = vmDiskObj.new Backing();
        backing.setVmdk_file("VMDK_FILE");
        backing.setType("VMDK");
        vmDiskObj.setBacking(backing);
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_COMPLETED, "", vmDiskObj);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.OK);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.disconnectDiskFromVM(vCenterClient, "VM_NAME", "DISK_ID");
        Assert.assertEquals(result.getExitCode(), 0);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void disconnectDiskFromVMTestFailure() throws MangleException {
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        doThrow(IOException.class).when(vCenterAdapterClient).post(anyString(), anyString(),
                eq(VCenterAdapterResponse.class));
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        CommandExecutionResult result = VMOperations.disconnectDiskFromVM(vCenterClient, "VM_NAME", "DISK_ID");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(0)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void disconnectDiskFromVMTestFailureTaskFail() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VMDiskObj vmDiskObj = new VMDiskObj();
        VMDiskObj.Backing backing = vmDiskObj.new Backing();
        backing.setVmdk_file("VMDK_FILE");
        backing.setType("VMDK");
        vmDiskObj.setBacking(backing);
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_FAILED, "", vmDiskObj);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.BAD_REQUEST);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.disconnectDiskFromVM(vCenterClient, "VM_NAME", "DISK_ID");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }


    @Test
    public void addDiskFromVMTest() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_COMPLETED, "", null);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.OK);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.addDiskFromVM(vCenterClient, "VM_NAME", "DISK_ID", "", "", "");
        Assert.assertEquals(result.getExitCode(), 0);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }

    @Test
    public void addDiskFromVMTestFailureTaskFail() throws MangleException {
        VCenterAdapterResponse vCenterAdapterResponse = new VCenterAdapterResponse();
        VCenterOperationTaskQueryResponse taskQueryResponse = new VCenterOperationTaskQueryResponse(
                UUID.randomUUID().toString(), VCenterConstants.TASK_STATUS_FAILED, "", null);
        ResponseEntity responseEntity = new ResponseEntity(vCenterAdapterResponse, HttpStatus.OK);
        ResponseEntity pollResponse = new ResponseEntity(taskQueryResponse, HttpStatus.BAD_REQUEST);
        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        when(vCenterAdapterClient.post(anyString(), anyString(), eq(VCenterAdapterResponse.class)))
                .thenReturn(responseEntity);
        when(vCenterAdapterClient.get(anyString(), eq(VCenterOperationTaskQueryResponse.class)))
                .thenReturn(pollResponse);

        CommandExecutionResult result = VMOperations.addDiskFromVM(vCenterClient, "VM_NAME", "DISK_ID", "", "", "");
        Assert.assertEquals(result.getExitCode(), 1);
        verify(vCenterAdapterClient, times(1)).post(anyString(), anyString(), eq(VCenterAdapterResponse.class));
        verify(vCenterAdapterClient, times(1)).get(anyString(), eq(VCenterOperationTaskQueryResponse.class));
    }
}
