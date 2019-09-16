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

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.adapter.VMOperations;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.model.VCenterVMObject;
import com.vmware.mangle.model.VMDisk;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.status.VMOperationsTaskStatusEnum;

/**
 * @author chetanc
 *
 *         Provides wrapper over the VMOperations and makes each of the rest-api call async, and
 *         implements additional mechanism to verify that the fault is injected on the VM
 */
@Log4j2
@Service
public class VMOperationsTaskService {

    @Autowired
    VCenterClientInstantiationService clientInstantiationService;

    public boolean testConnection(VCenterSpec vCenterSpec) throws MangleException {
        VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
        return client.testConnection();
    }

    /**
     * triggers the VM power off, and verifies if the fault injection is complete by verifying the
     * actual status of the VM guest OS
     *
     * @param vmName
     * @param taskId
     * @param dcName
     * @throws MangleException
     *             : if VM is already off, VM is not found
     */
    @Async
    public void powerOffVM(String vmName, String taskId, String dcName, VCenterSpec vCenterSpec)
            throws MangleException {
        VCenterClient client;
        VMOperationsTaskStore.addTask(taskId, VMOperationsTaskStatusEnum.TRIGGERED.toString());
        try {
            client = clientInstantiationService.getVCenterClient(vCenterSpec);
            client.setJsonRequest(null);
            VMOperations.powerOffVM(client, vmName, null);
            log.info("Triggered power off VM for {} on VC", vmName);
        } catch (MangleException e) {
            log.error("Power off VM {} failed with the error: {}", vmName, e.getMessage());
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.FAILED.toString(), e.getMessage(),
                    null);
            return;
        }

        boolean isGuestOsOn = true;
        int retryCount = Constants.RETRY_COUNT;
        while (isGuestOsOn && retryCount > 0) {
            CommonUtils.delayInSeconds(5);
            retryCount--;
            isGuestOsOn = VMOperations.isVMPoweredOn(client, vmName);
        }

