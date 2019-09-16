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

package com.vmware.mangle.unittest.services.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.services.mockdata.ModelMockData;

/**
 *
 *
 * @author chetanc
 */
public class ModelUserTest {
    private String username = "dummy_user";
    private String pwd = UUID.randomUUID().toString();

    private ModelMockData modelMockData;

    @BeforeTest
    public void init() {
        modelMockData = new ModelMockData();
    }

    @Test
    public void testUserCreationSuccessfull() {
        User user = new User(username, pwd, modelMockData.getMockRoles());
        Assert.assertEquals(2, user.getRoles().size());
        Role role = new Role();
        role.setName("ROLE_DUMMY");
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        Assert.assertEquals(1, user.getRoles().size());
    }
}
