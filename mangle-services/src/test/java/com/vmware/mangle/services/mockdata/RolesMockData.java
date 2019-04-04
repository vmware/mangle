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

package com.vmware.mangle.services.mockdata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.model.enums.MangleScopeEnum;

/**
 *
 *
 * @author chetanc
 */
@Getter
public class RolesMockData {
    private static String dummyRole = "DUMMY_ROLE";
    private static String dummyPrivilege = "READONLY";
    private static String dummy2Privilege = "ADMIN_READ";

    public Role getDummyRole() {

        Set<Privilege> privileges = new HashSet<>();
        privileges.add(getDummyPrivilege());

        Role role = new Role();
        role.setName(dummyRole);
        role.setPrivileges(privileges);
        role.setPrivilegeNames(privileges.stream().map(Privilege::getName).collect(Collectors.toSet()));
        role.setId(UUID.randomUUID().toString());
        role.setType(MangleScopeEnum.MANGLE_ADMIN);
        return role;
    }

    public Role getDummy2Role() {
        Set<Privilege> privileges = new HashSet<>();
        privileges.add(getDummy2Privilege());
        privileges.add(getDummyPrivilege());

        Role role = new Role();
        role.setName(dummyRole);
        role.setPrivileges(privileges);
        role.setPrivilegeNames(privileges.stream().map(Privilege::getName).collect(Collectors.toSet()));
        role.setId(UUID.randomUUID().toString());
        role.setType(MangleScopeEnum.MANGLE_ADMIN);
        return role;
    }

    public Privilege getDummyPrivilege() {
        Privilege privilege = new Privilege(UUID.randomUUID().toString(), dummyPrivilege);
        return privilege;
    }

    public Privilege getDummy2Privilege() {
        Privilege privilege = new Privilege(UUID.randomUUID().toString(), dummy2Privilege);
        return privilege;
    }

    public List<Role> getRoles() {
        List<Role> roles = new ArrayList<>();
        roles.add(getDummyRole());
        return roles;
    }
}
