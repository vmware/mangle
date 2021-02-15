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

package com.vmware.mangle.services.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.cassandra.model.endpoint.VCenterAdapterDetails;
import com.vmware.mangle.services.VCenterAdapterDetailsService;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */
@RestController
@RequestMapping("rest/api/v1/vcenter-adapter-details")
@Log4j2
public class VCenterAdapterDetailsController {

    private VCenterAdapterDetailsService vCenterAdapterDetailsService;

    @Autowired
    public VCenterAdapterDetailsController(VCenterAdapterDetailsService adapterDetailsService) {
        this.vCenterAdapterDetailsService = adapterDetailsService;
    }

    @GetMapping
    public ResponseEntity<Resources<VCenterAdapterDetails>> getAllAdapterDetails() {
        log.trace("Starting execution of the getAllAdapterDetails");
        List<VCenterAdapterDetails> adapterDetailsList = vCenterAdapterDetailsService.getAllVCenterAdapterDetails();

        Resources<VCenterAdapterDetails> adapterDetailsResource = new Resources<>(adapterDetailsList);
        adapterDetailsResource
                .add(linkTo(methodOn(VCenterAdapterDetailsController.class).getAllAdapterDetails()).withSelfRel());

        return new ResponseEntity<>(adapterDetailsResource, HttpStatus.OK);
    }


    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<VCenterAdapterDetails>> createAdapterDetails(
            @Validated @RequestBody VCenterAdapterDetails adapterDetails) throws MangleException {
        log.trace("Starting execution of the createAdapterDetails for {}", adapterDetails.getName());
        VCenterAdapterDetails persistedAdapterDetails =
                vCenterAdapterDetailsService.createVCenterAdapterDetails(adapterDetails);

        Resource<VCenterAdapterDetails> adapterDetailsResource = new Resource<>(persistedAdapterDetails);
        adapterDetailsResource
                .add(linkTo(methodOn(VCenterAdapterDetailsController.class).createAdapterDetails(null)).withSelfRel());

        return new ResponseEntity<>(adapterDetailsResource, HttpStatus.CREATED);
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<VCenterAdapterDetails>> updateAdapterDetails(
            @Validated @RequestBody VCenterAdapterDetails adapterDetails) throws MangleException {
        log.trace("Starting execution of the updateAdapterDetails for {}", adapterDetails.getName());
        VCenterAdapterDetails persistedAdapterDetails =
                vCenterAdapterDetailsService.updateVCenterAdapterDetails(adapterDetails);

        Resource<VCenterAdapterDetails> adapterDetailsResource = new Resource<>(persistedAdapterDetails);
        adapterDetailsResource
                .add(linkTo(methodOn(VCenterAdapterDetailsController.class).updateAdapterDetails(null)).withSelfRel());

        return new ResponseEntity<>(adapterDetailsResource, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAdapterDetails(@RequestParam List<String> adapterDetailsNames) throws MangleException {
        log.trace("Starting the execution of the method deleteAdapterDetails for names: {}", adapterDetailsNames);

        vCenterAdapterDetailsService.deleteVCenterAdapterDetails(adapterDetailsNames);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "/test-connection")
    public ResponseEntity<Resource<Boolean>> testConnection(@RequestBody VCenterAdapterDetails adapterDetails)
            throws MangleException {
        log.trace("Starting execution of the testConnection for adapter {}", adapterDetails.getAdapterUrl());
        boolean isTestConnectionSuccessful = vCenterAdapterDetailsService.testConnection(adapterDetails);
        Resource<Boolean> adapterDetailsResource = new Resource<>(isTestConnectionSuccessful);
        adapterDetailsResource
                .add(linkTo(methodOn(VCenterAdapterDetailsController.class).testConnection(null)).withSelfRel());

        return new ResponseEntity<>(adapterDetailsResource, HttpStatus.OK);
    }

}
