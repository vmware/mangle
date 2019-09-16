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

package com.vmware.mangle.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vmware.mangle.service.CustomerUserDetailsService;

/**
 * @author Chethan C(chetanc)
 */

/**
 * @author chetanc
 *
 *         An AuthenticationProvider implementation that retrieves user details
 *         from a UserDetailsService.
 */
public class UserDaoAuthenticationProvider extends DaoAuthenticationProvider {

    /**
     * Sets the default userDetailsService and passwordEncoder that has to be used
     * for Authentication
     *
     * @param customerUserDetailsService
     * @param passwordEncoder
     */
    @Autowired
    public UserDaoAuthenticationProvider(CustomerUserDetailsService customerUserDetailsService,
            PasswordEncoder passwordEncoder) {
        this.setUserDetailsService(customerUserDetailsService);
        this.setPasswordEncoder(passwordEncoder);
    }
}
