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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.auth.AuthTestRequest;
import com.slack.api.methods.response.auth.AuthTestResponse;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
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
import com.vmware.mangle.utils.notification.SlackClient;

/**
 * Unit Test cases for SlackClient.
 *
 * @author kumargautam
 */
@PrepareForTest(value = { Slack.class })
@PowerMockIgnore({"javax.net.ssl.*", "com.sun.org.apache.xalan.internal.xsltc.trax.*"})
public class SlackClientTest extends PowerMockTestCase {
    @Mock
    private MethodsClient methodsClient;
    private SlackClient slackClient;
    private Notifier notification;
    private Slack slack;

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Slack.class);
        notification = getSlackInfo();
        slackClient = new SlackClient(notification);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.notification.SlackClient#getClient()}.
     */
    @Test
    public void testGetClient() {
        PowerMockito.mockStatic(Slack.class);
        slack = PowerMockito.mock(Slack.class);
        when(Slack.getInstance()).thenReturn(slack);
        when(slack.methods(anyString())).thenReturn(methodsClient);
        assertNotNull(slackClient.getClient());
        PowerMockito.verifyStatic(Slack.class, times(1));
        slack.methods(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.notification.SlackClient#testConnection()}.
     *
     * @throws MangleException
     */
    @Test
    public void testTestConnection() throws Exception {
        PowerMockito.mockStatic(Slack.class);
        slack = PowerMockito.mock(Slack.class);
        when(Slack.getInstance()).thenReturn(slack);
        when(slack.methods(anyString())).thenReturn(methodsClient);
        when(methodsClient.authTest(any(AuthTestRequest.class))).thenReturn(new AuthTestResponse());
        assertTrue(slackClient.testConnection());
        slack.methods(anyString());
        verify(methodsClient, times(1)).authTest(any(AuthTestRequest.class));
        PowerMockito.verifyStatic(Slack.class, times(1));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.notification.SlackClient#testConnection()}.
     *
     * @throws MangleException
     */
    @Test
    public void testTestConnectionForMangleException() throws MangleException, IOException, SlackApiException {
        PowerMockito.mockStatic(Slack.class);
        slack = PowerMockito.mock(Slack.class);
        when(Slack.getInstance()).thenReturn(slack);
        when(slack.methods(anyString())).thenReturn(methodsClient);
        doThrow(new IllegalArgumentException("testTestConnectionForMangleException")).when(methodsClient)
                .authTest(any(AuthTestRequest.class));
        try {
            slackClient.testConnection();
        } catch (MangleException e) {
            assertEquals(e.getErrorCode(), ErrorCode.NOTIFICATION_CONNECTION_FAILED);
            slack.methods(anyString());
            verify(methodsClient, times(1)).authTest(any(AuthTestRequest.class));
            PowerMockito.verifyStatic(Slack.class, times(1));
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.notification.SlackClient#shutdown()}.
     */
    @Test
    public void testShutdown() throws Exception {
        PowerMockito.mockStatic(Slack.class);
        slack = PowerMockito.mock(Slack.class);
        when(Slack.getInstance()).thenReturn(slack);
        when(slack.methods(anyString())).thenReturn(methodsClient);
        doNothing().when(slack).close();
        slackClient.getClient();
        slackClient.shutdown();
        slack.methods(anyString());
        verify(slack, times(1)).close();
        PowerMockito.verifyStatic(Slack.class, times(1));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.notification.SlackClient#shutdown()}.
     */
    @Test
    public void testShutdownForException() throws Exception {
        PowerMockito.mockStatic(Slack.class);
        slack = PowerMockito.mock(Slack.class);
        when(Slack.getInstance()).thenReturn(slack);
        when(slack.methods(anyString())).thenReturn(methodsClient);
        doThrow(new IllegalArgumentException("testShutdownForException")).when(slack).close();
        slackClient.getClient();
        slackClient.shutdown();
        slack.methods(anyString());
        verify(slack, times(1)).close();
        PowerMockito.verifyStatic(Slack.class, times(1));
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
