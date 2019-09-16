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

package com.vmware.mangle.services.plugin.config;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vmware.mangle.services.FileStorageService;
import com.vmware.mangle.services.constants.CommonConstants;

/**
 * SpringConfiguration class for plugin.
 *
 * @author kumargautam
 */
@Configuration
@EnableConfigurationProperties({ PluginProperties.class })
public class SpringConfiguration {

    @Bean(name = "springPluginManager")
    public SpringPluginManager pluginManager(FileStorageService storageService, PluginProperties pluginProperties) {
        System.setProperty(CommonConstants.PF4J_PLUGINS_DIR, storageService.getFileStorageLocation().toString());
        System.setProperty(CommonConstants.PF4J_MODE, pluginProperties.getMode());//deployment or development
        storageService.createDefaultFile();
        return new SpringPluginManager(storageService.getFileStorageLocation());
    }
}
