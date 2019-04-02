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
import java.util.Set;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.cassandra.model.security.UserAuthentication;
import com.vmware.mangle.services.PasswordResetService;
import com.vmware.mangle.services.UserAuthenticationService;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author chetanc
 */
@Log4j2
@RestController
@RequestMapping("rest/api/v1/authentication-management/")
public class AuthenticationController {

    private UserAuthenticationService service;
    private PasswordResetService passwordResetService;

    @Autowired
    public AuthenticationController(UserAuthenticationService service, PasswordResetService passwordResetService) {
        this.service = service;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("users")
    public ResponseEntity<Resource<UserAuthentication>> createUser(@RequestBody UserAuthentication userAuthentication)
            throws MangleException {
        UserAuthentication persistedUser = service.createUser(userAuthentication);

        Resource<UserAuthentication> userResource = new Resource<>(persistedUser);
        Link link = linkTo(methodOn(AuthenticationController.class).createUser(null)).withSelfRel();
        userResource.add(link);

        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    @GetMapping("users")
    public ResponseEntity<Resources<UserAuthentication>> getUsers() {
        List<UserAuthentication> users = service.getAllUsers();

        Resources<UserAuthentication> userResource = new Resources<>(users);
        Link link = linkTo(methodOn(AuthenticationController.class).getUsers()).withSelfRel();
        userResource.add(link);

        return new ResponseEntity<>(userResource, HttpStatus.OK);
    }

    @PutMapping("users")
    @ApiOperation(value = "Update a users", nickname = "delete-users-by-name")
    public ResponseEntity<Resource<UserAuthentication>> updateUser(@RequestBody UserAuthentication userAuthentication)
            throws MangleException {
        UserAuthentication user = service.updateUser(userAuthentication);

        Resource<UserAuthentication> userResource = new Resource<>(user);
        Link link = linkTo(methodOn(AuthenticationController.class).updateUser(null)).withSelfRel();
        userResource.add(link);

        return new ResponseEntity<>(userResource, HttpStatus.OK);
    }

    @DeleteMapping("users")
    @ApiOperation(value = "Delete the users", nickname = "delete-users-by-name")
    public ResponseEntity<Resource<String>> deleteUsersByName(@RequestParam List<String> usernames) {
        Set<String> failedUsers = service.deleteUsersByUsername(usernames);

        String response = "";
        if (!failedUsers.isEmpty()) {
            response = String.format(ErrorConstants.USER_DELETION_FAILED, failedUsers.toString());
        }
        Resource<String> userResource = new Resource<>(response);
        Link link = linkTo(methodOn(AuthenticationController.class).deleteUsersByName(usernames)).withSelfRel();
        userResource.add(link);

        return new ResponseEntity<>(userResource, HttpStatus.OK);
    }

    @GetMapping("users/admin")
    @ApiOperation(value = "Read admin password reset status", nickname = "read-admin-password-status")
    public ResponseEntity<Resource<Boolean>> getAdminPasswordResetStatus() {
        log.info("Initiating to read the status of password reset status for the default user");
        boolean isReset = passwordResetService.readResetStatus();
        Resource<Boolean> statusResource = new Resource<>(isReset);
        Link link = linkTo(methodOn(AuthenticationController.class).getAdminPasswordResetStatus()).withSelfRel();
        statusResource.add(link);
        return new ResponseEntity<>(statusResource, HttpStatus.OK);
    }

    @PostMapping("users/admin")
    @ApiOperation(value = "Set admin password reset status", nickname = "reset-admin-password-status")
    public ResponseEntity<Resource<Boolean>> setAdminPasswordResetStatus() {
        log.info("Initiating the setting of the default password reset status to true");
        boolean isReset = passwordResetService.updateResetStatus();
        Resource<Boolean> statusResource = new Resource<>(isReset);
        Link link = linkTo(methodOn(AuthenticationController.class).setAdminPasswordResetStatus()).withSelfRel();
        statusResource.add(link);
        return new ResponseEntity<>(statusResource, HttpStatus.OK);
    }
}
