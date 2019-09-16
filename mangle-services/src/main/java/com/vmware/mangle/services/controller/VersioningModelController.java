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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import com.vmware.mangle.cassandra.model.Fault;
import com.vmware.mangle.model.FaultV0;
import com.vmware.mangle.services.FaultService;
import com.vmware.mangle.services.MappingService;

/**
 *
 *
 * @author chetanc
 */
@RestController
@RequestMapping(value = "rest/api/")
@ApiIgnore
@Api(description = "Example of versioning of the models")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VersioningModelController {

    private FaultService faultService;
    private MappingService mappingService;

    @Autowired
    public VersioningModelController(FaultService faultService, MappingService mappingService) {
        this.faultService = faultService;
        this.mappingService = mappingService;
    }

    @GetMapping(value = "v2/faults")
    @ApiOperation(value = "Get all the faults")
    public ResponseEntity<Resources<Fault>> getAllFaults() {
        List<Fault> faults = faultService.getAllFaults();

        Resources<Fault> faultResource = new Resources<>(faults);
        Link link = linkTo(methodOn(UserManagementController.class).getAllUsers()).withSelfRel();
        faultResource.add(link);

        return ResponseEntity.ok(faultResource);
    }

    @GetMapping("v1/faults")
    @ApiOperation(value = "Get all v1 faults")
    public ResponseEntity<Resources<FaultV0>> getAllV0Faults() {
        List<Fault> faults = faultService.getAllFaults();
        List<FaultV0> faultV0s = mappingService.map(faults, FaultV0.class);
        Resources<FaultV0> faultResource = new Resources<>(faultV0s);
        Link link = linkTo(methodOn(UserManagementController.class).getAllUsers()).withSelfRel();
        faultResource.add(link);

        return ResponseEntity.ok(faultResource);
    }

    /**
     * @param faultName
     * @return
     */
    @GetMapping("v2/faults/{faultName}")
    @ApiOperation(value = "Get a specific fault matching the given name")
    public ResponseEntity<Resource<Fault>> getFault(@PathVariable String faultName) {
        Fault fault = faultService.getFaultByName(faultName);
        Resource<Fault> faultResource = new Resource<>(fault);
        Link link = linkTo(methodOn(UserManagementController.class).getAllUsers()).withSelfRel();
        faultResource.add(link);

        return ResponseEntity.ok(faultResource);
    }

    @GetMapping("v1/faults/{faultName}")
    @ApiOperation(value = "Get a specific fault matching the given name")
    public ResponseEntity<Resource<FaultV0>> getV0Fault(@PathVariable String faultName) {
        Fault fault = faultService.getFaultByName(faultName);
        FaultV0 faultV0 = mappingService.map(fault, FaultV0.class);
        Resource<FaultV0> faultResource = new Resource<>(faultV0);
        Link link = linkTo(methodOn(UserManagementController.class).getAllUsers()).withSelfRel();
        faultResource.add(link);

        return ResponseEntity.ok(faultResource);
    }

    @PostMapping("v2/faults")
    @ApiOperation(value = "Create a fault")
    public ResponseEntity<Resource<Fault>> createFault(@RequestBody Fault fault) {
        Fault persistantFault = faultService.createFault(fault);

        Resource<Fault> faultResource = new Resource<>(persistantFault);
        Link link = linkTo(methodOn(VersioningModelController.class).createFault(fault)).withSelfRel();
        faultResource.add(link);

        return new ResponseEntity<>(faultResource, HttpStatus.CREATED);
    }

    @PostMapping("v1/faults")
    @ApiOperation(value = "Create a v1 fault")
    public ResponseEntity<Resource<FaultV0>> createV0Fault(@RequestBody FaultV0 faultV0) {
        Fault fault = mappingService.map(faultV0, Fault.class);
        Fault persistantFault = faultService.createFault(fault);
        faultV0 = mappingService.map(persistantFault, FaultV0.class);

        Resource<FaultV0> faultResource = new Resource<>(faultV0);
        Link link = linkTo(methodOn(VersioningModelController.class).createV0Fault(faultV0)).withSelfRel();
        faultResource.add(link);
        return new ResponseEntity<>(faultResource, HttpStatus.CREATED);
    }

    @PostMapping("v2/faults/{faultType}")
    @ApiOperation(value = "Get all the faults of matching type")
    public ResponseEntity<Resources<Fault>> getFaultByType(@PathVariable String faultType) {
        List<Fault> faults = faultService.getFaultsByType(faultType);
        Resources<Fault> faultResource = new Resources<>(faults);
        Link link = linkTo(methodOn(VersioningModelController.class).getFaultByType(faultType)).withSelfRel();
        faultResource.add(link);

        return new ResponseEntity<>(faultResource, HttpStatus.OK);
    }

}
