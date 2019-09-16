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

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
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

import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.model.UserPasswordUpdateDTO;
import com.vmware.mangle.model.enums.HateoasOperations;
import com.vmware.mangle.services.PasswordResetService;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author chetanc
 */
@RestController
@RequestMapping("rest/api/v1/user-management/")
@Log4j2
public class UserManagementController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    /**
     * API to add new user to mangle
     *
     * @param user
     * @return ResponseEntity with Hateoas resource, response code 201
     * @throws MangleException
     *             when role is not found in mangle
     */
    @PostMapping(value = "users")
    public ResponseEntity<Resource<User>> createUser(@Validated @RequestBody User user) throws MangleException {
        log.info(String.format("Starting execution of createUser for user %s", user.getName()));

        if (userService.getUserByName(user.getName()) != null) {
            throw new MangleException(String.format(ErrorConstants.USER_ALREADY_EXISTS, user.getName()),
                    ErrorCode.USER_ALREADY_EXISTS);
        }
        User persisted = userService.createUser(user);

        Resource<User> userResource = new Resource<>(persisted);
        Link link = linkTo(methodOn(UserManagementController.class).createUser(null)).withSelfRel();
        userResource.add(linkTo(methodOn(UserManagementController.class).updateUser(user))
                .withRel(HateoasOperations.UPDATE.toString()));
        userResource.add(link);

        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    /**
     * API to update the existing mangle user
     *
     * @param user
     *            new user configuration to be added for the existing user
     * @return
     * @throws MangleException
     */
    @PutMapping(value = "users")
    public ResponseEntity<Resource<User>> updateUser(@Validated @RequestBody User user) throws MangleException {
        log.info(String.format("Starting execution of updateUser for the user %s", user.getName()));

        user.setAccountLocked(false);
        User persisted = userService.updateUser(user);

        Resource<User> userResource = new Resource<>(persisted);
        Link link = linkTo(methodOn(UserManagementController.class).updateUser(null)).withSelfRel();
        userResource.add(link);
        userService.terminateUserSession(user.getName());
        userService.triggerMultiNodeResync(user.getName());
        return new ResponseEntity<>(userResource, HttpStatus.OK);
    }

    /**
     * API to update the password of logged in user
     *
     * @param userPasswordUpdateDTO
     *
     * @return
     * @throws MangleException
     */
    @PutMapping(value = "/password")
    @ApiOperation(value = "API to update the current user password", nickname = "update password")
    public ResponseEntity<Resource<User>> updateUserPassword(
            @Validated @RequestBody UserPasswordUpdateDTO userPasswordUpdateDTO) throws MangleException {
        String currentUser = userService.getCurrentUserName();
        log.info(String.format("Starting execution of updateUserPassword for the user %s", currentUser));

        userService.updatePassword(currentUser, userPasswordUpdateDTO.getCurrentPassword(),
                userPasswordUpdateDTO.getNewPassword());
        userService.terminateCurrentSession();

        userService.triggerMultiNodeResync(currentUser);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * API to get all the user configured in the mangle
     *
     * @return List of all the user
     */
    @GetMapping(value = "users")
    public ResponseEntity<Resources<User>> getAllUsers() {
        log.info("Starting execution of getAllUsers method");
        List<User> users = userService.getAllUsers();
        Resources<User> userResource = new Resources<>(users);
        Link link = linkTo(methodOn(UserManagementController.class).getAllUsers()).withSelfRel();
        userResource.add(link);

        return new ResponseEntity<>(userResource, HttpStatus.OK);

    }

    /**
     * API to get current local user configured in the mangle
     *
     * @return current user details
     */
    @GetMapping(value = "user")
    public ResponseEntity<Resource<User>> getCurrentUser() throws MangleException {
        log.info("Starting execution of getAllUsers method");
        User user = userService.getCurrentUser();
        Resource<User> userResource = new Resource<>(user);
        Link link = linkTo(methodOn(UserManagementController.class).getCurrentUser()).withSelfRel();
        userResource.add(linkTo(methodOn(UserManagementController.class).updateUser(user))
                .withRel(HateoasOperations.UPDATE.toString()));
        userResource.add(link);

        return new ResponseEntity<>(userResource, HttpStatus.OK);
    }


    @DeleteMapping(value = "users")
    public ResponseEntity<Void> deleteUsersByNames(@RequestParam List<String> usernames) throws MangleException {
        log.info("Received request to delete users by name for users: {}", usernames);
        userService.deleteUsersByNames(usernames);
        userService.triggerMultiNodeResync(usernames.toArray(new String[0]));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "users/admin")
    @ApiOperation(value = "Read admin password reset status", nickname = "read-admin-password-status", hidden = true)
    public ResponseEntity<Resource<Boolean>> getAdminPasswordResetStatus() {
        log.info("Received request to read password reset status for the default user");
        boolean isReset = passwordResetService.readResetStatus();
        Resource<Boolean> statusResource = new Resource<>(isReset);
        Link link = linkTo(methodOn(UserManagementController.class).getAdminPasswordResetStatus()).withSelfRel();
        statusResource.add(link);
        return new ResponseEntity<>(statusResource, HttpStatus.OK);
    }

    /**
     * API to reset admin password during the first boot
     *
     * @param user
     *            new admin user configuration
     *
     * @return
     * @throws MangleException
     */
    @PutMapping(value = "users/admin")
    @ApiOperation(value = "Update admin user creds for the first login", nickname = "update-admin-creds", hidden = true)
    public ResponseEntity<Resource<User>> resetAdminCredsForFirstLogin(@Validated @RequestBody User user)
            throws MangleException {
        log.info(String.format("Starting execution of updateUser for the user %s", user.getName()));

        User persisted = userService.updateUser(user);
        passwordResetService.updateResetStatus();

        Resource<User> userResource = new Resource<>(persisted);
        Link link = linkTo(methodOn(UserManagementController.class).updateUser(null)).withSelfRel();
        userResource.add(link);

        return new ResponseEntity<>(userResource, HttpStatus.OK);
    }

}
