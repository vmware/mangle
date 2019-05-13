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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.model.enums.HateoasOperations;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.model.response.ErrorDetails;
import com.vmware.mangle.services.PrivilegeService;
import com.vmware.mangle.services.RoleService;
import com.vmware.mangle.services.deletionutils.RoleDeletionService;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author chetanc
 */
@Log4j2
@RestController
@RequestMapping("rest/api/v1/role-management/")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PrivilegeService privilegeService;

    @Autowired
    private RoleDeletionService roleDeletionService;

    /**
     * API to create a custom role in the application
     *
     * @param role
     *            custom role configuration
     * @return created role; HttpStatus 201
     * @throws MangleException
     */
    @ApiOperation(value = "API to create a custom role", nickname = "createRole")
    @PostMapping(value = "roles")
    public ResponseEntity<Resource<Role>> createRole(@RequestBody Role role) throws MangleException {
        log.info(String.format("Starting execution of the createRole for role %s", role.getName()));
        Role persisted = roleService.createRole(role);

        Resource<Role> roleResource = new Resource<>(persisted);
        Link link = linkTo(methodOn(RoleController.class).createRole(null)).withSelfRel();
        roleResource.add(
                linkTo(methodOn(RoleController.class).updateRole(null)).withRel(HateoasOperations.UPDATE.toString()));
        roleResource.add(linkTo(methodOn(RoleController.class).deleteRoles(Collections.singletonList(role.getName())))
                .withRel(HateoasOperations.DELETE.toString()));
        roleResource.add(link);

        return new ResponseEntity<>(roleResource, HttpStatus.CREATED);
    }

    /**
     * API to updated an existing role
     *
     * @param role
     *            to be updated
     * @return updated role
     * @throws MangleException
     *             1. when no role is not found 2. when user tries to delete mangle default role
     */
    @ApiOperation(value = "API to update a role", nickname = "updateRole")
    @PutMapping(value = "roles")
    public ResponseEntity<Resource<Role>> updateRole(@RequestBody Role role) throws MangleException {
        log.info(String.format("Starting execution of the updateRole on the role %s", role.getName()));

        Role persisted = roleService.updateRole(role);

        Resource<Role> roleResource = new Resource<>(persisted);
        Link link = linkTo(methodOn(RoleController.class).createRole(null)).withSelfRel();
        roleResource.add(linkTo(methodOn(RoleController.class).deleteRoles(Collections.singletonList(role.getName())))
                .withRel(HateoasOperations.DELETE.toString()));
        roleResource.add(link);
        return new ResponseEntity<>(roleResource, HttpStatus.OK);
    }

    /**
     * API to get all the roles from the application
     *
     * @return list of all the roles
     */
    @ApiOperation(value = "API to get all the roles", nickname = "getAllRoles")
    @GetMapping(value = "roles")
    public ResponseEntity<Resources<Role>> getAllRoles(
            @RequestParam(name = "names", required = false) List<String> names) {
        log.info("Starting execution of getAllRoles method");
        List<Role> roles;
        Resources<Role> roleResources;
        if (!CollectionUtils.isEmpty(names)) {
            roles = roleService.getRolesByNames(names);
            roleResources = new Resources<>(roles);
            roleResources.add(linkTo(methodOn(RoleController.class).getAllRoles(names)).withSelfRel());
        } else {
            roles = roleService.getAllRoles();
            roleResources = new Resources<>(roles);
            roleResources
                    .add(linkTo(methodOn(RoleController.class).getAllRoles(Collections.emptyList())).withSelfRel());
        }
        return new ResponseEntity<>(roleResources, HttpStatus.OK);
    }

    /**
     * API to get all the privileges configured in the application
     *
     * @return list of the privileges in the application
     */
    @GetMapping(value = "privileges")
    public ResponseEntity<Resources<Privilege>> getPrivileges() {
        log.info("Starting execution of getPrivileges method");

        List<Privilege> privileges = privilegeService.getAllPrivileges();

        Resources<Privilege> privilegesResources = new Resources<>(privileges);
        privilegesResources.add(linkTo(methodOn(RoleController.class).getPrivileges()).withSelfRel());

        return new ResponseEntity<>(privilegesResources, HttpStatus.OK);
    }

    /**
     * API to delete multiple roles on the application
     *
     * @param roles:
     *            list of roles that are to be deleted
     * @return
     * @throws MangleException:
     *             if user tries to delete default role
     */
    @DeleteMapping(value = "roles")
    public ResponseEntity<ErrorDetails> deleteRoles(@RequestParam List<String> roles) throws MangleException {
        log.info(String.format("Starting execution of the roles: %s", roles.toString()));
        DeleteOperationResponse response = roleDeletionService.deleteRolesByNames(roles);
        ErrorDetails errorDetails = new ErrorDetails();
        if (CollectionUtils.isEmpty(response.getAssociations())) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            Map<String, Map<String, List<String>>> associations = new HashMap<>();
            associations.put("associations", response.getAssociations());
            errorDetails.setTimestamp(new Date());
            errorDetails.setDescription(response.getResponseMessage());
            errorDetails.setCode(ErrorCode.DELETE_OPERATION_FAILED.getCode());
            errorDetails.setDetails(associations);
        }

        return new ResponseEntity<>(errorDetails, HttpStatus.PRECONDITION_FAILED);
    }
}
