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

package com.vmware.mangle.unittest.services.config;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.services.config.CommonConfig;

/**
 * @author chetanc
 *
 *
 */
public class CommonConfigTest {

    CommonConfig config = new CommonConfig();

    @Test
    public void testThreadPoolTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = config.threadPoolTaskScheduler();
        Assert.assertEquals(taskScheduler.getPoolSize(), 5);
        Assert.assertEquals(taskScheduler.getThreadNamePrefix(), "MangleThreadPoolTaskScheduler");
    }

    @Test
    public void testPasswordEncoder() {
        PasswordEncoder passwordEncoder = config.passwordEncoder();
        Assert.assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }
}
