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

package com.vmware.mangle.service;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.vmware.mangle.adapter.HostOperations;
import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.inventory.helpers.HostInventoryHelper;
import com.vmware.mangle.model.Host;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.utils.RetryUtils;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.status.VCenterOperationsTaskStatus;

/**
 * @author chetanc
 */
@Log4j2
@Component
public class HostOperationsService {
    private VCenterClientInstantiationService clientInstantiationService;
    private HostInventoryHelper hostInventoryHelper;
    private VCenterOperationsTaskStore vCenterOperationsTaskStore;
    private HostOperations hostOperations;

    @Autowired
    public HostOperationsService(HostInventoryHelper hostInventoryHelper,
            VCenterOperationsTaskStore vCenterOperationsTaskStore,
            VCenterClientInstantiationService clientInstantiationService, HostOperations hostOperations) {
        this.hostInventoryHelper = hostInventoryHelper;
        this.vCenterOperationsTaskStore = vCenterOperationsTaskStore;
        this.clientInstantiationService = clientInstantiationService;
        this.hostOperations = hostOperations;
    }

    public List<Host> getHosts(String cluster, String datacenterName, String folder, VCenterSpec vCenterSpec)
            throws MangleException {
        VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
        return hostInventoryHelper.getAllHost(client, cluster, datacenterName, folder);
    }

    public List<Host> getHosts(String hostName, String cluster, String datacenterName, String folder, VCenterSpec vCenterSpec)
            throws MangleException {
        VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
        List<Host> hosts = new ArrayList<>();
        hosts.add(hostInventoryHelper.getHostByName(client, hostName, cluster, datacenterName, folder));
        return hosts;
    }

    @Async
    public void disconnectHost(String taskId, String host, String clusterName, String datacenterName, String folder,
            VCenterSpec vCenterSpec) throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            String hostId = hostInventoryHelper.getHostId(client, host, clusterName, datacenterName, folder);
            handleHostDisconnection(client, hostId, taskId);
        } catch (MangleException e) {
            log.error("Disconnect Host with name {} failed with an exception: {}", host, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }

    @Async
    public void disconnectHostById(String taskId, String hostId, VCenterSpec vCenterSpec) throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            handleHostDisconnection(client, hostId, taskId);
        } catch (MangleException e) {
            log.error("Disconnect Host By Id {} failed with an exception: {}", hostId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }

    @Async
    public void connectHost(String taskId, String host, String clusterName, String datacenterName, String folder,
            VCenterSpec vCenterSpec) throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            String hostId = hostInventoryHelper.getHostId(client, host, clusterName, datacenterName, folder);
            handleHostConnection(client, hostId, taskId);
        } catch (MangleException e) {
            log.error("Connect Host with name {} failed with an exception: {}", host, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }

    @Async
    public void connectHostById(String taskId, String hostId, VCenterSpec vCenterSpec) throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            handleHostConnection(client, hostId, taskId);
        } catch (MangleException e) {
            log.error("Connect Host with id {} failed with an exception: {}", hostId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }

    public void handleHostDisconnection(VCenterClient client, String hostId, String taskId) throws MangleException {
        try {
            client.setJsonRequest(null);
            hostOperations.disconnectHost(client, hostId);
            log.info("Triggered disconnect operation on host {}", hostId);
        } catch (MangleException e) {
            log.error("Disconnect Host with id {} failed with an exception: {}", hostId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage(), null);
            return;
        }

        int retryCount = Constants.RETRY_COUNT;
        boolean hostConnected = RetryUtils.retry(() -> {
            boolean isHostConnected = hostOperations.isHostConnected(client, hostId);
            if (isHostConnected) {
                throw new MangleException("Host is not powered off yet");
            }
            return false;
        }, new MangleException("Timeout waiting for response"), retryCount, 5);

        if (!hostConnected) {
            String message = String.format(ErrorConstants.HOST_DISCONNECTED_SUCCESSFUL, hostId);
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.COMPLETED.toString(),
                    message);
            log.info(message);
        } else {
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.TIME_OUT.toString(), null);
            log.error("Host disconnection couldn't be verified on HOST {}, timed out waiting for host "
                    + "disconnect status", hostId);
        }
        client.terminateConnection();
    }

    public void handleHostConnection(VCenterClient client, String hostId, String taskId) throws MangleException {
        try {
            client.setJsonRequest(null);
            hostOperations.connectHost(client, hostId);
            log.info("Triggered connect operation on host {}", hostId);
        } catch (MangleException e) {
            log.error("Connect Host with id {} failed with an exception: {}", hostId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage(), null);
            return;
        }

        int retryCount = Constants.RETRY_COUNT;
        boolean hostConnected = RetryUtils.retry(() -> {
            boolean isHostConnected = hostOperations.isHostConnected(client, hostId);
            if (!isHostConnected) {
                throw new MangleException("Host is not powered off yet");
            }
            return true;
        }, new MangleException("Timeout waiting for response"), retryCount, 5);

        if (hostConnected) {
            String message = String.format(ErrorConstants.HOST_CONNECTED_SUCCESSFUL, hostId);
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.COMPLETED.toString(),
                    message);
            log.info(message);
        } else {
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.TIME_OUT.toString(), null);
            log.info("Host {} connect verification failed, timed out waiting for connect status", hostId);
        }
        client.terminateConnection();
    }


}
