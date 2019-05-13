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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.utils.constants.Constants;

/**
 *
 *
 * @author chetanc
 */
public class UserMockData {
    private static final String USER1 = "DUMMY_USER@" + Constants.LOCAL_DOMAIN_NAME;
    private static final String USER2 = "DUMMY_USER2@" + Constants.LOCAL_DOMAIN_NAME;
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String pwd = UUID.randomUUID().toString();

    private RolesMockData rolesMockData = new RolesMockData();

    public User getMockUser() {
        User user = new User(USER1, pwd, getDummyRole());
        return user;
    }

    public Role getDummyRole() {
        Role role = new Role();
        role.setName(ROLE_ADMIN);
        return role;

    }

    public Role getDummyRole3() {
        Role role = new Role();
        role.setName(ROLE_ADMIN);
        Set<Privilege> privilegeSet = new HashSet<>();
        privilegeSet.add(rolesMockData.getDummyPrivilege());
        privilegeSet.add(rolesMockData.getDummy2Privilege());
        role.setPrivileges(privilegeSet);
        role.setPrivilegeNames(privilegeSet.stream().map(Privilege::getName).collect(Collectors.toSet()));
        return role;

    }

    public User getMockUser2() {
        Role role = new Role();
        role.setName(ROLE_ADMIN);
        User user = new User(USER2, pwd, role);
        return user;
    }


    public User getMockUser3() {
        User user = new User(USER1, pwd, getDummyRole3());
        return user;
    }

    public User getUpdateMockUser() {
        Role role = new Role();
        role.setName(ROLE_USER);
        User user = new User(USER1, pwd, role);
        return user;
    }
}
