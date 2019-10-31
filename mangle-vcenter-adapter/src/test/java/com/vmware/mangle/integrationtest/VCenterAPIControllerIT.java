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

package com.vmware.mangle.integrationtest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.integrationtest.mockdata.IntegrationVCenterSpecMockData;
import com.vmware.mangle.integrationtest.mockdata.MangleClient;
import com.vmware.mangle.mockdata.VCenterAdapterTestConstants;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.model.response.VCenterOperationTask;
import com.vmware.mangle.model.response.VCenterTaskTriggeredResponse;
import com.vmware.mangle.utils.status.VMOperationsTaskStatusEnum;

/**
 * @author chetanc
 */
public class VCenterAPIControllerIT extends AbstractTestNGSpringContextTests {
    MangleClient mangleClient;
    IntegrationVCenterSpecMockData mockData = new IntegrationVCenterSpecMockData();

    @BeforeClass
    @AfterClass
    public void powerOnVM() {
        mangleClient = new MangleClient(mockData.getVcAdapterHost(), mockData.getVcAdapterPort(),
                mockData.getVcAdapterUsername(), mockData.getVcAdapterPassword(), MediaType.APPLICATION_JSON);
        VCenterSpec entity = mockData.getVCenterSpecForIntegration();
        String vmName = mockData.getVMName();

        ResponseEntity<VCenterTaskTriggeredResponse> response =
                (ResponseEntity<VCenterTaskTriggeredResponse>) mangleClient.post(
                        String.format(VCenterAdapterTestConstants.POWER_ON, vmName), mangleClient.objectToJson(entity),
                        VCenterTaskTriggeredResponse.class);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        VCenterTaskTriggeredResponse task = response.getBody();
        String taskId = task.getTaskId();
        logger.info(retrieveStatusForTask(taskId));
    }

