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

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.adapter.VMOperations;
import com.vmware.mangle.inventory.helpers.VMInventoryHelper;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.model.VCenterVMObject;
import com.vmware.mangle.model.VM;
import com.vmware.mangle.model.VMDisk;
import com.vmware.mangle.model.VMDiskDetails;
import com.vmware.mangle.model.VMNic;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.RetryUtils;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.status.VCenterOperationsTaskStatus;

/**
 * @author chetanc
 *         <p>
 *         Provides wrapper over the VMOperations and makes each of the rest-api call async, and
 *         implements additional mechanism to verify that the fault is injected on the VM
 */
@Log4j2
@Service
public class VMOperationsTaskService {

    private VCenterClientInstantiationService clientInstantiationService;
    private VMInventoryHelper vmInventoryHelper;
    private VCenterOperationsTaskStore vCenterOperationsTaskStore;
    private VMOperations vmOperations;

    @Autowired
    public VMOperationsTaskService(VCenterClientInstantiationService clientInstantiationService,
            VMInventoryHelper vmInventoryHelper, VCenterOperationsTaskStore vCenterOperationsTaskStore,
            VMOperations vmOperations) {
        this.clientInstantiationService = clientInstantiationService;
        this.vmInventoryHelper = vmInventoryHelper;
        this.vCenterOperationsTaskStore = vCenterOperationsTaskStore;
        this.vmOperations = vmOperations;
    }


    public List<VM> getVMs(String host, String clusterName, String dcName, String folder, String resourcePoolName,
            VCenterSpec vCenterSpec) throws MangleException {
        List<VM> vms = new ArrayList<>();
        try {
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            vms.addAll(vmInventoryHelper.getAllVM(client, host, clusterName, dcName, folder, resourcePoolName));
        } catch (MangleException e) {
            log.error("Get all VMs failed with the error: {}", e.getMessage());
            throw e;
        }
        return vms;
    }


    public List<VM> getVMs(String vmName, String host, String clusterName, String dcName, String folder,
            String resourcePoolName, VCenterSpec vCenterSpec) throws MangleException {
        List<VM> vms = new ArrayList<>();
        try {
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            vms.add(vmInventoryHelper.getVMByName(client, vmName, host, clusterName, dcName, folder, resourcePoolName));
        } catch (MangleException e) {
            log.error("Get all VMs failed with the error: {}", e.getMessage());
            throw e;
        }
        return vms;
    }


    public List<VMNic> getVMNics(String vmId, VCenterSpec vCenterSpec) throws MangleException {
        List<VMNic> vmNics = new ArrayList<>();
        try {
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            vmNics.addAll(vmInventoryHelper.getEthernetConnectedToVM(client, vmId));
        } catch (MangleException e) {
            log.error("Get all VMs failed with the error: {}", e.getMessage());
            throw e;
        }
        return vmNics;
    }


