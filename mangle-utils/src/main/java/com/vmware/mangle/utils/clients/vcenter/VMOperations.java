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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.model.response.VCenterAdapterResponse;
import com.vmware.mangle.model.response.VCenterOperationTaskQueryResponse;
import com.vmware.mangle.model.vcenter.VCenterAdapterErrorObj;
import com.vmware.mangle.model.vcenter.VCenterSpec;
import com.vmware.mangle.model.vcenter.VM;
import com.vmware.mangle.model.vcenter.VMDisk;
import com.vmware.mangle.model.vcenter.VMDiskObj;
import com.vmware.mangle.model.vcenter.VMNic;
import com.vmware.mangle.utils.InventoryHelperUtil;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.VCenterConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 *
 *         Provides methods for orchestrating different operations on VM identified by vmName, of a
 *         VCenter identified by the VCenterClient object
 */
@SuppressWarnings("squid:S2142")
@Log4j2
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
    public static boolean testConnection(VCenterAdapterClient client, VCenterSpec vCenterSpec) throws MangleException {
        boolean returnValue = false;
        try {
            ResponseEntity responseEntity =
                    client.post(VCenterConstants.TEST_CONNECTION, mapper.writeValueAsString(vCenterSpec), Object.class);
            if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                returnValue = true;
            } else if (responseEntity != null
                    && responseEntity.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                throw new MangleException(
                        String.format(ErrorConstants.VCENTER_AUTHENTICATION_FAILED, vCenterSpec.getVcServerUrl()),
                        ErrorCode.VCENTER_AUTHENTICATION_FAILED);
            }
        } catch (IOException e) {
            throw new MangleException(e.getMessage(), ErrorCode.GENERIC_ERROR, "NA");
        }
        return returnValue;
    }

    public static List<String> getVMsList(VCenterClient client, boolean random, Map<String, String> filters)
            throws MangleException {
        String filter = constructFilters(filters);
        String urlSuffix = VCenterConstants.VMS_LIST_QUERY;
        List<String> response = getVMS(client, urlSuffix, filter);

        if (random) {
            Random rand = new Random();
            return Collections.singletonList(response.get(rand.nextInt(response.size())));
        }

        return response;
    }

    public static List<String> getVMsList(VCenterClient client, String vmName, Map<String, String> filters)
            throws MangleException {
        String filter = constructFilters(filters);
        String urlSuffix = String.format(VCenterConstants.VMS_LIST_QUERY_BY_VM, vmName);
        return getVMS(client, urlSuffix, filter);
    }

    private static List<String> getVMS(VCenterClient client, String urlSuffix, String filter) throws MangleException {
        VCenterAdapterClient adapterClient = client.getVCenterAdapterClient();
        if (StringUtils.hasText(filter)) {
            urlSuffix += "?" + filter;
        }
        List<String> response = new ArrayList<>();
        client.testConnection();
        ResponseEntity<Object> responseEntity = (ResponseEntity<Object>) adapterClient.post(urlSuffix,
                VCenterAdapterClient.objectToJson(client.getVCenterSpec()), Object.class);

        if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            if (responseEntity.getBody() != null) {
                List<VM> vms = InventoryHelperUtil.convertLinkedHashMapToObjectList(responseEntity.getBody(), VM.class);
                response = vms.stream().map(VM::getVm).collect(Collectors.toList());
            }
        } else if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            VCenterAdapterErrorObj errorObj = InventoryHelperUtil.convertLinkedHashMapToObject(responseEntity.getBody(),
                    VCenterAdapterErrorObj.class);
            throw new MangleException(errorObj.getMessage(), ErrorCode.VCENTER_VM_NOT_FOUND);
        } else {
            throw new MangleException(ErrorCode.VCENTER_VM_NOT_FOUND);
        }
        return response;
    }

    public static List<String> getVMNicList(VCenterClient client, String vmId, boolean random) throws MangleException {
        VCenterAdapterClient adapterClient = client.getVCenterAdapterClient();
        String urlSuffix = String.format(VCenterConstants.VM_NIC, vmId);
        List<String> response = new ArrayList<>();
        adapterClient.testConnection();
        ResponseEntity<List<Object>> responseEntity = (ResponseEntity<List<Object>>) adapterClient.post(urlSuffix,
                VCenterAdapterClient.objectToJson(client.getVCenterSpec()), Object.class);

        if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            if (responseEntity.getBody() != null) {
                List<VMNic> vms =
                        InventoryHelperUtil.convertLinkedHashMapToObjectList(responseEntity.getBody(), VMNic.class);
                response = vms.stream().map(VMNic::getNic).collect(Collectors.toList());
            }
        } else if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            VCenterAdapterErrorObj errorObj = InventoryHelperUtil.convertLinkedHashMapToObject(responseEntity.getBody(),
                    VCenterAdapterErrorObj.class);
            throw new MangleException(errorObj.getMessage(), ErrorCode.VCENTER_VM_NIC_NOT_FOUND);
        }

        if (random) {
            Random rand = new Random();
            return Collections.singletonList(response.get(rand.nextInt(response.size())));
        }

        return response;
    }

    public static List<String> getVMDiskList(VCenterClient client, String vmId, boolean random) throws MangleException {
        VCenterAdapterClient adapterClient = client.getVCenterAdapterClient();
        String urlSuffix = String.format(VCenterConstants.VM_DISK, vmId);
        List<String> response = new ArrayList<>();
        adapterClient.testConnection();
        ResponseEntity<List<Object>> responseEntity = (ResponseEntity<List<Object>>) adapterClient.post(urlSuffix,
                VCenterAdapterClient.objectToJson(client.getVCenterSpec()), Object.class);

        if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            if (responseEntity.getBody() != null) {
                List<VMDisk> vms =
                        InventoryHelperUtil.convertLinkedHashMapToObjectList(responseEntity.getBody(), VMDisk.class);
                response = vms.stream().map(VMDisk::getDisk).collect(Collectors.toList());
            }
        } else if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            VCenterAdapterErrorObj errorObj = InventoryHelperUtil.convertLinkedHashMapToObject(responseEntity.getBody(),
                    VCenterAdapterErrorObj.class);
            throw new MangleException(errorObj.getMessage(), ErrorCode.VCENTER_VM_DISK_NOT_FOUND);
        }

        if (random) {
            Random rand = new Random();
            return Collections.singletonList(response.get(rand.nextInt(response.size())));
        }

        return response;
    }

    private static String constructFilters(Map<String, String> filters) {
        StringBuilder filter = new StringBuilder();
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            filter.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        return filter.toString().replaceAll("&+$", "");
    }

    /**
     * Turns off the VM on the remote VCenter
     *
     * @param client
     * @param vmId
     * @return CommandExecutionResult object with exit code set to 0 if power off is successful,
     *         else sets it to 1
     */
    @SuppressWarnings({ "unchecked", "unused" })
    public static CommandExecutionResult powerOffVM(VCenterClient client, String vmId) {
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        try {
            testAdapterClientConnection(clientAdapter);
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.POWER_OFF, vmId),
                            mapper.writeValueAsString(client.getVCenterSpec()), VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult.setCommandOutput(String.format("Power off operation on VM %s is successful", vmId));
            commandExecutionResult.setExitCode(0);
        } catch (IOException | MangleException e) {
            log.error(e);
            commandExecutionResult.setCommandOutput(
                    String.format("Power off of VM with id %s failed with error ", vmId) + e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * Turns off the VM on the remote Vcenter
     *
     * @param client
     * @param vmId
     * @return CommandExecutionResult object with exit code set to 0 if power on is successful, else
     *         sets it to 1
     */
    @SuppressWarnings("unchecked")
    public static CommandExecutionResult powerOnVM(VCenterClient client, String vmId) {
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            testAdapterClientConnection(clientAdapter);
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.POWER_ON, vmId),
                            mapper.writeValueAsString(client.getVCenterSpec()), VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult.setCommandOutput(String.format("Power On operation on VM %s is successful", vmId));
            commandExecutionResult.setExitCode(0);
        } catch (IOException | MangleException e) {
            log.error(e);
            commandExecutionResult.setCommandOutput(
                    String.format("Power on of VM with id %s failed with error ", vmId) + e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * Suspends VM on the remote VCenter
     *
     * @param client
     * @param vmId
     * @return CommandExecutionResult object with exit code set to 0 if suspend is successful, else
     *         sets it to 1
     */
    @SuppressWarnings("unchecked")
    public static CommandExecutionResult suspendVM(VCenterClient client, String vmId) {
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            testAdapterClientConnection(clientAdapter);
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.SUSPEND_VM, vmId),
                            mapper.writeValueAsString(client.getVCenterSpec()), VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult
                    .setCommandOutput(String.format("Suspend VM operation of VM with Id %s is successful", vmId));
            commandExecutionResult.setExitCode(0);
        } catch (IOException | MangleException e) {
            log.error(e);
            commandExecutionResult.setCommandOutput(
                    String.format("Suspend VM with Id %s Failed with the exception: %s", vmId, e.getMessage()));
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
    public static CommandExecutionResult resetVM(VCenterClient client, String vmName) {
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            testAdapterClientConnection(clientAdapter);
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.RESET_VM, vmName),
                            mapper.writeValueAsString(client.getVCenterSpec()), VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult
                    .setCommandOutput(String.format("Reset operation of VM with Id %s is successful", vmName));
            commandExecutionResult.setExitCode(0);
        } catch (IOException | MangleException e) {
            log.error(e);
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }

        return commandExecutionResult;
    }

    /**
     * Disconnects NIC from the VM on the remote Vcenter
     *
     * @param client
     * @param vmId
     * @param vmNicId
     * @return CommandExecutionResult object with exit code set to 0 if disconnect is successful,
     *         else sets it to 1
     */
    @SuppressWarnings("unchecked")
    public static CommandExecutionResult disconnectNicFromVM(VCenterClient client, String vmId, String vmNicId) {
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            testAdapterClientConnection(clientAdapter);
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.DISCONNECT_NIC_WITH_ID, vmId, vmNicId),
                            mapper.writeValueAsString(client.getVCenterSpec()), VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult.setCommandOutput(
                    String.format("Disconnect Nic operation of NIC %s on VM %s is successful", vmNicId, vmId));
            commandExecutionResult.setExitCode(0);
        } catch (IOException | MangleException e) {
            log.error(e);
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * Connects NIC to the VM on the remote VCenter
     *
     * @param client
     * @param vmId
     * @param vmNicId
     * @return CommandExecutionResult object with exit code set to 0 if connect NIC is successful,
     *         else sets it to 1
     */
    @SuppressWarnings("unchecked")
    public static CommandExecutionResult addNicFromVM(VCenterClient client, String vmId, String vmNicId) {
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            testAdapterClientConnection(clientAdapter);
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.CONNECT_NIC_WITH_ID, vmId, vmNicId),
                            mapper.writeValueAsString(client.getVCenterSpec()), VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult.setCommandOutput(
                    String.format("Connect Nic operation of NIC %s on VM %s is successful", vmNicId, vmId));
            commandExecutionResult.setExitCode(0);
        } catch (IOException | MangleException e) {
            log.error(e);
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * Disconnects Disk from VM on the remote VCenter
     *
     * @param client
     * @param vmId
     * @param vmDiskId
     * @return CommandExecutionResult object with exit code set to 0 if disconnect DISK is
     *         successful, else sets it to 1
     */
    @SuppressWarnings("unchecked")
    public static CommandExecutionResult disconnectDiskFromVM(VCenterClient client, String vmId, String vmDiskId) {
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            testAdapterClientConnection(clientAdapter);
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.DISCONNECT_DISK_WITH_ID, vmId, vmDiskId),
                            mapper.writeValueAsString(client.getVCenterSpec()), VCenterAdapterResponse.class);
            String result = pollForDiskDisconnect(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult.setCommandOutput(
                    String.format("Disconnect disk operation of disk %s on VM %s is successful with output: %s",
                            vmDiskId, vmId, result));
            commandExecutionResult.setExitCode(0);
        } catch (IOException | MangleException e) {
            log.error(e);
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * Connects the disconnected Disk to the VM on the remote VCenter
     *
     * @param client
     * @param vmId
     * @param vmDiskId
     * @param vmDiskType
     * @param vmDiskBackingType
     * @param vmDiskBackingFile
     * @return CommandExecutionResult object with exit code set to 0 if connect DISK is successful,
     *         else sets it to 1
     * @throws MangleException
     */
    @SuppressWarnings({ "unchecked" })
    public static CommandExecutionResult addDiskFromVM(VCenterClient client, String vmId, String vmDiskId,
            String vmDiskType, String vmDiskBackingType, String vmDiskBackingFile) throws MangleException {

        String request = "{\"vcenterspec\":  %s , \"diskspec\":  %s }";
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();

        VMDiskObj vmDiskObj = new VMDiskObj(vmDiskType, vmDiskBackingType, vmDiskBackingFile.replace("___", " "));
        try {
            String diskSpec = mapper.writeValueAsString(vmDiskObj);
            String vSpec = mapper.writeValueAsString(client.getVCenterSpec());
            request = String.format(request, vSpec, diskSpec);
        } catch (JsonProcessingException e) {
            throw new MangleException(e.getMessage(), ErrorCode.GENERIC_ERROR, "NA");
        }

        try {
            VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
            testAdapterClientConnection(clientAdapter);
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.CONNECT_DISK_WITH_ID, vmId, vmDiskId), request,
                            VCenterAdapterResponse.class);
            Thread.sleep(10000);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult.setCommandOutput(
                    String.format("Connect disk operation of disk %s on VM %s is successful", vmDiskId, vmId));
            commandExecutionResult.setExitCode(0);
        } catch (MangleException | InterruptedException e) {
            log.error(e);
            commandExecutionResult.setCommandOutput(e.getMessage());
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
        ResponseEntity<VCenterOperationTaskQueryResponse> responeEntity =
                VCenterTaskUtils.getTaskStatus(clientAdapter, taskId);

        handleTaskNotCompleted(responeEntity);

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
        ResponseEntity<VCenterOperationTaskQueryResponse> responeEntity =
                VCenterTaskUtils.getTaskStatus(clientAdapter, taskId);

        handleTaskNotCompleted(responeEntity);

        VMDiskObj vmDiskObj = mapper.convertValue(responeEntity.getBody().getVCenterVMObject(), VMDiskObj.class);
        return vmDiskObj.retrieveDiskInfo();
    }

    private static boolean testAdapterClientConnection(VCenterAdapterClient vCenterAdapterClient)
            throws MangleException {
        if (vCenterAdapterClient.testConnection()) {
            return true;
        } else {
            throw new MangleException(ErrorConstants.VCENTER_ADAPTER_CLIENT_UNREACHABLE,
                    ErrorCode.VCENTER_ADAPTER_CLIENT_UNREACHABLE);
        }
    }

    public static void handleTaskNotCompleted(ResponseEntity<VCenterOperationTaskQueryResponse> responseEntity)
            throws MangleException {
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)
                || !VCenterConstants.TASK_STATUS_COMPLETED.equals(responseEntity.getBody().getTaskStatus())) {
            throw new MangleException(responseEntity.getBody().getResponseMessage(), ErrorCode.GENERIC_ERROR);
        }
    }
}
