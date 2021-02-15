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

package com.vmware.mangle.unittest.utils.notification;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import allbegray.slack.SlackClientFactory;
import allbegray.slack.type.Authentication;
import allbegray.slack.webapi.SlackWebApiClient;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.slack.Notifier;
import com.vmware.mangle.cassandra.model.slack.NotifierType;
import com.vmware.mangle.cassandra.model.slack.SlackInfo;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.notification.SlackClient;

/**
 * Unit Test cases for SlackClient.
 *
 * @author kumargautam
 */
@PrepareForTest(value = { SlackClientFactory.class })
@PowerMockIgnore({ "com.sun.org.apache.xalan.internal.xsltc.trax.*" })
public class SlackClientTest extends PowerMockTestCase {

    @Mock
    private SlackWebApiClient slackWebApiClient;
    private SlackClient slackClient;
    private Notifier notification;

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(SlackClientFactory.class);
        notification = getSlackInfo();
        slackClient = new SlackClient(notification);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.notification.SlackClient#getClient(java.lang.String)}.
     */
    @Test
    public void testGetClient() {
        PowerMockito.mockStatic(SlackClientFactory.class);
        when(SlackClientFactory.createWebApiClient(anyString())).thenReturn(slackWebApiClient);
        assertNotNull(slackClient.getClient());
        PowerMockito.verifyStatic(SlackClientFactory.class, times(1));
        SlackClientFactory.createWebApiClient(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.notification.SlackClient#testConnection(allbegray.slack.webapi.SlackWebApiClient)}.
     *
     * @throws MangleException
     */
    @Test
    public void testTestConnection() throws MangleException {
        PowerMockito.mockStatic(SlackClientFactory.class);
        when(SlackClientFactory.createWebApiClient(anyString())).thenReturn(slackWebApiClient);
        when(slackWebApiClient.auth()).thenReturn(new Authentication());
        assertTrue(slackClient.testConnection());
        PowerMockito.verifyStatic(SlackClientFactory.class, times(1));
        SlackClientFactory.createWebApiClient(anyString());
        verify(slackWebApiClient, times(1)).auth();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.notification.SlackClient#testConnection(allbegray.slack.webapi.SlackWebApiClient)}.
     *
     * @throws MangleException
     */
    @Test
    public void testTestConnectionForMangleException() throws MangleException {
        PowerMockito.mockStatic(SlackClientFactory.class);
        when(SlackClientFactory.createWebApiClient(anyString())).thenReturn(slackWebApiClient);
        doThrow(new IllegalArgumentException("testTestConnectionForMangleException")).when(slackWebApiClient).auth();
        try {
            slackClient.testConnection();
        } catch (MangleException e) {
            assertEquals(e.getErrorCode(), ErrorCode.NOTIFICATION_CONNECTION_FAILED);
            PowerMockito.verifyStatic(SlackClientFactory.class, times(1));
            SlackClientFactory.createWebApiClient(anyString());
            verify(slackWebApiClient, times(1)).auth();
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.notification.SlackClient#shutdown(allbegray.slack.webapi.SlackWebApiClient)}.
     */
    @Test
    public void testShutdown() {
        PowerMockito.mockStatic(SlackClientFactory.class);
        when(SlackClientFactory.createWebApiClient(anyString())).thenReturn(slackWebApiClient);
        doNothing().when(slackWebApiClient).shutdown();
        slackClient.shutdown(slackClient.getClient());
        PowerMockito.verifyStatic(SlackClientFactory.class, times(1));
        SlackClientFactory.createWebApiClient(anyString());
        verify(slackWebApiClient, times(1)).shutdown();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.notification.SlackClient#shutdown(allbegray.slack.webapi.SlackWebApiClient)}.
     */
    @Test
    public void testShutdownForException() {
        PowerMockito.mockStatic(SlackClientFactory.class);
        when(SlackClientFactory.createWebApiClient(anyString())).thenReturn(slackWebApiClient);
        doThrow(new IllegalArgumentException("testShutdownForException")).when(slackWebApiClient).shutdown();
        slackClient.shutdown(slackClient.getClient());
        PowerMockito.verifyStatic(SlackClientFactory.class, times(1));
        SlackClientFactory.createWebApiClient(anyString());
        verify(slackWebApiClient, times(1)).shutdown();
    }

    private Notifier getSlackInfo() {
        Notifier info = new Notifier();
        info.setName("Abc");
        info.setEnable(true);
        info.setNotifierType(NotifierType.SLACK);
        SlackInfo slackInfo = new SlackInfo();
        info.setSlackInfo(slackInfo);
        slackInfo.setToken("xxx");
        slackInfo.setChannels(Arrays.asList("dev"));
        slackInfo.setSenderName("Test");
        return info;
    }
}
