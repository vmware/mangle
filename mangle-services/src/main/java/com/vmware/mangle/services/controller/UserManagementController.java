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
import java.util.List;
import javax.annotation.PostConstruct;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.model.UserCreationDTO;
import com.vmware.mangle.model.UserPasswordUpdateDTO;
import com.vmware.mangle.model.UserRolesUpdateDTO;
import com.vmware.mangle.services.PasswordResetService;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.utils.constants.Constants;
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

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @Autowired
    public UserManagementController(UserService userService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    @PostConstruct
    public void initializeDefaultPasswordResetStatus() {
        Constants.setDefaultPasswordResetStatus(passwordResetService.readResetStatus());
    }

    /**
     * API to add new user to mangle
     *
     * @param userCreationDTO
     * @return ResponseEntity with Hateoas resource, response code 201
     * @throws MangleException
     *             when role is not found in mangle
     */
    @ApiOperation(value = "API to create User", nickname = "createUser")
    @PostMapping(value = "users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<User>> createUser(@Validated @RequestBody UserCreationDTO userCreationDTO)
            throws MangleException {
        User user = new User();
        user.setName(userCreationDTO.getName());
        user.setPassword(userCreationDTO.getPassword());
        user.setRoleNames(userCreationDTO.getRoleNames());

        log.trace(String.format("Starting execution of createUser for user %s", user.getName()));

        if (userService.getUserByName(user.getName()) != null) {
            throw new MangleException(String.format(ErrorConstants.USER_ALREADY_EXISTS, user.getName()),
                    ErrorCode.USER_ALREADY_EXISTS);
        }
        userService.validateADDetailsForUserCreation(user);
        User persisted = userService.createUser(user);

        Resource<User> userResource = new Resource<>(persisted);
        userResource.add(getSelfLink(), getHateoasLinkForDeleteUser(), getHateoasLinkForGetCurrentUser(),
                getHateoasLinkForUpdateUser(), getHateoasLinkForUpdateUserPassword());

        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    /**
     * API to update the existing mangle user
     *
     * @param userRolesUpdateDTO
     *            new user configuration to be added for the existing user
     * @return
     * @throws MangleException
     */
    @ApiOperation(value = "API to update User", nickname = "updateUser")
    @PutMapping(value = "users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<User>> updateUser(@Validated @RequestBody UserRolesUpdateDTO userRolesUpdateDTO)
            throws MangleException {
        log.trace(String.format("Starting execution of updateUser for the user %s", userRolesUpdateDTO.getName()));

        User persistedUser = userService.getUserByName(userRolesUpdateDTO.getName());
        User user = new User();
        user.setName(userRolesUpdateDTO.getName());
        user.setRoleNames(userRolesUpdateDTO.getRoleNames());
        if (persistedUser != null && !persistedUser.getAccountLocked()) {
            user.setAccountLocked(false);
        }

        User persisted = userService.updateUser(user);

        Resource<User> userResource = new Resource<>(persisted);

        userResource.add(getSelfLink(), getHateoasLinkForCreateUser(), getHateoasLinkForDeleteUser(),
                getHateoasLinkForGetCurrentUser(), getHateoasLinkForUpdateUserPassword());
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
    @ApiOperation(value = "API to update the current user password", nickname = "update password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<User>> updateUserPassword(
            @Validated @RequestBody UserPasswordUpdateDTO userPasswordUpdateDTO) throws MangleException {
        String currentUser = userService.getCurrentUserName();
        log.trace(String.format("Starting execution of updateUserPassword for the user %s", currentUser));

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
    @ApiOperation(value = "API to get all Users", nickname = "getAllUsers")
    @GetMapping(value = "users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<User>> getAllUsers() throws MangleException {
        log.trace("Starting execution of getAllUsers method");
        List<User> users = userService.getAllUsers();

        Resources<User> userResource = new Resources<>(users);
        userResource.add(getSelfLink(), getHateoasLinkForCreateUser(), getHateoasLinkForDeleteUser(),
                getHateoasLinkForGetCurrentUser(), getHateoasLinkForUpdateUser(),
                getHateoasLinkForUpdateUserPassword());

        return new ResponseEntity<>(userResource, HttpStatus.OK);

    }

    /**
     * API to get current local user configured in the mangle
     *
     * @return current user details
     */
    @ApiOperation(value = "API to get current User", nickname = "getCurrentUser")
    @GetMapping(value = "user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<User>> getCurrentUser() throws MangleException {
        log.info("Starting execution of getAllUsers method");
        User user = userService.getCurrentUser();

        Resource<User> userResource = new Resource<>(user);
        userResource.add(getSelfLink(), getHateoasLinkForCreateUser(), getHateoasLinkForDeleteUser(),
                getHateoasLinkForUpdateUser(), getHateoasLinkForUpdateUserPassword());

        return new ResponseEntity<>(userResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to delete Users by its names", nickname = "deleteUsersByNames")
    @DeleteMapping(value = "users")
    public ResponseEntity<Void> deleteUsersByNames(@RequestParam List<String> usernames) throws MangleException {
        log.trace("Received request to delete users by name for users: {}", usernames);
        userService.deleteUsersByNames(usernames);
        userService.triggerMultiNodeResync(usernames.toArray(new String[0]));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "users/admin", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Read admin password reset status", nickname = "read-admin-password-status", hidden = true)
    public ResponseEntity<Resource<Boolean>> getAdminPasswordResetStatus() {
        log.trace("Received request to read password reset status for the default user");
        Resource<Boolean> statusResource = new Resource<>(Constants.isDefaultPasswordResetStatus());
        statusResource.add(getSelfLink());

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
    @PutMapping(value = "users/admin", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update admin user creds for the first login", nickname = "update-admin-creds", hidden = true)
    public ResponseEntity<Resource<User>> resetAdminCredsForFirstLogin(@Validated @RequestBody User user)
            throws MangleException {
        log.trace(String.format("Starting execution of updateUser for the user %s", user.getName()));
        User persisted = userService.updateFirstTimePassword(user.getName(), user.getPassword());
        Constants.setDefaultPasswordResetStatus(passwordResetService.updateResetStatus());
        userService.terminateUserSession(user.getName());
        Resource<User> userResource = new Resource<>(persisted);
        userResource.add(getSelfLink());
        return new ResponseEntity<>(userResource, HttpStatus.OK);
    }


    /**
     * API to login to mangle
     *
     * @return true if successfully validated
     */
    @ApiOperation(value = "API to login to mangle", nickname = "login", hidden = true)
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> loginUser() {
        log.trace("Starting execution login");
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    public Link getSelfLink() {
        return new Link(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri().toASCIIString())
                .withSelfRel();
    }

    private Link getHateoasLinkForCreateUser() throws MangleException {
        return linkTo(methodOn(UserManagementController.class).createUser(new UserCreationDTO())).withRel("CREATE");
    }

    private Link getHateoasLinkForUpdateUser() throws MangleException {
        return linkTo(methodOn(UserManagementController.class).updateUser(new UserRolesUpdateDTO())).withRel("UPDATE");
    }

    private Link getHateoasLinkForUpdateUserPassword() throws MangleException {
        return linkTo(methodOn(UserManagementController.class).updateUserPassword(new UserPasswordUpdateDTO()))
                .withRel("UPDATE-PASSWORD");
    }

    private Link getHateoasLinkForGetCurrentUser() throws MangleException {
        return linkTo(methodOn(UserManagementController.class).getAllUsers()).withRel("GET-CURRENT-USER");
    }

    private Link getHateoasLinkForDeleteUser() throws MangleException {
        return linkTo(methodOn(UserManagementController.class).deleteUsersByNames(Collections.emptyList()))
                .withRel("DELETE");
    }

}