    @Test(priority = 0)
    public void VCenterTestConnectionTest() {
        VCenterSpec entity = mockData.getVCenterSpecForIntegration();
        String vmName = mockData.getVMName();
        ResponseEntity<String> response = (ResponseEntity<String>) mangleClient.post(
                String.format(VCenterAdapterTestConstants.TEST_CONNECTION, vmName), mangleClient.objectToJson(entity),
                String.class);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test(priority = 1)
    public void testVCenterResetVM() {
        VCenterSpec entity = mockData.getVCenterSpecForIntegration();
        String vmName = mockData.getVMName();
        ResponseEntity<VCenterTaskTriggeredResponse> response =
                (ResponseEntity<VCenterTaskTriggeredResponse>) mangleClient.post(
                        String.format(VCenterAdapterTestConstants.REBOOT_VM, vmName), mangleClient.objectToJson(entity),
                        VCenterTaskTriggeredResponse.class);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        VCenterTaskTriggeredResponse task = response.getBody();
        String taskId = task.getTaskId();
        VCenterOperationTask taskResponse = retrieveStatusForTask(taskId);
        Assert.assertEquals(taskResponse.getTaskStatus(), VMOperationsTaskStatusEnum.COMPLETED.toString(),
                taskResponse.getResponseMessage());
    }

    @Test(priority = 2)
    public void testVCenterPowerOffVM() {
        VCenterSpec entity = mockData.getVCenterSpecForIntegration();
        String vmName = mockData.getVMName();
        ResponseEntity<VCenterTaskTriggeredResponse> response =
                (ResponseEntity<VCenterTaskTriggeredResponse>) mangleClient.post(
                        String.format(VCenterAdapterTestConstants.POWER_OFF, vmName), mangleClient.objectToJson(entity),
                        VCenterTaskTriggeredResponse.class);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        VCenterTaskTriggeredResponse task = response.getBody();
        String taskId = task.getTaskId();
        VCenterOperationTask taskResponse = retrieveStatusForTask(taskId);
        Assert.assertEquals(taskResponse.getTaskStatus(), VMOperationsTaskStatusEnum.COMPLETED.toString(),
                taskResponse.getResponseMessage());
    }

    @Test(priority = 3)
    public void testVCenterPowerONVM() {
        VCenterSpec entity = mockData.getVCenterSpecForIntegration();
        String vmName = mockData.getVMName();
        ResponseEntity<VCenterTaskTriggeredResponse> response =
                (ResponseEntity<VCenterTaskTriggeredResponse>) mangleClient.post(
                        String.format(VCenterAdapterTestConstants.POWER_ON, vmName), mangleClient.objectToJson(entity),
                        VCenterTaskTriggeredResponse.class);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        VCenterTaskTriggeredResponse task = response.getBody();
        String taskId = task.getTaskId();
        VCenterOperationTask taskResponse = retrieveStatusForTask(taskId);
        Assert.assertEquals(taskResponse.getTaskStatus(), VMOperationsTaskStatusEnum.COMPLETED.toString(),
                taskResponse.getResponseMessage());
    }

    @Test(priority = 4)
    public void testVCenterDisconnectNICVMFailWrongNICId() {
        VCenterSpec entity = mockData.getVCenterSpecForIntegration();
        String nicId = UUID.randomUUID().toString();
        String vmName = mockData.getVMName();
        ResponseEntity<VCenterTaskTriggeredResponse> response =
                (ResponseEntity<VCenterTaskTriggeredResponse>) mangleClient.post(
                        String.format(VCenterAdapterTestConstants.DISCONNECT_NIC, vmName, nicId),
                        mangleClient.objectToJson(entity), VCenterTaskTriggeredResponse.class);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        VCenterTaskTriggeredResponse task = response.getBody();
        String taskId = task.getTaskId();
        VCenterOperationTask taskResponse = retrieveStatusForTask(taskId);
        Assert.assertEquals(taskResponse.getTaskStatus(), VMOperationsTaskStatusEnum.FAILED.toString());
        Assert.assertTrue(taskResponse.getResponseMessage().startsWith(String
                .format("Device with identifier '%s' does not exist on the virtual machine with identifier", nicId)));
    }

    @Test(priority = 5)
    public void testVCenterDisconnectNICVM() {
        VCenterSpec entity = mockData.getVCenterSpecForIntegration();
        String vmName = mockData.getVMName();
        String nicId = mockData.getVMNicId();
        ResponseEntity<VCenterTaskTriggeredResponse> response =
                (ResponseEntity<VCenterTaskTriggeredResponse>) mangleClient.post(
                        String.format(VCenterAdapterTestConstants.DISCONNECT_NIC, vmName, nicId),
                        mangleClient.objectToJson(entity), VCenterTaskTriggeredResponse.class);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        VCenterTaskTriggeredResponse task = response.getBody();
        String taskId = task.getTaskId();
        VCenterOperationTask taskResponse = retrieveStatusForTask(taskId);
        Assert.assertEquals(taskResponse.getTaskStatus(), VMOperationsTaskStatusEnum.COMPLETED.toString(),
                taskResponse.getResponseMessage());
    }

    @Test(priority = 6)
    public void testVCenterConnectNICVM() {
        VCenterSpec entity = mockData.getVCenterSpecForIntegration();
        String vmName = mockData.getVMName();
        String nicId = mockData.getVMNicId();
        ResponseEntity<VCenterTaskTriggeredResponse> response =
                (ResponseEntity<VCenterTaskTriggeredResponse>) mangleClient.post(
                        String.format(VCenterAdapterTestConstants.CONNECT_NIC, vmName, nicId),
                        mangleClient.objectToJson(entity), VCenterTaskTriggeredResponse.class);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        VCenterTaskTriggeredResponse task = response.getBody();
        String taskId = task.getTaskId();
        VCenterOperationTask taskResponse = retrieveStatusForTask(taskId);
        Assert.assertEquals(taskResponse.getTaskStatus(), VMOperationsTaskStatusEnum.COMPLETED.toString(),
                taskResponse.getResponseMessage());
    }

    @Test(priority = 7)
    public void testVCenterSuspendVM() {
        VCenterSpec entity = mockData.getVCenterSpecForIntegration();
        String vmName = mockData.getVMName();
        ResponseEntity<VCenterTaskTriggeredResponse> response =
                (ResponseEntity<VCenterTaskTriggeredResponse>) mangleClient.post(
                        String.format(VCenterAdapterTestConstants.SUSPEND_VM, vmName),
                        mangleClient.objectToJson(entity), VCenterTaskTriggeredResponse.class);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        VCenterTaskTriggeredResponse task = response.getBody();
        String taskId = task.getTaskId();
        VCenterOperationTask taskResponse = retrieveStatusForTask(taskId);
        Assert.assertEquals(taskResponse.getTaskStatus(), VMOperationsTaskStatusEnum.COMPLETED.toString(),
                taskResponse.getResponseMessage());
    }

    @Test(priority = 8)
    public void testVCenterPowerOffVMFailWrongVMName() {
        VCenterSpec entity = mockData.getVCenterSpecForIntegration();
        String vmName = UUID.randomUUID().toString();
        ResponseEntity<VCenterTaskTriggeredResponse> response =
                (ResponseEntity<VCenterTaskTriggeredResponse>) mangleClient.post(
                        String.format(VCenterAdapterTestConstants.POWER_OFF, vmName), mangleClient.objectToJson(entity),
                        VCenterTaskTriggeredResponse.class);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        VCenterTaskTriggeredResponse task = response.getBody();
        String taskId = task.getTaskId();
        VCenterOperationTask taskResponse = retrieveStatusForTask(taskId);
        Assert.assertEquals(taskResponse.getTaskStatus(), VMOperationsTaskStatusEnum.FAILED.toString());
        Assert.assertEquals(taskResponse.getResponseMessage(), String.format("VM with name %s not found", vmName));
    }

    @Test(priority = 9)
    public void powerOnVMFailWrongVMName() {
        VCenterSpec entity = mockData.getVCenterSpecForIntegration();
        String vmName = UUID.randomUUID().toString();
        ResponseEntity<VCenterTaskTriggeredResponse> response =
                (ResponseEntity<VCenterTaskTriggeredResponse>) mangleClient.post(
                        String.format(VCenterAdapterTestConstants.POWER_ON, vmName), mangleClient.objectToJson(entity),
                        VCenterTaskTriggeredResponse.class);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        VCenterTaskTriggeredResponse task = response.getBody();
        String taskId = task.getTaskId();
        VCenterOperationTask taskResponse = retrieveStatusForTask(taskId);
        Assert.assertEquals(taskResponse.getTaskStatus(), VMOperationsTaskStatusEnum.FAILED.toString());
        Assert.assertEquals(taskResponse.getResponseMessage(), String.format("VM with name %s not found", vmName));
    }

    @Test(priority = 10)
    public void testVCenterDisconnectNICVMFailVMPoweredOff() {
        VCenterSpec entity = mockData.getVCenterSpecForIntegration();
        String nicId = UUID.randomUUID().toString();
        String vmName = mockData.getVMName();
        ResponseEntity<VCenterTaskTriggeredResponse> response =
                (ResponseEntity<VCenterTaskTriggeredResponse>) mangleClient.post(
                        String.format(VCenterAdapterTestConstants.DISCONNECT_NIC, vmName, nicId),
                        mangleClient.objectToJson(entity), VCenterTaskTriggeredResponse.class);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        VCenterTaskTriggeredResponse task = response.getBody();
        String taskId = task.getTaskId();
        VCenterOperationTask taskResponse = retrieveStatusForTask(taskId);
        Assert.assertEquals(taskResponse.getTaskStatus(), VMOperationsTaskStatusEnum.FAILED.toString());
        Assert.assertTrue(taskResponse.getResponseMessage()
                .startsWith(String.format(
                        "Device with identifier '%s' cannot be disconnected unless the virtual machine with identifier",
                        nicId, vmName)));
    }

    private VCenterOperationTask retrieveStatusForTask(String taskId) {
        /*
         * Retrieve for task poll for the data continuously, until the status change
         */
        String triggered = VMOperationsTaskStatusEnum.TRIGGERED.toString();
        String taskStatus = triggered;
        ResponseEntity<VCenterOperationTask> response = null;
        while (taskStatus.equals(triggered)) {
            response = (ResponseEntity<VCenterOperationTask>) mangleClient
                    .get(String.format(VCenterAdapterTestConstants.TASK_QUERY, taskId), VCenterOperationTask.class);
            taskStatus = response.getBody().getTaskStatus();
        }
        return response.getBody();
    }
}
