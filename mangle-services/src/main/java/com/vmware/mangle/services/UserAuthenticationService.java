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

package com.vmware.mangle.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.cassandra.model.security.UserAuthentication;
import com.vmware.mangle.services.repository.UserAuthenticationRepository;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Acts as an interface between user-authentication controller, and repository
 *
 * @author chetanc
 */
@Log4j2
@Service
public class UserAuthenticationService {

    private UserAuthenticationRepository userAuthenticationRepository;
    private UserService userService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserAuthenticationService(UserAuthenticationRepository repository, UserService userService,
            PasswordEncoder passwordEncoder) {
        this.userAuthenticationRepository = repository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Gets the user object for a given username
     *
     * @param username
     * @return user object
     * @throws MangleException
     *             if the username is empty/null
     */
    public UserAuthentication getUserByUsername(String username) throws MangleException {
        log.info("Retrieving authentication user for the username: {}", username);
        if (!StringUtils.isEmpty(username)) {
            return userAuthenticationRepository.findByUsername(username);
        } else {
            log.error(ErrorConstants.USERNAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.USERNAME);
        }
    }

    public boolean createDefaultRoleForUser(String username) throws MangleException {
        log.info("Creating default local user with the default readonly role for the user {}", username);
        User user = new User();
        user.setName(username);
        Set<Role> userRoles = new HashSet<>();
        Role defaultRole = userService.getDefaultUserRole();
        userRoles.add(defaultRole);
        user.setRoleNames(userRoles.stream().map(Role::getName).collect(Collectors.toSet()));
        user.setRoles(userRoles);
        try {
            userService.createUser(user);
        } catch (MangleException e) {
            log.info("Local user creation failed for username {}", username);
            throw e;
        }
        return true;
    }

    /**
     * Create a user object in the db
     *
     * @param userAuthentication
     *            user object to be saved into db
     * @return saved user object
     */
    public UserAuthentication createUser(UserAuthentication userAuthentication) throws MangleException {
        if (userAuthentication != null) {
            log.info("Creating local user mapping for the user: {}", userAuthentication.getUsername());
            userAuthentication.setPassword(passwordEncoder.encode(userAuthentication.getPassword()));
            UserAuthentication persistedUser = userAuthenticationRepository.save(userAuthentication);
            if (persistedUser != null) {
                try {
                    createDefaultRoleForUser(persistedUser.getUsername());
                } catch (MangleException e) {
                    userAuthenticationRepository.delete(persistedUser);
                    throw e;
                }
            }
            return persistedUser;
        } else {
            log.error(ErrorCode.FIELD_VALUE_EMPTY + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.USERAUTHENTICATION);
        }
    }

    /**
     * Get the list of all the users from the db
     *
     * @return list of users or an empty list
     */
    public List<UserAuthentication> getAllUsers() {
        log.info("Retrieving all the users from the database");
        return userAuthenticationRepository.findAll();
    }

    /**
     * Update the existing user with a new password
     *
     * @param userAuthentication
     * @return updated user from the db
     * @throws MangleException
     *             if the user is not found in db, or if the user information to update is null
     */
    public UserAuthentication updateUser(UserAuthentication userAuthentication) throws MangleException {
        if (userAuthentication != null) {
            log.info("Updating user data for the user: {}", userAuthentication.getUsername());
            UserAuthentication persistantUser = getUserByUsername(userAuthentication.getUsername());
            if (persistantUser != null) {
                persistantUser.setPassword(passwordEncoder.encode(userAuthentication.getPassword()));
                return userAuthenticationRepository.save(persistantUser);
            } else {
                log.info("User information doesn't exist for the username: {}", userAuthentication.getUsername());
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.USERNAME,
                        userAuthentication.getUsername());
            }
        } else {
            log.error(ErrorConstants.USERNAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.USERNAME);
        }
    }

    /**
     * Delete list of usernames from the db
     *
     * @param usernames
     *            of the user that are to be from the db
     * @return set of users that were not able to be deleted from the db
     */
    public Set<String> deleteUsersByUsername(List<String> usernames) {
        log.info("Deleting the users: " + usernames.toString());
        Set<String> persistedUsers = getMultipleUsers(usernames);
        Set<String> usersWithActiveAuthorizations = new HashSet<>();
        Set<String> failedUsers = new HashSet<>(usernames);

        for (String username : usernames) {
            if (persistedUsers.contains(username)) {
                usersWithActiveAuthorizations.add(username);
                persistedUsers.remove(username);
                failedUsers.remove(username);
            }
        }
        Set<String> failedAuthorizationUsers = userService.deleteUsersByNames(usernames);

        failedUsers.retainAll(failedAuthorizationUsers);
        usersWithActiveAuthorizations.removeAll(failedAuthorizationUsers);

        userAuthenticationRepository.deleteByNameIn(usersWithActiveAuthorizations);
        return failedUsers;
    }

    /**
     * Get the list of the users from the db whose names are in the list of names passed
     *
     * @param names
     * @return list of the names that are present in the db
     */
    public Set<String> getMultipleUsers(List<String> names) {
        log.info("Retrieving user information for the following usernames: {}", names.toString());
        List<UserAuthentication> persistedUsers = userAuthenticationRepository.findByNameIn(names);
        Set<String> users = new HashSet<>();
        for (UserAuthentication user : persistedUsers) {
            users.add(user.getUsername());
        }
        return users;
    }

    public String getDefaultDomainName() {
        log.info("Fetching mangle local authentication domain information");
        return Constants.LOCAL_DOMAIN_NAME;
    }
}
