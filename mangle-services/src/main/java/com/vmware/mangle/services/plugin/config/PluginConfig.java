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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.extern.log4j.Log4j2;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Plugin Config class.
 *
 * @author kumargautam
 */
@Configuration
@Log4j2
public class PluginConfig {

    private SpringPluginManager pluginManager;

    @Autowired
    public PluginConfig(SpringPluginManager pm) {
        log.info("Plugin Config Start...");
        this.pluginManager = pm;
    }

    @PostConstruct
    public void init() {
        log.info("Plugin init Start...");
    }

    @PreDestroy
    public void cleanup() {
        log.info("Plugin cleanup Start...");
        pluginManager.stopPlugins();
    }
}
