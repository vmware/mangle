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

package com.vmware.mangle.plugin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vmware.mangle.plugin.utils.CustomPluginUtils;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;

/**
 * Mangle Plugin spring Configuration class. Plugin developer needs to Add new beans if any are
 * defined.
 *
 * @author hkilari
 */
@Configuration
public class ManglePluginSpringConfig {

    @Bean
    public CustomPluginUtils pluginUtils() {
        return new CustomPluginUtils();
    }

    @Bean
    public CommandInfoExecutionHelper commandInfoExecutionHelper() {
        return new CommandInfoExecutionHelper();
    }
}