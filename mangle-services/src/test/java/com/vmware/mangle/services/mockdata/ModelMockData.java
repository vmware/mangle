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

import com.vmware.mangle.cassandra.model.security.Role;

/**
 *
 *
 * @author chetanc
 */
public class ModelMockData {

    private String roleName1 = "ROLE_DUMMY1";
    private String roleName2 = "ROLE_DUMMY2";

    public Set<Role> getMockRoles() {
        Set<Role> roles = new HashSet<>();
        Role role1 = new Role();
        role1.setName(roleName1);
        Role role2 = new Role();
        role2.setName(roleName2);
        roles.add(role1);
        roles.add(role2);
        return roles;
    }
}