    public List<VMDisk> getVMDisks(String vmId, VCenterSpec vCenterSpec) throws MangleException {
        List<VMDisk> vmDisks = new ArrayList<>();
        try {
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            vmDisks.addAll(vmInventoryHelper.getDiskConnectedToVM(client, vmId));
        } catch (MangleException e) {
            log.error("Get all VMs failed with the error: {}", e.getMessage());
            throw e;
        }
        return vmDisks;
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
    public void powerOffVM(String vmName, String taskId, String host, String clusterName, String dcName, String folder,
            String resourcePoolName, VCenterSpec vCenterSpec) throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            String vmId =
                    vmInventoryHelper.getVMId(client, vmName, host, clusterName, dcName, folder, resourcePoolName);
            handlePowerOffVM(client, taskId, vmId);
        } catch (MangleException e) {
            log.error("Power off VM with name {} failed with the exception: {}", vmName, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }

    @Async
    public void powerOffVMById(String vmId, String taskId, VCenterSpec vCenterSpec) throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            handlePowerOffVM(client, taskId, vmId);
        } catch (MangleException e) {
            log.error("Power off VM with id {} failed with the exception: {}", vmId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }

    /**
     * triggers the VM power on, and verifies if the fault injection is complete by verifying the actual
     * status of the VM guest OS
     *
     * @param vmName
     * @param taskId
     * @throws MangleException
     *             : if VM is already on, VM is not found
     */
    @Async
    public void powerOnVM(String vmName, String taskId, String host, String clusterName, String dcName, String folder,
            String resourcePoolName, VCenterSpec vCenterSpec) throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            String vmId =
                    vmInventoryHelper.getVMId(client, vmName, host, clusterName, dcName, folder, resourcePoolName);
            handlePowerOnVM(client, taskId, vmId);
        } catch (MangleException e) {
            log.error("Power on VM with name {} failed with the exception: {}", vmName, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }


    @Async
    public void powerOnVMById(String vmId, String taskId, VCenterSpec vCenterSpec) throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            handlePowerOnVM(client, taskId, vmId);
        } catch (MangleException e) {
            log.error("Power on VM with id {} failed with the exception: {}", vmId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }

    /**
     * triggers the VM reset, and verifies if the fault injection is complete by verifying the actual
     * status of the VM guest OS
     *
     * @param vmName
     * @param taskId
     * @throws MangleException
     *             : VM is not found
     */
    @Async
    public void resetVM(String vmName, String taskId, String host, String clusterName, String dcName, String folder,
            String resourcePoolName, VCenterSpec vCenterSpec) throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            String vmId =
                    vmInventoryHelper.getVMId(client, vmName, host, clusterName, dcName, folder, resourcePoolName);
            handleResetVM(client, taskId, vmId);
        } catch (MangleException e) {
            log.error("Reset VM, on VM with name {} failed with the exception: {}", vmName, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }

    @Async
    public void resetVMById(String vmId, String taskId, VCenterSpec vCenterSpec) throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            handleResetVM(client, taskId, vmId);
        } catch (MangleException e) {
            log.error("Reset VM on, VM with id {} failed with the exception: {}", vmId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
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
    public void suspendVM(String vmName, String taskId, String host, String clusterName, String dcName, String folder,
            String resourcePoolName, VCenterSpec vCenterSpec) throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            String vmId =
                    vmInventoryHelper.getVMId(client, vmName, host, clusterName, dcName, folder, resourcePoolName);
            handleSuspendVM(client, taskId, vmId);
        } catch (MangleException e) {
            log.error("Suspend VM, on VM with name {} failed with an exception: {}", vmName, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }

    @Async
    public void suspendVMById(String vmId, String taskId, VCenterSpec vCenterSpec) throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            handleSuspendVM(client, taskId, vmId);
        } catch (MangleException e) {
            log.error("Suspend VM, on VM with id {} failed with an exception: {}", vmId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
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
    public void deleteDiskFromVM(String vmName, String diskId, String taskId, String host, String clusterName,
            String dcName, String folder, String resourcePoolName, VCenterSpec vCenterSpec) throws MangleException {
        try {
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            String vmId =
                    vmInventoryHelper.getVMId(client, vmName, host, clusterName, dcName, folder, resourcePoolName);
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            handleDeleteDiskFromVM(client, taskId, vmId, diskId);
        } catch (MangleException e) {
            log.error("Delete disk of disk {} on the VM with name {}, failed with an exception: {}", diskId, vmName,
                    e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }

    @Async
    public void deleteDiskFromVMById(String vmId, String diskId, String taskId, VCenterSpec vCenterSpec)
            throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            handleDeleteDiskFromVM(client, taskId, vmId, diskId);
        } catch (MangleException e) {
            log.error("Disconnect disk of disk {} on the VM with id {} failed with an exception: {}", diskId, vmId,
                    e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
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
    public void addDiskToVM(String vmName, VMDiskDetails diskSpec, String diskId, String taskId, String host,
            String clusterName, String dcName, String folder, String resourcePoolName, VCenterSpec vCenterSpec)
            throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            String vmId =
                    vmInventoryHelper.getVMId(client, vmName, host, clusterName, dcName, folder, resourcePoolName);
            handleAddDiskToVM(client, taskId, vmId, diskId, diskSpec);
        } catch (MangleException e) {
            log.error("Connect disk of disk {} on the VM with name {} failed with an exception: {}", diskId, vmName,
                    e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }

    @Async
    public void addDiskToVMById(String vmId, VMDiskDetails diskSpec, String diskId, String taskId,
            VCenterSpec vCenterSpec) throws MangleException {
        try {
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            handleAddDiskToVM(client, taskId, vmId, diskId, diskSpec);
        } catch (MangleException e) {
            log.error("Connect disk of disk {} on the VM with id {} failed with an exception: {}", diskId, vmId,
                    e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
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
    public void disconnectNicFromVM(String vmName, String nicId, String taskId, String host, String clusterName,
            String dcName, String folder, String resourcePoolName, VCenterSpec vCenterSpec) throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            String vmId =
                    vmInventoryHelper.getVMId(client, vmName, host, clusterName, dcName, folder, resourcePoolName);
            handleDisconnectNicFromVM(client, taskId, vmId, nicId);
        } catch (MangleException e) {
            log.error("Disconnect NIC with id {} on VM with name {} failed with the exception: {}", nicId, vmName,
                    e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }

    @Async
    public void disconnectNicFromVMById(String vmId, String nicId, String taskId, VCenterSpec vCenterSpec)
            throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            handleDisconnectNicFromVM(client, taskId, vmId, nicId);
        } catch (MangleException e) {
            log.error("Disconnect NIC with id {} on VM with id {} failed with the exception: {}", nicId, vmId,
                    e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
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
    public void connectNicToVM(String vmName, String nicId, String taskId, String host, String clusterName,
            String dcName, String folder, String resourcePoolName, VCenterSpec vCenterSpec) throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            String vmId =
                    vmInventoryHelper.getVMId(client, vmName, host, clusterName, dcName, folder, resourcePoolName);
            handleConnectNicToVM(client, taskId, vmId, nicId);
        } catch (MangleException e) {
            log.error("Connect NIC with id {} on VM with name {} failed with the exception: {}", nicId, vmName,
                    e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }

    @Async
    public void connectNicToVM(String vmId, String nicId, String taskId, VCenterSpec vCenterSpec)
            throws MangleException {
        try {
            vCenterOperationsTaskStore.addTask(taskId, VCenterOperationsTaskStatus.TRIGGERED.toString());
            VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
            handleConnectNicToVM(client, taskId, vmId, nicId);
        } catch (MangleException e) {
            log.error("Connect NIC with id {} on VM with name {} failed with the exception: {}", nicId, vmId,
                    e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
        }
    }

    private void handlePowerOffVM(VCenterClient client, String taskId, String vmId) throws MangleException {
        try {
            client.setJsonRequest(null);
            vmOperations.powerOffVM(client, vmId);
            log.info("Triggered power off VM for {} on VC", vmId);
        } catch (MangleException e) {
            log.error("Power off VM {} failed with the error: {}", vmId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage(), null);
            return;
        }

        boolean isGuestOsOn = true;
        int retryCount = Constants.RETRY_COUNT;
        while (isGuestOsOn && retryCount > 0) {
            CommonUtils.delayInSeconds(5);
            retryCount--;
            isGuestOsOn = vmOperations.isVMPoweredOn(client, vmId);
        }

        if (!isGuestOsOn) {
            String message = String.format(ErrorConstants.VM_POWER_OFF_SUCCESSFUL, vmId);
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.COMPLETED.toString(),
                    message);
            log.info(message);
        } else {
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.TIME_OUT.toString(), null);
            log.info("VM power off couldn't be verified on VM {}, timed out waiting for VM power status", vmId);
        }
        client.terminateConnection();
    }

    private void handlePowerOnVM(VCenterClient client, String taskId, String vmId) throws MangleException {
        try {
            client.setJsonRequest(null);
            vmOperations.powerOnVM(client, vmId);
            log.info("Triggered power on operation on VM {}", vmId);
        } catch (MangleException e) {
            log.error("Power on VM {} failed with the error: {}", vmId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage(), null);
            return;
        }

        int retryCount = Constants.RETRY_COUNT;
        boolean isVMOn = RetryUtils.retry(() -> {
            boolean isGuestOsOn = vmOperations.isVMPoweredOn(client, vmId);
            if (!isGuestOsOn) {
                throw new MangleException("VM is not powered on yet");
            }
            return true;
        }, new MangleException("Timeout waiting for response"), retryCount, 5);

        if (isVMOn) {
            String message = String.format(ErrorConstants.VM_POWER_ON_SUCCESSFUL, vmId);
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.COMPLETED.toString(),
                    message);
            log.info(message);
        } else {
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.TIME_OUT.toString(), null);
            log.info("VM power on couldn't be verified on VM {}, timed out waiting for VM power status", vmId);
        }
        client.terminateConnection();
    }


    private void handleResetVM(VCenterClient client, String taskId, String vmId) throws MangleException {
        try {
            client.setJsonRequest(null);
            vmOperations.resetVM(client, vmId);
            log.info("Triggered vm reset for VM {}", vmId);
        } catch (MangleException e) {
            log.error("Reset VM {} failed with the error: {}", vmId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage(), null);
            return;
        }

        int retryCount = Constants.RETRY_COUNT;
        boolean isVMOn = RetryUtils.retry(() -> {
            boolean isGuestOsOn = vmOperations.isVMPoweredOn(client, vmId);
            if (!isGuestOsOn) {
                throw new MangleException("VM is not reset yet");
            }
            return true;
        }, new MangleException("Timeout waiting for response"), retryCount, 5);

        if (isVMOn) {
            String message = String.format(ErrorConstants.VM_REBOOT_SUCCESSFUL, vmId);
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.COMPLETED.toString(),
                    message);
            log.info(message);
        } else {
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.TIME_OUT.toString(), null);
            log.info("VM power on couldn't be verified on VM {}, timed out waiting for VM power status", vmId);
        }
        client.terminateConnection();
    }


    private void handleSuspendVM(VCenterClient client, String taskId, String vmId) throws MangleException {
        try {
            log.info("Suspend VM is triggered on {}", vmId);
            client.setJsonRequest(null);
            vmOperations.suspendVM(client, vmId);
        } catch (MangleException e) {
            log.error("Suspend VM {} failed with the error: {}", vmId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage(), null);
            return;
        }

        int retryCount = Constants.RETRY_COUNT * 6;

        boolean isVMSus = RetryUtils.retry(() -> {
            boolean isVMSuspended = vmOperations.isVMSuspended(client, vmId);
            if (!isVMSuspended) {
                throw new MangleException("VM is not suspended yet");
            }
            return true;
        }, new MangleException("Timeout waiting for response"), retryCount, 5);

        if (isVMSus) {
            String message = String.format(ErrorConstants.VM_SUSPEND_SUCCESSFUL, vmId);
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.COMPLETED.toString(),
                    message);
            log.info(message);
        } else {
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.TIME_OUT.toString(), null);
            log.info("VM suspend operation couldn't be verified on VM {}, timed out waiting for VM suspend status",
                    vmId);
        }
        client.terminateConnection();
    }


    private void handleDeleteDiskFromVM(VCenterClient client, String taskId, String vmId, String diskId)
            throws MangleException {
        VCenterVMObject vmDisk;
        try {
            vmDisk = vmOperations.getVMDisk(client, vmId, diskId);
            log.info("Deletion of the disk {} is triggered on VM {}", diskId, vmId);
            vmOperations.deleteDiskFromVM(client, vmId, diskId);
        } catch (MangleException e) {
            log.error("Delete disk {} on VM {} failed with the error: {}", diskId, vmId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage());
            return;
        }

        int retryCount = Constants.RETRY_COUNT;
        boolean isDiskConnected = RetryUtils.retry(() -> {
            boolean diskConnected = vmOperations.isDiskConnected(client, vmId, diskId);
            if (diskConnected) {
                throw new MangleException("Disk disconnection is still in progress");
            }
            return false;
        }, new MangleException("Timeout waiting for response"), retryCount, 5);

        if (!isDiskConnected) {
            String message = String.format(ErrorConstants.VM_DISCONNECT_DISK_SUCCESSFUL, vmId, diskId);
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.COMPLETED.toString(),
                    message, vmDisk);
            log.info(message);
        } else {
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.TIME_OUT.toString(), null,
                    vmDisk);
            log.info("VM delete disk operation couldn't be verified on VM {}, timed out waiting for disk status", vmId);
        }
        client.terminateConnection();
    }


    private void handleAddDiskToVM(VCenterClient client, String taskId, String vmId, String diskId,
            VMDiskDetails diskSpec) throws MangleException {
        try {
            log.info("Addition of the disk {} on VM {} is triggered", diskSpec, vmId);
            vmOperations.addDiskToVM(client, diskSpec, vmId);
        } catch (JsonProcessingException | MangleException e) {
            log.error("Addition of the disk {} on VM {} failed with the error {}", diskSpec, vmId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage(), null);
            return;
        }

        int retryCount = Constants.RETRY_COUNT;
        boolean isDiskConnected = RetryUtils.retry(() -> {
            boolean diskConnected = vmOperations.isDiskConnected(client, vmId, diskId);
            if (!diskConnected) {
                throw new MangleException("Disk connection is still in progress");
            }
            return true;
        }, new MangleException("Timeout waiting for response"), retryCount, 5);

        if (isDiskConnected) {
            handleResetVM(client, taskId, vmId);
            String message = String.format(ErrorConstants.VM_CONNECT_DISK_SUCCESSFUL, vmId, diskSpec);
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.COMPLETED.toString(),
                    message);
            log.info(message);
        } else {
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.TIME_OUT.toString(), null);
            log.info("VM add disk operation couldn't be verified on VM {}, timed out waiting for disk status", vmId);
        }
    }


    private void handleDisconnectNicFromVM(VCenterClient client, String taskId, String vmId, String nicId)
            throws MangleException {
        try {
            log.info("NIC {} disconnect is triggered on VM {}", nicId, vmId);
            vmOperations.disconnectNicFromVM(client, nicId, vmId);
        } catch (MangleException e) {
            log.error("Delete NIC {} on VM {} failed with the error: {}", nicId, vmId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage(), null);
            return;
        }

        int retryCount = Constants.RETRY_COUNT;
        boolean isNicConnected = RetryUtils.retry(() -> {
            boolean nicConnected = vmOperations.isNicConnected(client, vmId, nicId);
            if (nicConnected) {
                throw new MangleException("Nic disconnection is still in progress");
            }
            return false;
        }, new MangleException("Timeout waiting for response"), retryCount, 5);


        if (!isNicConnected) {
            String message = String.format(ErrorConstants.VM_DISCONNECT_NIC_SUCCESSFUL, nicId, vmId);
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.COMPLETED.toString(),
                    message);
            log.info(message);
        } else {
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.TIME_OUT.toString(), null);
            log.info("VM disconnect nic operation couldn't be verified on VM {}, timed out waiting for the "
                    + "nic status", vmId);
        }
        client.terminateConnection();
    }


    private void handleConnectNicToVM(VCenterClient client, String taskId, String vmId, String nicId)
            throws MangleException {
        try {
            log.info("Connect NIC {} on VM {} is triggered", nicId, vmId);
            vmOperations.connectNicToVM(client, nicId, vmId);
        } catch (MangleException e) {
            log.error("Connect NIC {} on VM {} failed with the error: {}", nicId, vmId, e.getMessage());
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.FAILED.toString(),
                    e.getMessage(), null);
            return;
        }

        int retryCount = Constants.RETRY_COUNT;
        boolean isNicConnected = RetryUtils.retry(() -> {
            boolean nicConnected = vmOperations.isNicConnected(client, vmId, nicId);
            if (!nicConnected) {
                throw new MangleException("Nic disconnection is still in progress");
            }
            return true;
        }, new MangleException("Timeout waiting for response"), retryCount, 5);

        if (isNicConnected) {
            String message = String.format(ErrorConstants.VM_CONNECT_NIC_SUCCESSFUL, nicId, vmId);
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.COMPLETED.toString(),
                    message);
            log.info(message);
        } else {
            vCenterOperationsTaskStore.updateTaskStatus(taskId, VCenterOperationsTaskStatus.TIME_OUT.toString(), null);
            log.info("VM connect nic operation couldn't be verified on VM {}, timed out waiting for the nic status",
                    vmId);
        }
        client.terminateConnection();
    }
}
