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

package com.vmware.mangle.unittest.utils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.log4j.Logger;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.RemoteBase;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;

/**
 *
 *
 * @author chetanc
 */
@PrepareForTest(value = { RemoteBase.class })
public class RemoteBaseTest extends PowerMockTestCase {
    private static Logger log = Logger.getLogger(RemoteBaseTest.class);
    @Mock
    JSch jSch;

    @Mock
    Session session;

    private final String host = "10.10.10.10";
    private final String userName = "root";
    private final int port = 22;
    private final String privateKey = UUID.randomUUID().toString();
    private final String pwd = UUID.randomUUID().toString();
    private final int timeout = 10;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInstantiateRemoteBase() {
        RemoteBase remoteBase = new RemoteBase(host, userName, pwd, port, privateKey);
        Assert.assertEquals(remoteBase.getHost(), host);
        Assert.assertEquals(remoteBase.getUserName(), userName);
        Assert.assertEquals(remoteBase.getPassword(), pwd);
        Assert.assertEquals(remoteBase.getPort(), port);
        Assert.assertEquals(remoteBase.getPrivateKey(), privateKey);
        Assert.assertEquals(remoteBase.getTimeout(), 0);
    }

    @Test(description = "Test to verify the getter methods of the class RemoteBase class")
    public void testInstantiateRemoteBase1() {
        RemoteBase remoteBase = new RemoteBase(host, userName, pwd, port, privateKey, timeout);
        Assert.assertEquals(remoteBase.getHost(), host);
        Assert.assertEquals(remoteBase.getUserName(), userName);
        Assert.assertEquals(remoteBase.getPassword(), pwd);
        Assert.assertEquals(remoteBase.getPort(), port);
        Assert.assertEquals(remoteBase.getPrivateKey(), privateKey);
        Assert.assertEquals(remoteBase.getTimeout(), timeout);
    }

    @Test
    public void TestGetSession() throws Exception {
        SSHUtils sshUtils = new SSHUtils(host, userName, pwd, port, privateKey, timeout);
        PowerMockito.whenNew(JSch.class).withNoArguments().thenReturn(jSch);
        PowerMockito.when(jSch.getSession(anyString(), anyString(), anyInt())).thenReturn(session);
        PowerMockito.doNothing().when(jSch).addIdentity(any(), any(), any(), any());
        Assert.assertTrue(sshUtils.login());
        verify(jSch, times(1)).getSession(anyString(), anyString(), anyInt());
        verify(jSch, times(1)).addIdentity(any(), any(), any(), any());
    }

}
