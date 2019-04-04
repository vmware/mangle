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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.cassandra.model.security.UsernameDomain;
import com.vmware.mangle.model.enums.DefaultRoles;
import com.vmware.mangle.services.repository.UserRepository;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author chetanc
 */
@Service
@Log4j2
public class UserService {

    UserRepository userRepository;

    RoleService roleService;

    PrivilegeService privilegeService;

    ADAuthProviderService authProviderService;

    @Autowired
    public UserService(UserRepository userRepository, PrivilegeService privilegeService,
            ADAuthProviderService adAuthProviderService) {
        this.userRepository = userRepository;
        this.privilegeService = privilegeService;
        this.authProviderService = adAuthProviderService;
    }

    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    public Set<Role> getRoleForUser(String username) {
        log.info("Retrieving role for the user: " + username);
        Set<Role> roles = null;
        User user = userRepository.findByName(username);
        if (user != null) {
            user.setRoles(new HashSet<>(roleService.getRolesByNames(user.getRoleNames())));
            roles = user.getRoles();
            log.info(String.format("Found %d roles for user %s", roles.size(), username));
            log.debug(String.format("Roles for user %s are: %s", username, roles.toString()));
        } else {
            log.info(String.format("User %s does not have any rules defined", username));
        }
        return roles;
    }

    public User getUserByName(String username) {
        log.info("Retrieving User for the username: " + username);
        User user = userRepository.findByName(username);
        if (user != null) {
            user.setRoles(new HashSet<>(roleService.getRolesByNames(user.getRoleNames())));
        }
        return user;
    }

    public User updateUser(User user) throws MangleException {
        UsernameDomain usernameDomain = extractUserAndDomain(user.getName());

        if (CollectionUtils.isEmpty(user.getRoleNames())
                && Constants.LOCAL_DOMAIN_NAME.equals(usernameDomain.getDomain())) {
            log.error(String.format(ErrorConstants.LOCAL_USER_EMPTY_ROLE_UPDATE_FAIL, user.getName()));
            throw new MangleException(ErrorConstants.LOCAL_USER_EMPTY_ROLE_UPDATE_FAIL,
                    ErrorCode.LOCAL_USER_EMPTY_ROLE_UPDATE_FAIL, user.getName());
        } else if (authProviderService.getAllDomains().contains(usernameDomain.getDomain())
                && CollectionUtils.isEmpty(user.getRoleNames())) {
            deleteUserByName(user.getName());
            return null;
        }

        User dbUser = getUserByName(user.getName());

        if (dbUser == null) {
            log.error(String.format("Failed to find the user %s, execution of the updateUser failed", user.getName()));
            throw new MangleException(ErrorConstants.USER_NOT_FOUND, ErrorCode.USER_NOT_FOUND, user.getName());
        }

        dbUser.setRoleNames(user.getRoleNames());
        return createUser(dbUser);
    }

    public User createUser(User user) throws MangleException {
        log.info("Creating user " + user.getName());
        UsernameDomain usernameDomain = extractUserAndDomain(user.getName());
        if (usernameDomain.getDomain() == null || (!usernameDomain.getDomain().equals(Constants.LOCAL_DOMAIN_NAME)
                && !authProviderService.getAllDomains().contains(usernameDomain.getDomain()))) {
            throw new MangleException(ErrorConstants.INVALID_DOMAIN_NAME, ErrorCode.INVALID_DOMAIN_NAME);
        }

        if (CollectionUtils.isEmpty(user.getRoleNames())) {
            throw new MangleException(ErrorConstants.USER_EMPTY_ROLE_CREATE_FAIL, ErrorCode.USER_NOT_FOUND,
                    user.getName());
        }

        user.setRoles(getPersitentRoles(user.getRoleNames()));

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        log.info("Retrieving all the users");
        return userRepository.findAll();
    }

    public String getCurrentUserName() {
        Authentication authentication = getCurrentAuthentication();
        String currentUser = null;
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                currentUser = ((UserDetails) principal).getUsername();
            } else {
                currentUser = principal.toString();
            }
        }
        return currentUser;
    }

    public User getCurrentUser() {
        String currentUser = getCurrentUserName();
        return userRepository.findByName(currentUser);
    }

    public Set<String> deleteUsersByNames(List<String> usernames) {
        String currentUser = getCurrentUserName();
        log.info("Deleting the users: " + usernames.toString());
        Set<String> persistedUsers = getMultipleUsers(usernames);
        Set<String> toBeDeletedUsers = new HashSet<>();
        Set<String> failedUsers = new HashSet<>(usernames);

        for (String username : usernames) {
            if (persistedUsers.contains(username) && !username.equals(currentUser)) {
                toBeDeletedUsers.add(username);
                persistedUsers.remove(username);
                failedUsers.remove(username);
            }
        }
        userRepository.deleteByNameIn(toBeDeletedUsers);
        return failedUsers;
    }

    public void deleteUserByName(String username) throws MangleException {
        log.info("Deleting the user: " + username);
        User user = userRepository.findByName(username);
        if (user != null) {
            userRepository.delete(user);
        } else {
            log.error(String.format("User %s not found", username));
            throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.USER_NAME, username);
        }

    }

    public Set<Role> getPersitentRoles(Set<String> roles) throws MangleException {
        Set<Role> persistentRoles = new HashSet<>();
        for (String role : roles) {
            Role dbRole = roleService.getRoleByName(role);
            if (dbRole != null) {
                persistentRoles.add(dbRole);
            } else {
                log.error(String.format("Failed to find the role %s, execution of the createUser failed", role));
                throw new MangleException(ErrorConstants.ROLE_NOT_FOUND, ErrorCode.ROLE_NOT_FOUND, role);
            }
        }
        return persistentRoles;
    }

    public Role getDefaultUserRole() {
        return roleService.getRoleByName(DefaultRoles.ROLE_READONLY.name());
    }

    public Set<String> getMultipleUsers(List<String> names) {
        List<User> persistedUsers = userRepository.findByNameIn(names);
        Set<String> users = new HashSet<>();
        for (User user : persistedUsers) {
            users.add(user.getName());
        }
        return users;
    }

    public List<Privilege> getPrivilegeForUser(String username) {
        Set<Role> roles = getRoleForUser(username);
        List<Privilege> privileges = new ArrayList<>();
        for (Role role : roles) {
            privileges.addAll(privilegeService.getPrivilegeByNames(role.getPrivilegeNames()));
        }
        return privileges;
    }

    public Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }


    /**
     * Decodes the header into a username and password.
     *
     * @throws BadCredentialsException
     *             if the Basic header is not present or is not valid Base64
     */
    private UsernameDomain extractUserAndDomain(String userAndDomain) {
        int delim = userAndDomain.indexOf('@');
        if (delim == -1) {
            return new UsernameDomain(userAndDomain, null);
        }
        String username = userAndDomain.substring(0, delim);
        String domain = userAndDomain.substring(delim + 1);
        return new UsernameDomain(username, domain);
    }

    public List<User> getUsersForRole(String role) {
        return userRepository.findByRole(role);
    }
}
