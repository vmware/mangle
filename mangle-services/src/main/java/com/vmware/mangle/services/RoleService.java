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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.model.enums.MangleScopeEnum;
import com.vmware.mangle.services.repository.RoleRepository;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Acts as a wrapper over mongodb repository, and provide some default operations for roles
 *
 * @author chetanc
 */
@Service
@Log4j2
public class RoleService {

    private RoleRepository roleRepository;

    private PrivilegeService privilegeService;

    @Autowired
    public RoleService(RoleRepository roleRepository, PrivilegeService privilegeService) {
        this.roleRepository = roleRepository;
        this.privilegeService = privilegeService;
    }

    /**
     * Retrieves all the roles from the repository
     *
     * @return all the roles
     */
    public List<Role> getAllRoles() {
        log.info("Retrieving all the users");
        return roleRepository.findAll();
    }

    /**
     * Retrieves role for a given rolename
     *
     * @param roleName
     * @return role object from DB
     */
    public Role getRoleByName(String roleName) {
        log.info("Retriving role for the rolename: " + roleName);
        Role role = roleRepository.findByName(roleName);
        if (role != null) {
            role.setPrivileges(new HashSet<>(privilegeService.getPrivilegeByNames(role.getPrivilegeNames())));
        }
        return role;
    }

    /**
     * Retrieves the given list of roleNames from the repository
     *
     * @param roleNames
     * @throws MangleException
     */
    public List<Role> getRolesByNames(Collection<String> roleNames) {
        log.info("Retriving role for the rolename: " + roleNames);
        return roleRepository.findByNameIn(roleNames);
    }

    public Role updateRole(Role role) throws MangleException {
        Role persistedRole = getRoleByName(role.getName());

        if (null == persistedRole) {
            log.error(String.format("Role %s not found", role.getName()));
            throw new MangleException(ErrorConstants.ROLE_NOT_FOUND, ErrorCode.ROLE_NOT_FOUND, role.getName());
        }

        Set<Privilege> persistedPrivilege = getPersitentPrivileges(role.getPrivilegeNames());
        persistedRole.setPrivileges(persistedPrivilege);
        persistedRole
                .setPrivilegeNames(persistedPrivilege.stream().map(Privilege::getName).collect(Collectors.toSet()));

        if (MangleScopeEnum.MANGLE_DEFAULT == persistedRole.getType()) {
            log.error(String.format("Default role %s cannot be deleted", role.getName()));
            throw new MangleException(ErrorConstants.DEFAULT_ROLE_DELETE, ErrorCode.DEFAULT_ROLE_DELETE);
        }

        return roleRepository.save(persistedRole);
    }

    public Role createRole(Role role) throws MangleException {
        log.info("Creating role: " + role.getName());

        Set<Privilege> privileges = getPersitentPrivileges(role.getPrivilegeNames());
        role.setPrivileges(privileges);
        role.setType(MangleScopeEnum.MANGLE_ADMIN);

        return roleRepository.save(role);
    }

    public Set<Privilege> getPersitentPrivileges(Set<String> privileges) throws MangleException {
        Set<Privilege> persistentPrivileges = new HashSet<>();
        for (String privilege : privileges) {
            Privilege persistentPrivilege = privilegeService.getPrivilege(privilege);
            if (persistentPrivilege != null) {
                persistentPrivileges.add(persistentPrivilege);
            } else {
                log.error("Privilege {} not found", privilege);
                throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.PRIVILEGE_NAME, privilege);
            }
        }
        return persistentPrivileges;
    }

}
