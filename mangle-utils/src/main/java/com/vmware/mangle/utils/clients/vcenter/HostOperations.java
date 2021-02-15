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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.model.response.VCenterAdapterResponse;
import com.vmware.mangle.model.response.VCenterOperationTaskQueryResponse;
import com.vmware.mangle.model.vcenter.VCenterAdapterErrorObj;
import com.vmware.mangle.model.vcenter.VCenterHost;
import com.vmware.mangle.utils.InventoryHelperUtil;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.VCenterConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 */
public class HostOperations {

    private HostOperations() {
    }

    public static List<String> getHostList(VCenterClient client, boolean random, Map<String, String> filters)
            throws MangleException {
        VCenterAdapterClient adapterClient = client.getVCenterAdapterClient();
        String filter = constructFilters(filters);
        String urlSuffix = VCenterConstants.HOST_LIST_QUERY;
        if (StringUtils.hasText(filter)) {
            urlSuffix += "?" + filter;
        }
        List<String> response = new ArrayList<>();
        adapterClient.testConnection();
        ResponseEntity<List<Object>> responseEntity = (ResponseEntity<List<Object>>) adapterClient.post(urlSuffix,
                VCenterAdapterClient.objectToJson(client.getVCenterSpec()), Object.class);

        if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            if (responseEntity.getBody() != null) {
                List<VCenterHost> VCenterHosts = InventoryHelperUtil
                        .convertLinkedHashMapToObjectList(responseEntity.getBody(), VCenterHost.class);
                response = VCenterHosts.stream().map(VCenterHost::getHost).collect(Collectors.toList());
            }
        } else if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            VCenterAdapterErrorObj errorObj = InventoryHelperUtil.convertLinkedHashMapToObject(responseEntity.getBody(),
                    VCenterAdapterErrorObj.class);
            throw new MangleException(errorObj.getMessage(), ErrorCode.VCENTER_HOST_NOT_FOUND,
                    "Error: " + errorObj.getMessage());
        }

        if (random) {
            Random rand = new Random();
            return Collections.singletonList(response.get(rand.nextInt(response.size())));
        }

        return response;
    }

    public static List<String> getHostList(VCenterClient client, String hostName, Map<String, String> filters)
            throws MangleException {
        VCenterAdapterClient adapterClient = client.getVCenterAdapterClient();
        String filter = constructFilters(filters);
        String urlSuffix = String.format(VCenterConstants.HOST_LIST_QUERY_BY_HOST, hostName);
        if (StringUtils.hasText(filter)) {
            urlSuffix += "&" + filter;
        }
        List<String> response = new ArrayList<>();
        adapterClient.testConnection();
        ResponseEntity<List<Object>> responseEntity = (ResponseEntity<List<Object>>) adapterClient.post(urlSuffix,
                VCenterAdapterClient.objectToJson(client.getVCenterSpec()), Object.class);

        if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            if (responseEntity.getBody() != null) {
                List<VCenterHost> vms = InventoryHelperUtil.convertLinkedHashMapToObjectList(responseEntity.getBody(),
                        VCenterHost.class);
                response = vms.stream().map(VCenterHost::getHost).collect(Collectors.toList());
            }
        } else if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            VCenterAdapterErrorObj errorObj = InventoryHelperUtil.convertLinkedHashMapToObject(responseEntity.getBody(),
                    VCenterAdapterErrorObj.class);
            throw new MangleException(errorObj.getMessage(), ErrorCode.VCENTER_HOST_NOT_FOUND,
                    "Error: " + errorObj.getMessage());
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
     * Disconnects Host from VCenter
     *
     * @param client
     * @param hostId
     * @return CommandExecutionResult object with exit code set to 0 if disconnect is successful,
     *         else sets it to 1
     */
    @SuppressWarnings("unchecked")
    public static CommandExecutionResult disconnectHostFromVC(VCenterClient client, String hostId) {
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            testAdapterClientConnection(clientAdapter);
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.HOST_DISCONNECT, hostId),
                            RestTemplateWrapper.objectToJson(client.getVCenterSpec()), VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult
                    .setCommandOutput(String.format("Host Disconnect operation of host %s is successful", hostId));
            commandExecutionResult.setExitCode(0);
        } catch (MangleException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * Connects Host to the VCenter
     *
     * @param client
     * @param hostId
     * @return CommandExecutionResult object with exit code set to 0 if connect NIC is successful,
     *         else sets it to 1
     */
    @SuppressWarnings("unchecked")
    public static CommandExecutionResult addHostToVC(VCenterClient client, String hostId) {
        VCenterAdapterClient clientAdapter = client.getVCenterAdapterClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            testAdapterClientConnection(clientAdapter);
            ResponseEntity<VCenterAdapterResponse> responseEntity =
                    (ResponseEntity<VCenterAdapterResponse>) clientAdapter.post(
                            String.format(VCenterConstants.HOST_CONNECT, hostId),
                            RestTemplateWrapper.objectToJson(client.getVCenterSpec()), VCenterAdapterResponse.class);
            pollForTaskStatusChange(clientAdapter, responseEntity.getBody().getTaskId());
            commandExecutionResult
                    .setCommandOutput(String.format("Host connect operation of host %s is successful", hostId));
            commandExecutionResult.setExitCode(0);
        } catch (MangleException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
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

    public static void handleTaskNotCompleted(ResponseEntity<VCenterOperationTaskQueryResponse> responseEntity)
            throws MangleException {
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)
                || !VCenterConstants.TASK_STATUS_COMPLETED.equals(responseEntity.getBody().getTaskStatus())) {
            throw new MangleException(responseEntity.getBody().getResponseMessage(), ErrorCode.GENERIC_ERROR);
        }
    }
}
