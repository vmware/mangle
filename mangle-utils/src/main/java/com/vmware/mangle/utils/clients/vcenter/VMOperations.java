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

package com.vmware.mangle.utils.clients.vcenter;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.model.response.VCenterAdapterResponse;
import com.vmware.mangle.model.response.VCenterOperationTaskQueryResponse;
import com.vmware.mangle.model.vcenter.VCenterSpec;
import com.vmware.mangle.model.vcenter.VMDisk;
import com.vmware.mangle.utils.constants.VCenterConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 *
 *         Provides methods for orchestrating different operations on VM identified by vmName, of a
 *         VCenter identified by the VCenterClient object
 */
public class VMOperations {
    private static ObjectMapper mapper = new ObjectMapper();

    private VMOperations() {
    }

    /**
     * Test if the connection to the vcenter is possible
     *
     * @param client
     * @param vCenterSpec
     * @return true if vcenter is up and running; false otherwise
     * @throws MangleException
     */
    @SuppressWarnings("rawtypes")
    public static boolean testConnection(VCenterAdapterClient client, VCenterSpec vCenterSpec)
            throws MangleException {
        boolean returnValue = false;
        try {
            ResponseEntity responseEntity = client.post(VCenterConstants.TEST_CONNECTION,
                    mapper.writeValueAsString(vCenterSpec), Object.class);
            if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                returnValue = true;
            }
        } catch (IOException e) {
            throw new MangleException(e.getMessage(), ErrorCode.GENERIC_ERROR, "NA");
        }
        return returnValue;
    }

    /**
     * Turns off the VM on the remote VCenter
     *
     * @param client
     * @param vmName
     * @return CommandExecutionResult object with exit code set to 0 if power off is successful,
     *         else sets it to 1
     */
    @SuppressWarnings({ "unchecked", "unused" })
    public static CommandExecutionResult powerOffVM(VCenterClient client, String vmName) {
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        try {
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.POWER_OFF, vmName),
                            mapper.writeValueAsString(client.getVCenterSpec()), VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult
                    .setCommandOutput(String.format("Power off operation on VM %s is successful", vmName));
            commandExecutionResult.setExitCode(0);
        } catch (IOException | MangleException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * Turns off the VM on the remote Vcenter
     *
     * @param client
     * @param vmName
     * @return CommandExecutionResult object with exit code set to 0 if power on is successful, else
     *         sets it to 1
     */
    @SuppressWarnings("unchecked")
    public static CommandExecutionResult powerOnVM(VCenterClient client, String vmName) {
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.POWER_ON, vmName),
                            mapper.writeValueAsString(client.getVCenterSpec()), VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult.setCommandOutput(String.format("Power On operation on VM %s is successful", vmName));
            commandExecutionResult.setExitCode(0);
        } catch (IOException | MangleException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * Suspends VM on the remote VCenter
     *
     * @param client
     * @param vmName
     * @return CommandExecutionResult object with exit code set to 0 if suspend is successful, else
     *         sets it to 1
     */
    @SuppressWarnings("unchecked")
    public static CommandExecutionResult suspendVM(VCenterClient client, String vmName) {
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.SUSPEND_VM, vmName),
                            mapper.writeValueAsString(client.getVCenterSpec()), VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult
                    .setCommandOutput(String.format("Suspend VM operation of VM %s is successful", vmName));
            commandExecutionResult.setExitCode(0);
        } catch (IOException | MangleException e) {
            commandExecutionResult.setCommandOutput(
                    String.format("Terminate of VM %s Failed with the exception: %s", vmName, e.getMessage()));
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * Reboot VM on the remote vCenter
     *
     * @param client
     * @param vmName
     * @return CommandExecutionResult object with exit code set to 0 if restart is successful, else
     *         sets it to 1
     */
    @SuppressWarnings("unchecked")
    public static CommandExecutionResult guestRebootVM(VCenterClient client, String vmName) {
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.RESET_VM, vmName),
                            mapper.writeValueAsString(client.getVCenterSpec()), VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult.setCommandOutput(String.format("Reboot operation of VM %s is successful", vmName));
            commandExecutionResult.setExitCode(0);
        } catch (IOException | MangleException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }

        return commandExecutionResult;
    }

    /**
     * Disconnects NIC from the VM on the remote Vcenter
     *
     * @param client
     * @param vmName
     * @param vmNic
     * @return CommandExecutionResult object with exit code set to 0 if disconnect is successful,
     *         else sets it to 1
     */
    @SuppressWarnings("unchecked")
    public static CommandExecutionResult disconnectNicFromVM(VCenterClient client, String vmName, String vmNic) {
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.DISCONNECT_NIC, vmName, vmNic),
                            mapper.writeValueAsString(client.getVCenterSpec()), VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult.setCommandOutput(
                    String.format("Disconnect Nic operation of NIC %s on VM %s is successful", vmNic, vmName));
            commandExecutionResult.setExitCode(0);
        } catch (IOException | MangleException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * Connects NIC to the VM on the remote VCenter
     *
     * @param client
     * @param vmName
     * @param vmNic
     * @return CommandExecutionResult object with exit code set to 0 if connect NIC is successful,
     *         else sets it to 1
     */
    @SuppressWarnings("unchecked")
    public static CommandExecutionResult addNicFromVM(VCenterClient client, String vmName, String vmNic) {
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.CONNECT_NIC, vmName, vmNic),
                            mapper.writeValueAsString(client.getVCenterSpec()), VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult.setCommandOutput(
                    String.format("Connect Nic operation of NIC %s on VM %s is successful", vmNic, vmName));
            commandExecutionResult.setExitCode(0);
        } catch (IOException | MangleException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * Disconnects Disk from VM on the remote VCenter
     *
     * @param client
     * @param vmName
     * @param vmDiskId
     * @return CommandExecutionResult object with exit code set to 0 if disconnect DISK is
     *         successful, else sets it to 1
     */
    @SuppressWarnings("unchecked")
    public static CommandExecutionResult disconnectDiskFromVM(VCenterClient client, String vmName,
            String vmDiskId) {
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.DISCONNECT_DISK, vmName, vmDiskId),
                            mapper.writeValueAsString(client.getVCenterSpec()), VCenterAdapterResponse.class);
            String result = pollForDiskDisconnect(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult.setCommandOutput(
                    String.format("Disconnect disk operation of disk %s on VM %s is successful with output: %s",
                            vmDiskId, vmName, result));
            commandExecutionResult.setExitCode(0);
        } catch (IOException | MangleException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * Connects the disconnected Disk to the VM on the remote VCenter
     *
     * @param client
     * @param vmName
     * @param vmDiskId
     * @param vmDiskType
     * @param vmDiskBackingType
     * @param vmDiskBackingFile
     * @return CommandExecutionResult object with exit code set to 0 if connect DISK is successful,
     *         else sets it to 1
     * @throws MangleException
     */
    @SuppressWarnings({ "unchecked" })
    public static CommandExecutionResult addDiskFromVM(VCenterClient client, String vmName, String vmDiskId,
            String vmDiskType, String vmDiskBackingType, String vmDiskBackingFile) throws MangleException {

        String request = "{\"vcenterspec\":  %s , \"diskspec\":  %s }";
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();

        VMDisk vmDisk = new VMDisk(vmDiskType, vmDiskBackingType, vmDiskBackingFile.replace("___", " "));
        try {
            String diskSpec = mapper.writeValueAsString(vmDisk);
            String vSpec = mapper.writeValueAsString(client.getVCenterSpec());
            request = String.format(request, vSpec, diskSpec);
        } catch (JsonProcessingException e) {
            throw new MangleException(e.getMessage(), ErrorCode.GENERIC_ERROR, "NA");
        }

        try {
            VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.CONNECT_DISK, vmName, vmDiskId), request,
                            VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult.setCommandOutput(
                    String.format("Connect disk operation of disk %s on VM %s is successful", vmDiskId, vmName));
            commandExecutionResult.setExitCode(0);
        } catch (MangleException e) {
            commandExecutionResult.setCommandOutput(String.format("Connect disk %s on VM %s Failed", vmDiskId, vmName));
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * Polls the VCenter Adapter instance to identify the status of a task, until the status changes
     * from Triggered to any other value
     *
     * @param clientAdapter
     * @param taskId
     * @return Completed/Failed
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    public static String pollForTaskStatusChange(VCenterAdapterClient clientAdapter, String taskId)
            throws MangleException {
        ResponseEntity<VCenterOperationTaskQueryResponse> responeEntity = null;
        do {
            try {
                responeEntity = (ResponseEntity<VCenterOperationTaskQueryResponse>) clientAdapter.get(
                        String.format(VCenterConstants.TASK_STATUS, taskId),
                        VCenterOperationTaskQueryResponse.class);
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MangleException(e.getMessage(), ErrorCode.GENERIC_ERROR);
            }

        } while (responeEntity.getBody().getTaskStatus().equals(VCenterConstants.TASK_STATUS_TRIGGERED));

        if (!responeEntity.getStatusCode().equals(HttpStatus.OK)) {
            throw new MangleException(responeEntity.getBody().getResponseMessage(), ErrorCode.GENERIC_ERROR);
        }

        return responeEntity.getBody().getTaskStatus();
    }

    /**
     * Polls to VCenter Adapter instance to identify the status to identify the status of the VM
     * Disk disconnect
     *
     * @param clientAdapter
     * @param taskId
     * @return Completed/Failed
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    public static String pollForDiskDisconnect(VCenterAdapterClient clientAdapter, String taskId)
            throws MangleException {
        ResponseEntity<VCenterOperationTaskQueryResponse> responeEntity = null;
        do {
            try {
                responeEntity = (ResponseEntity<VCenterOperationTaskQueryResponse>) clientAdapter.get(
                        String.format(VCenterConstants.TASK_STATUS, taskId),
                        VCenterOperationTaskQueryResponse.class);
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MangleException(e.getMessage(), ErrorCode.GENERIC_ERROR);
            }

        } while (responeEntity.getBody().getTaskStatus().equals(VCenterConstants.TASK_STATUS_TRIGGERED));

        if (!responeEntity.getStatusCode().equals(HttpStatus.OK)) {
            throw new MangleException(responeEntity.getBody().getResponseMessage(), ErrorCode.GENERIC_ERROR, "NA");
        }

        VMDisk vmDisk = mapper.convertValue(responeEntity.getBody().getIfiaascoVCenterVMObject(), VMDisk.class);
        return vmDisk.getDiskInfo();
    }
}
