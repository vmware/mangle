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

package com.vmware.mangle.unittest.services.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.services.LoggersEndpointService;
import com.vmware.mangle.services.commons.ServiceCommonUtils;
import com.vmware.mangle.services.hazelcast.HazelcastSyncTopicManager;

/**
 * Unit test cases for LoggersEndpointService.
 *
 * @author kumargautam
 */
public class LoggersEndpointServiceTest {

    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private LoggingSystem loggingSystem;

    private LoggersEndpointService loggersEndpointService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        LoggersEndpoint loggersEndpoint = new LoggersEndpoint(loggingSystem);
        loggersEndpointService = new LoggersEndpointService(loggersEndpoint);
        ServiceCommonUtils.setApplicationContext(applicationContext);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.LoggersEndpointService#configureLogLevel(java.lang.String, org.springframework.boot.logging.LogLevel)}.
     */
    @Test
    public void testConfigureLogLevel() {
        LoggerConfiguration loggerConfiguration = getLoggerConfiguration();
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        doNothing().when(loggingSystem).setLogLevel(anyString(), any(LogLevel.class));
        loggersEndpointService.configureLogLevel(loggerConfiguration.getName(),
                loggerConfiguration.getConfiguredLevel());
        verify(loggingSystem, times(1)).setLogLevel(anyString(), any(LogLevel.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.LoggersEndpointService#resync(java.lang.String)}.
     */
    @Test
    public void testResync() {
        LoggerConfiguration loggerConfiguration = getLoggerConfiguration();
        String objectIdentifier = loggerConfiguration.getName() + "#" + loggerConfiguration.getConfiguredLevel().name();
        doNothing().when(loggingSystem).setLogLevel(anyString(), any(LogLevel.class));
        loggersEndpointService.resync(objectIdentifier);
        verify(loggingSystem, times(1)).setLogLevel(anyString(), any(LogLevel.class));
    }

    private LoggerConfiguration getLoggerConfiguration() {
        LoggerConfiguration loggerConfiguration = new LoggerConfiguration("com.test", LogLevel.INFO, LogLevel.INFO);
        return loggerConfiguration;
    }

}