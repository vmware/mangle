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

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.model.resource.VCenterAdapterGeneralReponse;
import com.vmware.mangle.service.VCenterOperationService;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */
@RequestMapping("/api/v1/vcenter")
@Log4j2
@RestController
public class VCenterController {

    private VCenterOperationService vCenterOperationService;

    @Autowired
    public VCenterController(VCenterOperationService vCenterOperationService) {
        this.vCenterOperationService = vCenterOperationService;
    }

    /**
     * checks if there exists a valid connection to the vcenter/ checks if the vcenter is healthy
     *
     * @param vCenterSpec:
     *            VC details: ip/hostname, vcUsername, vcPassword
     * @throws MangleException
     * @return: ResponseEntity, defining if the testconnection succeeded or failed
     */
    @PostMapping(value = "/testconnection", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Test the connection with the VCenter")
    public ResponseEntity<VCenterAdapterGeneralReponse> testConnection(@RequestBody VCenterSpec vCenterSpec) {
        try {
            boolean isSuccess = vCenterOperationService.testConnection(vCenterSpec);
            if (isSuccess) {
                log.error("Test connection to the vcenter {} is Successful", vCenterSpec.getVcServerUrl());
                return new ResponseEntity<>(new VCenterAdapterGeneralReponse<>("VCenter Authentication successful", ""),
                        HttpStatus.OK);
            } else {
                log.error("Test connection to the vcenter {} Failed", vCenterSpec.getVcServerUrl());
                return new ResponseEntity<>(new VCenterAdapterGeneralReponse<>("VCenter Authentication Failed", ""),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (MangleException e) {
            log.error("Test connection to VCenter failed with exception: {}", e.getMessage());
            return new ResponseEntity<>(new VCenterAdapterGeneralReponse<>(e.getMessage(), ""),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
