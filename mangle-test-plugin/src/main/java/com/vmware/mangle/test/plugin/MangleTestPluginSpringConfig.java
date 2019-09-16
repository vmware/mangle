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

package com.vmware.mangle.test.plugin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.test.plugin.helpers.systemresource.CustomDockerSystemResourceFaultHelper;
import com.vmware.mangle.test.plugin.helpers.systemresource.CustomK8sSystemResourceFaultHelper;
import com.vmware.mangle.test.plugin.helpers.systemresource.CustomLinuxSystemResourceFaultHelper;
import com.vmware.mangle.test.plugin.helpers.systemresource.CustomSystemResourceFaultHelperFactory;
import com.vmware.mangle.test.plugin.helpers.systemresource.CustomSystemResourceFaultUtils;
import com.vmware.mangle.test.plugin.utils.CustomPluginUtils;

/**
 * MangleTestPlugin spring Configuration class.
 *
 * @author hkilari
 */
@Configuration
public class MangleTestPluginSpringConfig {

    @Bean
    public EndpointClientFactory endpointClientFactory() {
        return new EndpointClientFactory();
    }

    @Bean
    public CommandInfoExecutionHelper commandInfoExecutionHelper() {
        return new CommandInfoExecutionHelper();
    }

    @Bean
    public CustomSystemResourceFaultUtils systemResourceFaultUtils() {
        return new CustomSystemResourceFaultUtils();
    }

    @Bean
    public CustomLinuxSystemResourceFaultHelper linuxSystemResourceFaultHelper(
            EndpointClientFactory endpointClientFactory, CustomSystemResourceFaultUtils systemResourceFaultUtils) {
        return new CustomLinuxSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils);
    }

    @Bean
    public CustomDockerSystemResourceFaultHelper dockerSystemResourceFaultHelper(
            EndpointClientFactory endpointClientFactory, CustomSystemResourceFaultUtils systemResourceFaultUtils) {
        return new CustomDockerSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils);
    }

    @Bean
    public CustomK8sSystemResourceFaultHelper k8sSystemResourceFaultHelper(EndpointClientFactory endpointClientFactory,
            CustomSystemResourceFaultUtils systemResourceFaultUtils) {
        return new CustomK8sSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils);
    }

    @Bean
    public CustomSystemResourceFaultHelperFactory systemResourceFaultHelperFactory() {
        return new CustomSystemResourceFaultHelperFactory();

    }

    @Bean
    public CustomPluginUtils pluginUtils() {
        return new CustomPluginUtils();
    }
}