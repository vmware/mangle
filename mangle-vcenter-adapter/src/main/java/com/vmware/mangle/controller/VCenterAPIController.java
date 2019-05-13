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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.model.VMDiskRequestBody;
import com.vmware.mangle.model.response.VCenterAdapterGeneralReponse;
import com.vmware.mangle.model.response.VCenterTaskTriggeredResponse;
import com.vmware.mangle.service.VMOperationsTaskService;
import com.vmware.mangle.service.VMOperationsTaskStore;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author Chethan C(chetanc)
 *
 *         provides endpoints for the vcenter fault injection all the api calls are delegated to
 *         VMOperations wrapper, which makes each call async, creating a new thread each call Each
 *         method returns a task id, that can used to query for the task status
 */

@Api(tags = "VCenter Operations", description = "Perform VM operations on VCenter")
@RestController
@RequestMapping("/api/v1/vcenter")
@Log4j2
public class VCenterAPIController {
    private static final String VC_SESSION_ID = "vmware-api-session-id";

    @Autowired
    VMOperationsTaskService vmOperationsTaskService;

    /**
     * checks if there exists a valid connection to the vcenter/ checks if the vcenter is healthy
     *
     * @param vCenterSpec:
     *            VC details: ip/hostname, vcUsername, vcPassword
     * @throws MangleException
     * @return: ResponseEntity, defining if the testconnection succeeded or failed
     */
    @RequestMapping(method = RequestMethod.POST, value = "/testconnection")
    @ApiOperation(value = "Test the connection with the VCenter")
    public ResponseEntity testConnection(@RequestBody VCenterSpec vCenterSpec) throws MangleException {
        boolean isSuccess = vmOperationsTaskService.testConnection(vCenterSpec);
        if (isSuccess) {
            log.error("Test connection to the vcenter {} is Successful", vCenterSpec.getVcServerUrl());
            return new ResponseEntity<>(new VCenterAdapterGeneralReponse<>("VCenter Authentication successful", ""),
                    HttpStatus.OK);
        } else {
            log.error("Test connection to the vcenter {} Failed", vCenterSpec.getVcServerUrl());
            return new ResponseEntity<>(new VCenterAdapterGeneralReponse<>("VCenter Authentication Failed", ""),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * rest endpoint to trigger VM power off operation on the vCenter, specified by the vCenterSpec
     *
     * @param vmName
     * @param vCenterSpec
     * @throws MangleException
     *             : if authentication fails, vm doesn't exist, vm already turned off
     * @return: task id
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(method = RequestMethod.POST, value = "/vm/{vmName}/poweroff")
    @ApiOperation(value = "Power off the VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> shutDownVMByName(@PathVariable String vmName,
            @RequestBody VCenterSpec vCenterSpec, @RequestParam(value = "dc", required = false) String dc)
            throws MangleException {
        String taskId = VMOperationsTaskStore.generateTaskId();
        log.debug(String.format("Triggering power off of the VM %s on the VCenter %s", vmName,
                vCenterSpec.getVcServerUrl()));
        this.vmOperationsTaskService.powerOffVM(vmName, taskId, dc, vCenterSpec);
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
    @RequestMapping(method = RequestMethod.POST, value = "/vm/{vmName}/poweron")
    @ApiOperation(value = "Power on the VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> turnOnVMByName(@PathVariable String vmName,
            @RequestBody VCenterSpec vCenterSpec) throws MangleException {
        String taskId = VMOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.powerOnVM(vmName, taskId, vCenterSpec);
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
    @RequestMapping(method = RequestMethod.POST, value = "/vm/{vmName}/reset")
    @ApiOperation(value = "Reset the VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> resetVMByName(@PathVariable String vmName,
            @RequestBody VCenterSpec vCenterSpec) throws MangleException {
        String taskId = VMOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.resetVM(vmName, taskId, vCenterSpec);
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
    @RequestMapping(method = RequestMethod.POST, value = "/vm/{vmName}/suspend")
    @ApiOperation(value = "Suspend the VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> suspendVMByName(@PathVariable String vmName,
            @RequestBody VCenterSpec vCenterSpec) throws MangleException {
        String taskId = VMOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.suspendVM(vmName, taskId, vCenterSpec);
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
    @RequestMapping(method = RequestMethod.POST, value = "/vm/{vmName}/disk/{diskId}/disconnect")
    @ApiOperation(value = "Disconnect disk from VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> deleteDiskFromVM(@PathVariable String vmName,
            @PathVariable String diskId, @RequestBody VCenterSpec vCenterSpec) throws MangleException {
        String taskId = VMOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.deleteDiskFromVM(vmName, diskId, taskId, vCenterSpec);
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
    @RequestMapping(method = RequestMethod.POST, value = "/vm/{vmName}/disk/{diskId}/connect")
    @ApiOperation(value = "Connect disk to VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> connectDiskToVM(@PathVariable String vmName,
            @PathVariable String diskId, @RequestBody VMDiskRequestBody vmDiskRequestBody) throws MangleException {
        String taskId = VMOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.addDiskToVM(vmName, vmDiskRequestBody.getDiskspec(), diskId, taskId,
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
     *             : if authentication fails, vm doesn't exist, VM is already powered off/suspended,
     *             nic doesn't exist
     */
    @RequestMapping(method = RequestMethod.POST, value = "/vm/{vmName}/nic/{nicId}/disconnect")
    @ApiOperation(value = "Disconnect NIC from VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> disconnectNicFromVM(@PathVariable String vmName,
            @PathVariable String nicId, @RequestBody VCenterSpec vCenterSpec) throws MangleException {
        String taskId = VMOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.disonnectNicFromVM(vmName, nicId, taskId, vCenterSpec);
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
    @RequestMapping(method = RequestMethod.POST, value = "/vm/{vmName}/nic/{nicId}/connect")
    @ApiOperation(value = "Connect NIC from VM")
    public ResponseEntity<VCenterTaskTriggeredResponse> connectNicToVM(@PathVariable String vmName,
            @PathVariable String nicId, @RequestBody VCenterSpec vCenterSpec) throws MangleException {
        String taskId = VMOperationsTaskStore.generateTaskId();
        this.vmOperationsTaskService.connectNicToVM(vmName, nicId, taskId, vCenterSpec);
        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }
}
