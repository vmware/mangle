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

import com.vmware.mangle.model.Host;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.model.resource.VCenterTaskTriggeredResponse;
import com.vmware.mangle.service.HostOperationsService;
import com.vmware.mangle.service.VCenterOperationsTaskStore;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */
@Api(tags = "VCenter Host Operations")
@RestController
@RequestMapping("api/v1/vcenter/host")
@Log4j2
public class VCenterHostController {

    private HostOperationsService hostOperationsService;
    private VCenterOperationsTaskStore vCenterOperationsTaskStore;

    @Autowired
    public VCenterHostController(HostOperationsService hostOperationsService,
            VCenterOperationsTaskStore vCenterOperationsTaskStore) {
        this.hostOperationsService = hostOperationsService;
        this.vCenterOperationsTaskStore = vCenterOperationsTaskStore;
    }

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<Host>> getHost(@RequestBody VCenterSpec vCenterSpec,
            @RequestParam(value = "dcName", required = false) String dcName,
            @RequestParam(value = "clusterName", required = false) String clusterName,
            @RequestParam(value = "folder", required = false) String folder) throws MangleException {
        log.debug(String.format("Triggering get all Hosts from the VCenter %s", vCenterSpec.getVcServerUrl()));
        List<Host> hosts = this.hostOperationsService.getHosts(clusterName, dcName, folder, vCenterSpec);

        return new ResponseEntity<>(hosts, HttpStatus.OK);
    }

    @PostMapping(value = "/name", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<Host>> getHost(@RequestBody VCenterSpec vCenterSpec,
            @RequestParam(value = "hostName", required = false) String hostName,
            @RequestParam(value = "dcName", required = false) String dcName,
            @RequestParam(value = "clusterName", required = false) String clusterName,
            @RequestParam(value = "folder", required = false) String folder) throws MangleException {
        log.debug(String.format("Triggering get all Hosts from the VCenter %s", vCenterSpec.getVcServerUrl()));
        List<Host> hosts = this.hostOperationsService.getHosts(hostName, clusterName, dcName, folder, vCenterSpec);

        return new ResponseEntity<>(hosts, HttpStatus.OK);
    }

    @PostMapping(value = "/name/{host}/disconnect", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<VCenterTaskTriggeredResponse> disconnectHost(@RequestBody VCenterSpec vCenterSpec,
            @PathVariable(value = "host", required = false) String host,
            @RequestParam(value = "dcName", required = false) String dcName,
            @RequestParam(value = "clusterName", required = false) String clusterName,
            @RequestParam(value = "folder", required = false) String folder) throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        log.debug(String.format("Triggering disconnect Host from the VCenter %s", vCenterSpec.getVcServerUrl()));
        this.hostOperationsService.disconnectHost(taskId, host, clusterName, dcName, folder, vCenterSpec);

        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    @PostMapping(value = "/name/{host}/connect", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<VCenterTaskTriggeredResponse> connectHost(@RequestBody VCenterSpec vCenterSpec,
            @PathVariable(value = "host", required = false) String host,
            @RequestParam(value = "dcName", required = false) String dcName,
            @RequestParam(value = "clusterName", required = false) String clusterName,
            @RequestParam(value = "folder", required = false) String folder) throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        log.debug(String.format("Triggering connect Host from the VCenter %s", vCenterSpec.getVcServerUrl()));
        this.hostOperationsService.connectHost(taskId, host, clusterName, dcName, folder, vCenterSpec);

        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    @PostMapping(value = "/id/{hostId}/disconnect", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<VCenterTaskTriggeredResponse> disconnectHostById(@RequestBody VCenterSpec vCenterSpec,
            @PathVariable(value = "hostId", required = false) String hostId) throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        log.debug(String.format("Triggering disconnect Host from the VCenter %s", vCenterSpec.getVcServerUrl()));
        this.hostOperationsService.disconnectHostById(taskId, hostId, vCenterSpec);

        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }

    @PostMapping(value = "/id/{hostId}/connect", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<VCenterTaskTriggeredResponse> connectHostById(@RequestBody VCenterSpec vCenterSpec,
            @PathVariable(value = "hostId", required = false) String hostId) throws MangleException {
        String taskId = vCenterOperationsTaskStore.generateTaskId();
        log.debug(String.format("Triggering connect Host from the VCenter %s", vCenterSpec.getVcServerUrl()));
        this.hostOperationsService.connectHostById(taskId, hostId, vCenterSpec);

        return new ResponseEntity<>(new VCenterTaskTriggeredResponse(taskId), HttpStatus.OK);
    }
}
