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

package com.vmware.mangle.adapter;

import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_VM_DISK;
import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_VM_DISK_OBJ;
import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_VM_NETWORK;
import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_VM_POWER;
import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_VM_POWER_STATE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.vmware.mangle.model.ResourceObject;
import com.vmware.mangle.model.VCenterVMNic;
import com.vmware.mangle.model.VCenterVMObject;
import com.vmware.mangle.model.VCenterVMState;
import com.vmware.mangle.model.VMDisk;
import com.vmware.mangle.model.VMStates;
import com.vmware.mangle.model.response.VMOperationsRepsonse;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.status.VCenterVmNicStatus;

/**
 * @author Chethan C(chetanc)
 *
 *         Provides the methods for orchestrating an operation on VM identified by vmname, of a
 *         VCenter identified by the VCenterClient object
 */
public class VMOperations {

    private VMOperations() {
    }

    /**
     * performs poweroff on the VM provided
     *
     * @param client
     * @param vmname
     * @param dcName
     * @throws MangleException
     *             : if VM doesn't exist, or if the VM is already powered off
     * @return: true if power-off is triggered; else false
     */
    @SuppressWarnings("unchecked")
    public static boolean powerOffVM(VCenterClient client, String vmname, String dcName) throws MangleException {
        ResponseEntity<VMOperationsRepsonse> response = (ResponseEntity<VMOperationsRepsonse>) client.post(
                String.format(REST_VC_VM_POWER, InventoryHelper.getVMID(client, vmname, dcName), "stop"), null,
                VMOperationsRepsonse.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return true;
        } else {
            throw new MangleException(response.getBody().getValue().getMessages().get(0).getDefault_message());
        }
    }

    /**
     * performs poweron on the VM provided
     *
     * @param client
     * @param vmname
     * @throws MangleException
     *             : if VM doesn't exist, or if the VM is already powered on
     * @return: true if power-on is triggered; else false
     */
    @SuppressWarnings("unchecked")
    public static boolean powerOnVM(VCenterClient client, String vmname) throws MangleException {
        ResponseEntity<VMOperationsRepsonse> response = (ResponseEntity<VMOperationsRepsonse>) client.post(
                String.format(REST_VC_VM_POWER, InventoryHelper.getVMID(client, vmname, null), "start"), null,
                VMOperationsRepsonse.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return true;
        } else {
            throw new MangleException(response.getBody().getValue().getMessages().get(0).getDefault_message());
        }
    }

    /**
     * performs reset off the VM provided
     *
     * @param client
     * @param vmname
     * @throws MangleException
     *             : if VM doesn't exist
     * @return: true if reset is triggered; else false
     */
    @SuppressWarnings("unchecked")
    public static boolean resetVM(VCenterClient client, String vmname) throws MangleException {
        ResponseEntity<VMOperationsRepsonse> response = (ResponseEntity<VMOperationsRepsonse>) client.post(
                String.format(REST_VC_VM_POWER, InventoryHelper.getVMID(client, vmname, null), "reset"), null,
                VMOperationsRepsonse.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return true;
        } else {
            throw new MangleException(response.getBody().getValue().getMessages().get(0).getDefault_message());
        }
    }

    /**
     * suspends VM
     *
     * @param client
     * @param vmname
     * @throws MangleException
     *             : if VM doesn't exist or if the VM is already powered off
     * @return: true if suspend is triggered; else false
     */
    @SuppressWarnings("unchecked")
    public static boolean suspendVM(VCenterClient client, String vmname) throws MangleException {
        ResponseEntity<VMOperationsRepsonse> response = (ResponseEntity<VMOperationsRepsonse>) client.post(
                String.format(REST_VC_VM_POWER, InventoryHelper.getVMID(client, vmname, null), "suspend"), null,
                VMOperationsRepsonse.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return true;
        } else {
            throw new MangleException(response.getBody().getValue().getMessages().get(0).getDefault_message());
        }
    }

    /**
     * Detaches the Disk from the VM This operation doesn't delete the disk, but removes it from the
     * VM, one can attach the disk given disk spec to addDisk endpoint
     *
     * @param client
     * @param diskID
     * @param vmname
     * @throws MangleException
     *             : if disk is not found, or if VM doesn't exist
     * @return: true if disk deletion is triggered; else false
     */
    @SuppressWarnings("unchecked")
    public static boolean deleteDiskFromVM(VCenterClient client, String diskID, String vmname) throws MangleException {

        ResponseEntity<VMOperationsRepsonse> response = (ResponseEntity<VMOperationsRepsonse>) client.delete(
                String.format(REST_VC_VM_DISK, InventoryHelper.getVMID(client, vmname, null)) + "/" + diskID,
                VMOperationsRepsonse.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return true;
        } else {
            throw new MangleException(String.format(ErrorConstants.VC_DEVICE_NOT_FOUND_ERROR, diskID, vmname));
        }
    }

    /**
     * Adds the disk to the VM, according to the given spec, if it doesn't exist, VCenter creates a
     * new spec and adds the same to the VM
     *
     * @param client
     * @param vmname
     * @return
     * @throws MangleException
     *             : if VM doesn't exist
     */
    @SuppressWarnings("unchecked")
    public static boolean addDiskToVM(VCenterClient client, VMDisk diskSpec, String vmname)
            throws MangleException, JsonProcessingException {
        String diskFinalSpec = "{\"spec\": %s }";
        ObjectMapper mapper = new ObjectMapper();
        ResponseEntity response =
                client.post(String.format(REST_VC_VM_DISK, InventoryHelper.getVMID(client, vmname, null)),
                        String.format(diskFinalSpec, mapper.writeValueAsString(diskSpec)), Object.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return true;
        } else {
            throw new MangleException("Adding disk to the VM failed");
        }

    }

