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

package com.vmware.mangle.services.deletionutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.model.enums.MangleScopeEnum;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.repository.RoleRepository;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 *
 *
 */
@Log4j2
@Service
public class RoleDeletionService {

    private RoleRepository roleRepository;
    private UserService userService;

    @Autowired
    public RoleDeletionService(RoleRepository roleRepository, UserService userService) {
        this.roleRepository = roleRepository;
        this.userService = userService;
    }

    /**
     * Deletes the given list of roles from the repository
     *
     * @param roles
     * @throws MangleException
     */
    public DeleteOperationResponse deleteRolesByNames(List<String> roles) throws MangleException {
        log.info("Deleting the roles: " + roles.toString());
        DeleteOperationResponse response = new DeleteOperationResponse();
        Map<String, List<String>> associations = new HashMap<>();
        List<Role> toBeDeletedRoles = new ArrayList<>();

        List<Role> persistedRoles = roleRepository.findByNameIn(roles);
        List<String> roleNames = persistedRoles.stream().map(Role::getName).collect(Collectors.toList());
        roles.removeAll(roleNames);

        if (!roles.isEmpty()) {
            throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.ROLE, roles.toString());
        }

        for (Role role : persistedRoles) {
            if (role.getType() == MangleScopeEnum.MANGLE_DEFAULT) {
                log.error("Deletion of mangle default roles is not supported");
                throw new MangleException(ErrorCode.DEFAULT_ROLE_DELETE);
            }

            List<String> associatedUsers = getUserAssociationForRole(role.getName());
            if (!CollectionUtils.isEmpty(associatedUsers)) {
                associations.put(role.getName(), associatedUsers);
            } else {
                toBeDeletedRoles.add(role);
            }
        }

        if (CollectionUtils.isEmpty(associations)) {
            log.info("Pre-check successful, Deleting following roles: {}", roleNames.toString());
            roleRepository.deleteAll(toBeDeletedRoles);
        } else {
            response.setAssociations(associations);
            response.setResponseMessage(ErrorConstants.ROLE_DELETION_PRE_CONDITION_FAILURE);
        }
        return response;
    }

    private List<String> getUserAssociationForRole(String roleName) {
        List<User> users = userService.getUsersForRole(roleName);
        if (!CollectionUtils.isEmpty(users)) {
            return users.stream().map(User::getName).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
