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
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.cassandra.model.security.UserAuthentication;
import com.vmware.mangle.model.enums.HateoasOperations;
import com.vmware.mangle.services.UserAuthenticationService;
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
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserAuthenticationService userAuthenticationService;

    /**
     * API to add new user to mangle
     *
     * @param user
     * @return ResponseEntity with Hateoas resource, response code 201
     * @throws MangleException
     *             when role is not found in mangle
     */
    @PostMapping(value = "users")
    public ResponseEntity<Resource<User>> createUser(@RequestBody User user) throws MangleException {
        log.info(String.format("Starting execution of createUser for user %s", user.getName()));

        if (user.getName().contains(userAuthenticationService.getDefaultDomainName())) {
            UserAuthentication userAuthentication = userAuthenticationService.getUserByUsername(user.getName());
            if (userAuthentication == null) {
                throw new MangleException(ErrorConstants.AUTHENTICATION_SETUP, ErrorCode.AUTHENTICATION_SETUP,
                        user.getName());
            }
        }


        User persisted = userService.createUser(user);

        Resource<User> userResource = new Resource<>(persisted);
        Link link = linkTo(methodOn(UserController.class).createUser(null)).withSelfRel();
        userResource.add(
                linkTo(methodOn(UserController.class).updateUser(user)).withRel(HateoasOperations.UPDATE.toString()));
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
    public ResponseEntity<Resource<User>> updateUser(@RequestBody User user) throws MangleException {
        log.info(String.format("Starting execution of updateUser for the user %s", user.getName()));

        User persisted = userService.updateUser(user);
        if (null == persisted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        Resource<User> userResource = new Resource<>(persisted);
        Link link = linkTo(methodOn(UserController.class).updateUser(null)).withSelfRel();
        userResource.add(link);

        return new ResponseEntity<>(userResource, HttpStatus.OK);
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
        Link link = linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel();
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
        Link link = linkTo(methodOn(UserController.class).getCurrentUser()).withSelfRel();
        userResource.add(
                linkTo(methodOn(UserController.class).updateUser(user)).withRel(HateoasOperations.UPDATE.toString()));
        userResource.add(link);

        return new ResponseEntity<>(userResource, HttpStatus.OK);
    }
}
