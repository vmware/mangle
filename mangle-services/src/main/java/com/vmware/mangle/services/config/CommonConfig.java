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

package com.vmware.mangle.services.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vmware.mangle.services.commons.ServiceCommonUtils;
import com.vmware.mangle.utils.constants.Constants;

/**
 *
 * @author chetanc
 * @author dbhat
 */
@Configuration
public class CommonConfig {

    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(Constants.DEFAULT_THREAD_POOL_SIZE);
        threadPoolTaskScheduler.setThreadNamePrefix(Constants.DEFAULT_TASK_SCHEDULER_THREAD_POOL_NAME);
        return threadPoolTaskScheduler;
    }

    @PostConstruct
    public void init() {
        ServiceCommonUtils.setApplicationContext(applicationContext);
    }
}
