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

package com.vmware.mangle.controller;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.model.VM;
import com.vmware.mangle.model.VMDisk;
import com.vmware.mangle.model.VMDiskRequestBody;
import com.vmware.mangle.model.VMNic;
import com.vmware.mangle.model.resource.VCenterTaskTriggeredResponse;
import com.vmware.mangle.service.VCenterOperationsTaskStore;
import com.vmware.mangle.service.VMOperationsTaskService;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author Chethan C(chetanc)
 *
 *         provides endpoints for the vcenter fault injection all the api calls are delegated to
 *         VMOperations wrapper, which makes each call async, creating a new thread each call Each
 *         method returns a task id, that can used to query for the task status
 */

@Api(tags = "VCenter VM Operations", description = "Perform VM operations on VCenter")
@RestController
@RequestMapping("/api/v1/vcenter")
@Log4j2
public class VCenterVMController {

    private VMOperationsTaskService vmOperationsTaskService;
    private VCenterOperationsTaskStore vCenterOperationsTaskStore;

    @Autowired
    public VCenterVMController(VCenterOperationsTaskStore vCenterOperationsTaskStore,
            VMOperationsTaskService vmOperationsTaskService) {
        this.vmOperationsTaskService = vmOperationsTaskService;
        this.vCenterOperationsTaskStore = vCenterOperationsTaskStore;
    }