    /**
     * Disconnects the nic from the VM
     *
     * @param client
     * @param nicID
     * @param vmname
     * @throws MangleException
     *             : if the nic is already disconnected, or if the nic is not found, or if VM
     *             doesn't exist
     * @return: true if NIC disconnect operation is triggered, else false
     */
    @SuppressWarnings("unchecked")
    public static boolean disconnectNicFromVM(VCenterClient client, String nicID, String vmname)
            throws MangleException {
        ResponseEntity<VMOperationsRepsonse> response = (ResponseEntity<VMOperationsRepsonse>) client
                .post(String.format(REST_VC_VM_NETWORK, InventoryHelper.getVMID(client, vmname, null)) + "/" + nicID
                        + "/disconnect", null, VMOperationsRepsonse.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return true;
        } else {
            throw new MangleException(response.getBody().getValue().getMessages().get(0).getDefault_message());
        }
    }

    /**
     * connects the NIC corresponding to the given id
     *
     * @param client
     * @param nicID
     * @param vmname
     * @throws MangleException
     *             : if NIC is not for the given NIC id, or if VM doesn't exist
     * @return: true if the connect operation is triggerd on the VM; else false
     */
    @SuppressWarnings("unchecked")
    public static boolean connectNicToVM(VCenterClient client, String nicID, String vmname) throws MangleException {
        ResponseEntity<VMOperationsRepsonse> response = (ResponseEntity<VMOperationsRepsonse>) client
                .post(String.format(REST_VC_VM_NETWORK, InventoryHelper.getVMID(client, vmname, null)) + "/" + nicID
                        + "/connect", null, VMOperationsRepsonse.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return true;
        } else {
            throw new MangleException(response.getBody().getValue().getMessages().get(0).getDefault_message());
        }
    }

    /**
     * check if the given NIC is connected to the VM
     *
     * @param client
     * @param vmname
     * @param nicID
     * @throws MangleException
     *             : if NIC is not found for the given ID, or if VM doesn't exist
     * @return: true if NIC is connected; else false
     */
    @SuppressWarnings("unchecked")
    public static boolean isNicConnected(VCenterClient client, String vmname, String nicID) throws MangleException {
        ResourceObject resourceObject = (ResourceObject) client
                .get(String.format(REST_VC_VM_NETWORK, InventoryHelper.getVMID(client, vmname, null)) + "/" + nicID,
                        ResourceObject.class)
                .getBody();
        VCenterVMNic vmNicState =
                InventoryHelper.convertLinkedHashMapToObject(resourceObject.getValue(), VCenterVMNic.class);
        return VCenterVmNicStatus.STATE_CONNECTED.equals(vmNicState.getState());
    }

    /**
     * check if the given disk is connected to the VM
     *
     * @param client
     * @param vmname
     * @param diskID
     * @throws MangleException
     *             : if disk is not found, or if VM doesn't exist
     * @return: true if the given disk is connected, else fasle
     */
    @SuppressWarnings("unchecked")
    public static boolean isDiskConnected(VCenterClient client, String vmname, String diskID) throws MangleException {
        client.setJsonRequest(null);
        ResponseEntity<VMOperationsRepsonse> response = (ResponseEntity<VMOperationsRepsonse>) client.get(
                String.format(REST_VC_VM_DISK, InventoryHelper.getVMID(client, vmname, null)) + "/" + diskID,
                VMOperationsRepsonse.class);
        return response.getStatusCode().equals(HttpStatus.OK);
    }

    /**
     * checks if the guest os is running on the VM
     *
     * @param client
     * @param vmname
     * @throws MangleException
     *             if VM doesn't exist
     * @return: true if the guest os if up and running; else false
     */
    @SuppressWarnings("unchecked")
    public static String getVMPowerState(VCenterClient client, String vmname) throws MangleException {
        ResourceObject resourceObject = (ResourceObject) client
                .get(String.format(REST_VC_VM_POWER_STATE, InventoryHelper.getVMID(client, vmname, null)),
                        ResourceObject.class)
                .getBody();
        VCenterVMState vCenterVMState =
                InventoryHelper.convertLinkedHashMapToObject(resourceObject.getValue(), VCenterVMState.class);
        return vCenterVMState.getState();
    }

    public static boolean isVMPoweredOn(VCenterClient client, String vmname) throws MangleException {
        return VMStates.POWERED_ON.name().equals(getVMPowerState(client, vmname));
    }

    public static boolean isVMSuspended(VCenterClient client, String vmname) throws MangleException {
        return VMStates.SUSPENDED.name().equals(getVMPowerState(client, vmname));
    }

    /**
     * Retrieve VM Disk Object
     *
     * @param client
     * @param vmname
     * @throws MangleException
     *             if VM doesn't exist
     * @return: disk Object
     */
    @SuppressWarnings("unchecked")
    public static VCenterVMObject getVMDisk(VCenterClient client, String vmname, String diskId) throws MangleException {
        ResourceObject resourceObject = (ResourceObject) client
                .get(String.format(REST_VC_VM_DISK_OBJ, InventoryHelper.getVMID(client, vmname, null), diskId),
                        ResourceObject.class)
                .getBody();
        VMDisk vmDisk = InventoryHelper.convertLinkedHashMapToObject(resourceObject.getValue(), VMDisk.class);
        return vmDisk;
    }

}
