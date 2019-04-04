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

import java.util.UUID;

import com.vmware.mangle.cassandra.model.security.UserAuthentication;

/**
 *
 *
 * @author chetanc
 */
public class UserAuthenticationServiceMockData {

    private String username1 = "dummy_user1";
    private String username2 = "dummy_user2";
    private String password1 = UUID.randomUUID().toString();
    private String password2 = UUID.randomUUID().toString();
    private String id1 = UUID.randomUUID().toString();
    private String id2 = UUID.randomUUID().toString();

    public UserAuthentication getDummyUser1() {
        UserAuthentication user = new UserAuthentication();
        user.setId(id1);
        user.setUsername(username1);
        user.setPassword(password1);
        return user;
    }

    public UserAuthentication getDummyUser2() {
        UserAuthentication user = new UserAuthentication();
        user.setId(id2);
        user.setUsername(username2);
        user.setPassword(password2);
        return user;
    }


}