        if (!isGuestOsOn) {
            String message = String.format(ErrorConstants.VM_POWER_OFF_SUCCESSFUL, vmName);
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.COMPLETED.toString(), message);
            log.info(message);
        } else {
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.TIME_OUT.toString(), null);
            log.info("VM power off couldn't be verified on VM {}, timed out waiting for VM power status", vmName);
        }
        client.terminateConnection();
    }

    /**
     * triggers the VM power on, and verifies if the fault injection is complete by verifying the
     * actual status of the VM guest OS
     *
     * @param vmName
     * @param taskId
     * @throws MangleException
     *             : if VM is already on, VM is not found
     */
    @Async
    public void powerOnVM(String vmName, String taskId, VCenterSpec vCenterSpec) throws MangleException {
        VMOperationsTaskStore.addTask(taskId, VMOperationsTaskStatusEnum.TRIGGERED.toString());
        VCenterClient client;
        try {
            client = clientInstantiationService.getVCenterClient(vCenterSpec);
            client.setJsonRequest(null);
            VMOperations.powerOnVM(client, vmName);
            log.info("Triggered power on operation on VM {}", vmName);
        } catch (MangleException e) {
            log.error("Power on VM {} failed with the error: {}", vmName, e.getMessage());
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.FAILED.toString(), e.getMessage(),
                    null);
            return;
        }

        boolean isGuestOsOn = false;
        int retryCount = Constants.RETRY_COUNT;
        while (!isGuestOsOn && retryCount > 0) {
            retryCount--;
            CommonUtils.delayInSeconds(5);
            isGuestOsOn = VMOperations.isVMPoweredOn(client, vmName);
        }

        if (isGuestOsOn) {
            String message = String.format(ErrorConstants.VM_POWER_ON_SUCCESSFUL, vmName);
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.COMPLETED.toString(), message);
            log.info(message);
        } else {
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.TIME_OUT.toString(), null);
            log.info("VM power on couldn't be verified on VM {}, timed out waiting for VM power status", vmName);
        }
        client.terminateConnection();
    }

    /**
     * triggers the VM reset, and verifies if the fault injection is complete by verifying the
     * actual status of the VM guest OS
     *
     * @param vmName
     * @param taskId
     * @throws MangleException
     *             : VM is not found
     */
    @Async
    public void resetVM(String vmName, String taskId, VCenterSpec vCenterSpec) throws MangleException {
        VMOperationsTaskStore.addTask(taskId, VMOperationsTaskStatusEnum.TRIGGERED.toString());
        VCenterClient client;
        try {
            client = clientInstantiationService.getVCenterClient(vCenterSpec);
            client.setJsonRequest(null);
            VMOperations.resetVM(client, vmName);
            log.info("Triggered vm reset for VM {}", vmName);
        } catch (MangleException e) {
            log.error("Reset VM {} failed with the error: {}", vmName, e.getMessage());
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.FAILED.toString(), e.getMessage(),
                    null);
            return;
        }

        boolean isGuestOsOn = false;
        int retryCount = Constants.RETRY_COUNT;
        while (!isGuestOsOn && retryCount > 0) {
            CommonUtils.delayInSeconds(5);
            retryCount--;
            isGuestOsOn = VMOperations.isVMPoweredOn(client, vmName);
        }

        if (isGuestOsOn) {
            String message = String.format(ErrorConstants.VM_REBOOT_SUCCESSFUL, vmName);
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.COMPLETED.toString(), message);
            log.info(message);
        } else {
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.TIME_OUT.toString(), null);
            log.info("VM power on couldn't be verified on VM {}, timed out waiting for VM power status", vmName);
        }
        client.terminateConnection();
    }

    /**
     * triggers the VM suspension, and verifies if the fault injection is complete by verifying the
     * actual status of the VM guest OS
     *
     * @param vmName
     * @param taskId
     * @throws MangleException
     *             : VM is not found, VM is already powered off/suspended
     */
    @Async
    public void suspendVM(String vmName, String taskId, VCenterSpec vCenterSpec) throws MangleException {
        VMOperationsTaskStore.addTask(taskId, VMOperationsTaskStatusEnum.TRIGGERED.toString());
        VCenterClient client;
        try {
            client = clientInstantiationService.getVCenterClient(vCenterSpec);
            log.info("Suspend VM is triggered on {}", vmName);
            client.setJsonRequest(null);
            VMOperations.suspendVM(client, vmName);
        } catch (MangleException e) {
            log.error("Suspend VM {} failed with the error: {}", vmName, e.getMessage());
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.FAILED.toString(), e.getMessage(),
                    null);
            return;
        }

        boolean isVMSuspended = false;
        int retryCount = Constants.RETRY_COUNT * 6;
        while (!isVMSuspended && retryCount > 0) {
            isVMSuspended = VMOperations.isVMSuspended(client, vmName);
            retryCount--;
            CommonUtils.delayInSeconds(5);
        }

        if (isVMSuspended) {
            String message = String.format(ErrorConstants.VM_SUSPEND_SUCCESSFUL, vmName);
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.COMPLETED.toString(), message);
            log.info(message);
        } else {
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.TIME_OUT.toString(), null);
            log.info("VM suspend operation couldn't be verified on VM {}, timed out waiting for VM suspend status",
                    vmName);
        }
        client.terminateConnection();
    }

    /**
     * triggers VM delete disk operation
     *
     * @param vmName
     * @param diskId
     * @param taskId
     * @throws MangleException
     *             : VM is not found, VM is already powered off/suspended, disk not found
     */
    @Async
    public void deleteDiskFromVM(String vmName, String diskId, String taskId, VCenterSpec vCenterSpec)
            throws MangleException {
        VCenterClient client;
        VMOperationsTaskStore.addTask(taskId, VMOperationsTaskStatusEnum.TRIGGERED.toString());
        VCenterVMObject vmDisk;
        try {
            client = clientInstantiationService.getVCenterClient(vCenterSpec);
            vmDisk = VMOperations.getVMDisk(client, vmName, diskId);
            log.info("Deletion of the disk {} is triggered on VM {}", diskId, vmName);
            VMOperations.deleteDiskFromVM(client, diskId, vmName);
        } catch (MangleException e) {
            log.error("Delete disk {} on VM {} failed with the error: {}", diskId, vmName, e.getMessage());
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.FAILED.toString(),
                    e.getMessage());
            return;
        }

        boolean isDiskConnected = true;
        int retryCount = Constants.RETRY_COUNT;
        while (isDiskConnected && retryCount > 0) {
            CommonUtils.delayInSeconds(5);
            retryCount--;
            isDiskConnected = VMOperations.isDiskConnected(client, vmName, diskId);
        }

        if (!isDiskConnected) {
            String message = String.format(ErrorConstants.VM_DISCONNECT_DISK_SUCCESSFUL, vmName, diskId);
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.COMPLETED.toString(), message,
                    vmDisk);
            log.info(message);
        } else {
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.TIME_OUT.toString(), null,
                    vmDisk);
            log.info("VM delete disk operation couldn't be verified on VM {}, timed out waiting for disk status",
                    vmName);
        }
        client.terminateConnection();

    }

    /**
     * add disk back to vm
     *
     * @param vmName
     * @param diskSpec
     * @param taskId
     * @throws MangleException
     */
    @Async
    public void addDiskToVM(String vmName, VMDisk diskSpec, String diskId, String taskId, VCenterSpec vCenterSpec)
            throws MangleException {
        VCenterClient client;
        VMOperationsTaskStore.addTask(taskId, VMOperationsTaskStatusEnum.TRIGGERED.toString());
        try {
            client = clientInstantiationService.getVCenterClient(vCenterSpec);
            log.info("Addition of the disk {} on VM {} is triggered", diskSpec, vmName);
            VMOperations.addDiskToVM(client, diskSpec, vmName);
        } catch (JsonProcessingException | MangleException e) {
            log.error("Addition of the disk {} on VM {} failed with the error {}", diskSpec, vmName, e.getMessage());
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.FAILED.toString(), e.getMessage(),
                    null);
            return;
        }

        boolean isDiskConnected = false;
        int retryCount = Constants.RETRY_COUNT;
        while (!isDiskConnected && retryCount > 0) {
            CommonUtils.delayInSeconds(5);
            retryCount--;
            isDiskConnected = VMOperations.isDiskConnected(client, vmName, diskId);
        }

        if (isDiskConnected) {
            String message = String.format(ErrorConstants.VM_CONNECT_DISK_SUCCESSFUL, vmName, diskSpec);
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.COMPLETED.toString(), message);
            log.info(message);
        } else {
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.TIME_OUT.toString(), null);
            log.info("VM add disk operation couldn't be verified on VM {}, timed out waiting for disk status", vmName);
        }
        client.terminateConnection();
    }

    /**
     * disconnect NIC from the VM
     *
     * @param vmName
     * @param nicId
     * @param taskId
     * @throws MangleException
     *             : if nic not found, vm not found, vm turned off, nic is already disconnected
     */
    @Async
    public void disonnectNicFromVM(String vmName, String nicId, String taskId, VCenterSpec vCenterSpec)
            throws MangleException {
        VMOperationsTaskStore.addTask(taskId, VMOperationsTaskStatusEnum.TRIGGERED.toString());
        VCenterClient client;
        try {
            client = clientInstantiationService.getVCenterClient(vCenterSpec);
            log.info("NIC {} disconnect is triggered on VM {}", nicId, vmName);
            VMOperations.disconnectNicFromVM(client, nicId, vmName);
        } catch (MangleException e) {
            log.error("Delete NIC {} on VM {} failed with the error: {}", nicId, vmName, e.getMessage());
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.FAILED.toString(), e.getMessage(),
                    null);
            return;
        }

        boolean isNicConnected = true;
        int retryCount = Constants.RETRY_COUNT;
        while (isNicConnected && retryCount > 0) {
            CommonUtils.delayInSeconds(5);
            retryCount--;
            isNicConnected = VMOperations.isNicConnected(client, vmName, nicId);
        }

        if (!isNicConnected) {
            String message = String.format(ErrorConstants.VM_DISCONNECT_NIC_SUCCESSFUL, nicId, vmName);
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.COMPLETED.toString(), message);
            log.info(message);
        } else {
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.TIME_OUT.toString(), null);
            log.info("VM disconnect nic operation couldn't be verified on VM {}, timed out waiting for the nic status",
                    vmName);
        }
        client.terminateConnection();
    }

    /**
     * connect nic to VM
     *
     * @param vmName
     * @param nicId
     * @param taskId
     * @throws MangleException
     *             : if nic not found, vm not found, vm turned off, nic is already disconnected
     */
    @Async
    public void connectNicToVM(String vmName, String nicId, String taskId, VCenterSpec vCenterSpec)
            throws MangleException {
        VMOperationsTaskStore.addTask(taskId, VMOperationsTaskStatusEnum.TRIGGERED.toString());
        VCenterClient client;
        try {
            client = clientInstantiationService.getVCenterClient(vCenterSpec);
            log.info("Connect NIC {} on VM {} is triggered", nicId, vmName);
            VMOperations.connectNicToVM(client, nicId, vmName);
        } catch (MangleException e) {
            log.error("Connect NIC {} on VM {} failed with the error: {}", nicId, vmName, e.getMessage());
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.FAILED.toString(), e.getMessage(),
                    null);
            return;
        }

        boolean isNicConnected = false;
        int retryCount = Constants.RETRY_COUNT;
        while (!isNicConnected && retryCount > 0) {
            CommonUtils.delayInSeconds(5);
            retryCount--;
            isNicConnected = VMOperations.isNicConnected(client, vmName, nicId);
        }

        if (isNicConnected) {
            String message = String.format(ErrorConstants.VM_CONNECT_NIC_SUCCESSFUL, nicId, vmName);
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.COMPLETED.toString(), message);
            log.info(message);
        } else {
            VMOperationsTaskStore.updateTaskStatus(taskId, VMOperationsTaskStatusEnum.TIME_OUT.toString(), null);
            log.info("VM connect nic operation couldn't be verified on VM {}, timed out waiting for the nic status",
                    vmName);
        }
        client.terminateConnection();
    }
}
