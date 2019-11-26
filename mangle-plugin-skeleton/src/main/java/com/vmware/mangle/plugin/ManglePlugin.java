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

import lombok.extern.log4j.Log4j2;

import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Mangle Plugin Initializer class.
 *
 * @author kumargautam
 */
@Log4j2
public class ManglePlugin extends SpringPlugin {

    public ManglePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        //Custom Logic to start plugin goes here.
        log.info("Starting plugin {}", wrapper.getPluginId());
    }

    @Override
    public void stop() {
        //Custom Logic to stop plugin goes here.
        log.info("Stopping plugin {}", wrapper.getPluginId());
    }

    @Override
    protected ApplicationContext createApplicationContext() {
        //Refrain from refactoring, unless you are sure about the Pf4j and Spring Frameworks.
        //Present Implementation take care of Spring Beans Initialization defined in ManglePluginSpringConfig through a new Classloader.
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.setClassLoader(getWrapper().getPluginClassLoader());
        applicationContext.register(ManglePluginSpringConfig.class);
        applicationContext.refresh();
        return applicationContext;
    }
}