    /**
     * rest endpoint to fetch all the VM based on the given set of filters on a VCenter, identified by
     * the vCenterSpec
     *
     * @param vCenterSpec
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    @PostMapping(value = "/vm", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Get list of VMs matching the given filter")
    public ResponseEntity<List<VM>> getVM(@RequestBody VCenterSpec vCenterSpec,
            @RequestParam(value = "dcName", required = false) String dcName,
            @RequestParam(value = "clusterName", required = false) String clusterName,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "resourcePoolName", required = false) String resourcePoolName)
            throws MangleException {
        log.debug(String.format("Triggering get all VM from the VCenter %s", vCenterSpec.getVcServerUrl()));
        List<VM> vms =
                this.vmOperationsTaskService.getVMs(host, clusterName, dcName, folder, resourcePoolName, vCenterSpec);

        return new ResponseEntity<>(vms, HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @PostMapping(value = "/vm/{vmId}/nic", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Get list of NICs attached to a VM")
    public ResponseEntity<List<VMNic>> getVMNics(@RequestBody VCenterSpec vCenterSpec,
            @PathVariable(value = "vmId", required = false) String vmId) throws MangleException {
        log.debug("Triggering get all VM Nic from VM with Id {} from the VCenter {}", vmId,
                vCenterSpec.getVcServerUrl());
        List<VMNic> vmNics = this.vmOperationsTaskService.getVMNics(vmId, vCenterSpec);

        return new ResponseEntity<>(vmNics, HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @PostMapping(value = "/vm/{vmId}/disk", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Get list of Disks attached to a VM")
    public ResponseEntity<List<VMDisk>> getVMDisks(@RequestBody VCenterSpec vCenterSpec,
            @PathVariable(value = "vmId", required = false) String vmId) throws MangleException {
        log.debug("Triggering get all VM Disk for VM with Id {} from the VCenter {}", vmId,
                vCenterSpec.getVcServerUrl());
        List<VMDisk> vmDisks = this.vmOperationsTaskService.getVMDisks(vmId, vCenterSpec);

        return new ResponseEntity<>(vmDisks, HttpStatus.OK);
    }

    /**
     * rest endpoint to trigger VM power off operation on the vCenter, specified by the vCenterSpec
     *
     * @param vCenterSpec
     * @throws MangleException
     *             : if authentication fails, vm doesn't exist, vm already turned off
     * @return: task id
     */
    @SuppressWarnings("unchecked")
    @PostMapping(value = "/vm/{vmName}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Get list of VMs matching the given filter")
    public ResponseEntity<List<VM>> getVMByName(@RequestBody VCenterSpec vCenterSpec,
            @PathVariable(value = "vmName", required = true) String vmName,
            @RequestParam(value = "dcName", required = false) String dcName,
            @RequestParam(value = "clusterName", required = false) String clusterName,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "resourcePoolName", required = false) String resourcePoolName)
            throws MangleException {
        log.debug(String.format("Triggering get all VM from the VCenter %s", vCenterSpec.getVcServerUrl()));
        List<VM> vms = this.vmOperationsTaskService.getVMs(vmName, host, clusterName, dcName, folder, resourcePoolName,
                vCenterSpec);

        return new ResponseEntity<>(vms, HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @PostMapping(value = "/vm/{vmName}/poweroff", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Power off the VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> shutDownVMByName(@PathVariable String vmName,
            @RequestBody VCenterSpec vCenterSpec, @RequestParam(value = "dcName", required = false) String dcName,
            @RequestParam(value = "clusterName", required = false) String clusterName,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "resourcePoolName", required = false) String resourcePoolName)
            throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        log.debug(String.format("Triggering power off of the VM %s on the VCenter %s", vmName,
                vCenterSpec.getVcServerUrl()));
        this.vmOperationsTaskService.powerOffVM(vmName, taskId, host, clusterName, dcName, folder, resourcePoolName,
                vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @PostMapping(value = "/vm/id/{vmId}/poweroff", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Power off the VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> shutDownVMById(@PathVariable String vmId,
            @RequestBody VCenterSpec vCenterSpec) throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        log.debug(String.format("Triggering power off of the VM %s on the VCenter %s", vmId,
                vCenterSpec.getVcServerUrl()));
        this.vmOperationsTaskService.powerOffVMById(vmId, taskId, vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    /**
     * rest endpoint to trigger VM power on operation on the vCenter, specified by the vCenterSpec
     *
     * @param vmName
     * @param vCenterSpec
     * @throws MangleException
     *             : if authentication fails, vm doesn't exist, vm already turned on
     * @return: task id
     */
    @PostMapping(value = "/vm/{vmName}/poweron", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Power on the VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> turnOnVMByName(@PathVariable String vmName,
            @RequestBody VCenterSpec vCenterSpec, @RequestParam(value = "dcName", required = false) String dcName,
            @RequestParam(value = "clusterName", required = false) String clusterName,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "resourcePoolName", required = false) String resourcePoolName)
            throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.powerOnVM(vmName, taskId, host, clusterName, dcName, folder, resourcePoolName,
                vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    @PostMapping(value = "/vm/id/{vmId}/poweron", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Power on the VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> turnOnVMById(@PathVariable String vmId,
            @RequestBody VCenterSpec vCenterSpec) throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.powerOnVMById(vmId, taskId, vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    /**
     * rest endpoint to trigger VM reset operation on the vCenter, specified by the vCenterSpec
     *
     * @param vmName
     * @param vCenterSpec
     * @throws MangleException
     *             : if authentication fails, vm doesn't exist
     * @return: task id
     */
    @PostMapping(value = "/vm/{vmName}/reset", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Reset the VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> resetVMByName(@PathVariable String vmName,
            @RequestBody VCenterSpec vCenterSpec, @RequestParam(value = "dcName", required = false) String dcName,
            @RequestParam(value = "clusterName", required = false) String clusterName,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "resourcePoolName", required = false) String resourcePoolName)
            throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.resetVM(vmName, taskId, host, clusterName, dcName, folder, resourcePoolName,
                vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    @PostMapping(value = "/vm/id/{vmId}/reset", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Reset the VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> resetVMById(@PathVariable String vmId,
            @RequestBody VCenterSpec vCenterSpec) throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.resetVMById(vmId, taskId, vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    /**
     * rest endpoint to trigger suspend VM operation on the vCenter, specified by the vCenterSpec
     *
     * @param vmName
     * @param vCenterSpec
     * @throws MangleException
     *             : if authentication fails, vm doesn't exist, VM is already powered off/suspended
     * @return: task id
     */
    @PostMapping(value = "/vm/{vmName}/suspend", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Suspend the VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> suspendVMByName(@PathVariable String vmName,
            @RequestBody VCenterSpec vCenterSpec, @RequestParam(value = "dcName", required = false) String dcName,
            @RequestParam(value = "clusterName", required = false) String clusterName,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "resourcePoolName", required = false) String resourcePoolName)
            throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.suspendVM(vmName, taskId, host, clusterName, dcName, folder, resourcePoolName,
                vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    @PostMapping(value = "/vm/id/{vmId}/suspend", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Suspend the VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> suspendVMById(@PathVariable String vmId,
            @RequestBody VCenterSpec vCenterSpec) throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.suspendVMById(vmId, taskId, vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    /**
     * rest endpoint to trigger disconnect disk operation for the given VM, on the given vCenter,
     * specified by the vCenterSpec
     *
     * @param vmName
     * @param diskId
     * @param vCenterSpec
     * @return task id
     * @throws MangleException
     *             : if authentication fails, vm doesn't exist, VM is already powered off/suspended,
     *             disk doesn't exist
     */
    @PostMapping(value = "/vm/{vmName}/disk/{diskId}/disconnect", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Disconnect disk from VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> deleteDiskFromVM(@PathVariable String vmName,
            @PathVariable String diskId, @RequestBody VCenterSpec vCenterSpec,
            @RequestParam(value = "dcName", required = false) String dcName,
            @RequestParam(value = "clusterName", required = false) String clusterName,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "resourcePoolName", required = false) String resourcePoolName)
            throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.deleteDiskFromVM(vmName, diskId, taskId, host, clusterName, dcName, folder,
                resourcePoolName, vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    @PostMapping(value = "/vm/id/{vmId}/disk/{diskId}/disconnect", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Disconnect disk from VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> deleteDiskFromVM(@PathVariable String vmId,
            @PathVariable String diskId, @RequestBody VCenterSpec vCenterSpec) throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.deleteDiskFromVMById(vmId, diskId, taskId, vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);

    }

    /**
     * rest endpoint to trigger connect nic to VM
     *
     * @param vmName
     * @return task id
     * @throws MangleException
     *             : if authentication fails, VM doesn't exist, nic doesn't exist
     */
    @PostMapping(value = "/vm/{vmName}/disk/{diskId}/connect", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Connect disk to VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> connectDiskToVM(@PathVariable String vmName,
            @PathVariable String diskId, @RequestBody VMDiskRequestBody vmDiskRequestBody,
            @RequestParam(value = "dcName", required = false) String dcName,
            @RequestParam(value = "clusterName", required = false) String clusterName,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "resourcePoolName", required = false) String resourcePoolName)
            throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.addDiskToVM(vmName, vmDiskRequestBody.getDiskspec(), diskId, taskId, host,
                clusterName, dcName, folder, resourcePoolName, vmDiskRequestBody.getVcenterspec());
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    @PostMapping(value = "/vm/id/{vmId}/disk/{diskId}/connect", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Connect disk to VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> connectDiskToVM(@PathVariable String vmId,
            @PathVariable String diskId, @RequestBody VMDiskRequestBody vmDiskRequestBody) throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.addDiskToVMById(vmId, vmDiskRequestBody.getDiskspec(), diskId, taskId,
                vmDiskRequestBody.getVcenterspec());
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    /**
     * rest endpoint to trigger disconnect NIC operation for the given VM, on the given vCenter,
     * specified by the vCenterSpec
     *
     * @param vmName
     * @param nicId
     * @param vCenterSpec
     * @return task id
     * @throws MangleException
     *             : if authentication fails, vm doesn't exist, VM is already powered off/suspended, nic
     *             doesn't exist
     */
    @PostMapping(value = "/vm/{vmName}/nic/{nicId}/disconnect", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Disconnect NIC from VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> disconnectNicFromVM(@PathVariable String vmName,
            @PathVariable String nicId, @RequestBody VCenterSpec vCenterSpec,
            @RequestParam(value = "dcName", required = false) String dcName,
            @RequestParam(value = "clusterName", required = false) String clusterName,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "resourcePoolName", required = false) String resourcePoolName)
            throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.disconnectNicFromVM(vmName, nicId, taskId, host, clusterName, dcName, folder,
                resourcePoolName, vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    @PostMapping(value = "/vm/id/{vmId}/nic/{nicId}/disconnect", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Disconnect NIC from VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> disconnectNicFromVM(@PathVariable String vmId,
            @PathVariable String nicId, @RequestBody VCenterSpec vCenterSpec) throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.disconnectNicFromVMById(vmId, nicId, taskId, vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    /**
     * rest endpoint to trigger connect nic to VM
     *
     * @param vmName
     * @param nicId
     * @param vCenterSpec
     * @return task id
     * @throws MangleException
     *             : if authentication fails, VM doesn't exist, nic doesn't exist
     */
    @PostMapping(value = "/vm/{vmName}/nic/{nicId}/connect", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Connect NIC from VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> connectNicToVM(@PathVariable String vmName,
            @PathVariable String nicId, @RequestBody VCenterSpec vCenterSpec,
            @RequestParam(value = "dcName", required = false) String dcName,
            @RequestParam(value = "clusterName", required = false) String clusterName,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "resourcePoolName", required = false) String resourcePoolName)
            throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.connectNicToVM(vmName, nicId, taskId, host, clusterName, dcName, folder,
                resourcePoolName, vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    @PostMapping(value = "/vm/id/{vmId}/nic/{nicId}/connect", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Connect NIC from VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> connectNicToVM(@PathVariable String vmId,
            @PathVariable String nicId, @RequestBody VCenterSpec vCenterSpec) throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.connectNicToVM(vmId, nicId, taskId, vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }
}
