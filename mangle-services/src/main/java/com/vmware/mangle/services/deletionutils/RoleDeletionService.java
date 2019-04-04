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

        for (String roleName : roles) {
            Role role = getRoleForDeletion(roleName);
            List<String> associatedUsers = getUserAssociationForRole(roleName);
            if (!CollectionUtils.isEmpty(associatedUsers)) {
                associations.put(roleName, associatedUsers);
            } else {
                toBeDeletedRoles.add(role);
            }
        }

        if (CollectionUtils.isEmpty(associations)) {
            roleRepository.deleteAll(toBeDeletedRoles);
        } else {
            response.setAssociations(associations);
            response.setResponseMessage(ErrorConstants.ROLE_DELETION_PRE_CONDITION_FAILURE);
        }
        return response;
    }

    /**
     * Deletes single role matching the roleName
     *
     * @param roleName
     * @throws MangleException:
     *             if mangle_default role is tried to be deleted
     */
    public DeleteOperationResponse deleteRoleByName(String roleName) throws MangleException {
        log.info("Deleting the role: " + roleName);
        DeleteOperationResponse response = new DeleteOperationResponse();
        Map<String, List<String>> associations = new HashMap<>();

        Role role = getRoleForDeletion(roleName);
        List<String> associatedUsers = getUserAssociationForRole(roleName);
        if (!CollectionUtils.isEmpty(associatedUsers)) {
            associations.put(roleName, associatedUsers);
            response.setAssociations(associations);
            response.setResponseMessage(ErrorConstants.ROLE_DELETION_PRE_CONDITION_FAILURE);
        } else {
            roleRepository.delete(role);
        }
        return response;
    }

    private Role getRoleForDeletion(String roleName) throws MangleException {
        Role role = roleRepository.findByName(roleName);
        if (role != null) {
            if (MangleScopeEnum.MANGLE_DEFAULT == role.getType()) {
                log.info("Failed to delete the role, because role is of type mangle_default");
                throw new MangleException(ErrorConstants.DEFAULT_ROLE_DELETE, ErrorCode.DEFAULT_ROLE_DELETE);
            }
        } else {
            log.error(String.format("Role %s not found", roleName));
            throw new MangleException(ErrorConstants.ROLE_NOT_FOUND, ErrorCode.ROLE_NOT_FOUND, roleName);
        }
        return role;
    }

    private List<String> getUserAssociationForRole(String roleName) {
        List<User> users = userService.getUsersForRole(roleName);
        if (!CollectionUtils.isEmpty(users)) {
            return users.stream().map(user -> user.getName()).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
