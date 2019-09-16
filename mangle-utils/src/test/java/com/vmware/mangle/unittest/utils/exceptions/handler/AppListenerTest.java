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

package com.vmware.mangle.unittest.utils.exceptions.handler;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.exceptions.handler.AppListener;
import com.vmware.mangle.utils.exceptions.handler.MangleUncaughtExceptionHandler;

/**
 * Unit Test case for AppListener.
 *
 * @author kumargautam
 */
public class AppListenerTest {

    @Mock
    private SpringApplication springApplication;
    private AppListener appListener;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() {
        this.appListener = new AppListener(springApplication, null);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() {
        this.appListener = null;
        this.springApplication = null;
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.exceptions.handler.AppListener#failed(org.springframework.context.ConfigurableApplicationContext, java.lang.Throwable)}.
     */
    @Test
    public void testFailed() {
        appListener.starting();
        ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
        when(environment.getProperty(anyString())).thenReturn("/var/opt/mangle-tomcat/logs");
        appListener.environmentPrepared(environment);
        MangleUncaughtExceptionHandler.getExceptionHandler().setExitRequired(false);
        try {
            throw new IllegalArgumentException("Thread dump testing");
        } catch (Exception exception) {
            appListener.failed(null, exception);
            verify(environment, times(1)).getProperty(anyString());
        }
    }
}
