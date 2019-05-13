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

package com.vmware.mangle.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.vmware.mangle.utils.constants.Constants;

/**
 * @author chetanc
 *
 *         loads default user admin for the application authentication is done only against this
 *         user
 */
@Component
@Log4j2
public class CustomerUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) {
        log.debug("Logging in as user: %s", username);
        if (username.equals(Constants.DEFAULT_USER)) {
            return User.withUsername(Constants.DEFAULT_USER)
                    .password("$2a$10$93B/8pkk.Dl0z8Cfh6EW0uQnUkpjMCjS7sUtB9R0TA2ZthFd6jdtC").roles("USER").build();
        } else {
            return null;
        }
    }
}
