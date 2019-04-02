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

package com.vmware.mangle.utils.exceptions.handler;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 *
 * AppListener for Uncaught Exception.
 *
 * @author kumargautam
 */
@Log4j2
@Getter
public class AppListener implements SpringApplicationRunListener {
    private SpringApplication application;
    private String[] args;

    public AppListener(SpringApplication application, String[] args) {
        super();
        this.application = application;
        this.args = args;
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        MangleUncaughtExceptionHandler.getExceptionHandler().setLogDir(environment.getProperty("logging.path"));
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        //Not used.
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        //Not used.
    }

    /* (non-Javadoc)
     * @see org.springframework.boot.SpringApplicationRunListener#starting()
     */
    @Override
    public void starting() {
        log.info("AppListener starting...");
        MangleUncaughtExceptionHandler.register();
    }

    /* (non-Javadoc)
     * @see org.springframework.boot.SpringApplicationRunListener#started(org.springframework.context.ConfigurableApplicationContext)
     */
    @Override
    public void started(ConfigurableApplicationContext context) {
        //Not used.
    }

    /* (non-Javadoc)
     * @see org.springframework.boot.SpringApplicationRunListener#running(org.springframework.context.ConfigurableApplicationContext)
     */
    @Override
    public void running(ConfigurableApplicationContext context) {
        //Not used.
    }

    /* (non-Javadoc)
     * @see org.springframework.boot.SpringApplicationRunListener#failed(org.springframework.context.ConfigurableApplicationContext, java.lang.Throwable)
     */
    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        MangleUncaughtExceptionHandler.getExceptionHandler().uncaughtException(Thread.currentThread(), exception);
    }
}