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

package com.vmware.mangle.unittest.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.service.CustomerUserDetailsService;

/**
 * @author chetanc
 */

public class CustomerUserDetailsServiceTest {

    CustomerUserDetailsService customerUserDetailsService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String DUMMY_DEFAULT_ADMIN_PASSWORD = "admin";
    private static final String CUSTOM__ADMIN_PASSWORD = "";

    private String ADMIN_USERNAME = "admin";

    @BeforeMethod
    public void initMocks() {
        customerUserDetailsService =
                new CustomerUserDetailsService(passwordEncoder, CUSTOM__ADMIN_PASSWORD, DUMMY_DEFAULT_ADMIN_PASSWORD);
    }

    @Test
    public void testLoadUserByUsername() {
        UserDetails userDetails = customerUserDetailsService.loadUserByUsername(ADMIN_USERNAME);
        Assert.assertEquals(userDetails.getUsername(), "admin");
        Assert.assertEquals(userDetails.getPassword(), DUMMY_DEFAULT_ADMIN_PASSWORD);
    }

    @Test
    public void testNonNullCustomAdminPasswordLoadUserByUsername() {
        customerUserDetailsService = new CustomerUserDetailsService(passwordEncoder, DUMMY_DEFAULT_ADMIN_PASSWORD,
                DUMMY_DEFAULT_ADMIN_PASSWORD);
        UserDetails userDetails = customerUserDetailsService.loadUserByUsername(ADMIN_USERNAME);
        Assert.assertEquals(userDetails.getUsername(), "admin");
        Assert.assertNotEquals(userDetails.getPassword(), DUMMY_DEFAULT_ADMIN_PASSWORD);
        Assert.assertTrue(passwordEncoder.matches(DUMMY_DEFAULT_ADMIN_PASSWORD, userDetails.getPassword()));
    }
}
