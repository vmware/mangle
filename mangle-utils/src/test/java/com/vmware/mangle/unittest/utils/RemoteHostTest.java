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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.ConnectionInfo;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.RemoteHost;

/**
 *
 *
 * @author chetanc
 */
@PrepareForTest(value = { RemoteHost.class })
public class RemoteHostTest extends PowerMockTestCase {


    @Mock
    private Connection connection;

    @Mock
    private ConnectionInfo connectionInfo;

    @Mock
    private SCPClient scpClient;

    @Mock
    private Session session;

    @Mock
    private FileOutputStream fileOutputStream;

    @Mock
    private File file;

    String[] authMethods = new String[] { "password", "keyboard-interactive" };
    String[] authKeyboard = new String[] { "keyboard-interactive" };
    private String ip = "10.10.10.10";
    private String username = "username";
    private String pwd = "pwd";
    private static final String filename2 = "src/main/resources/FaultOperationProperties/VCenterFaultOperations.properties";
    private String command = "echo \"String\"";

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMakeConnection() throws Exception {
        InputStream inputStream = new FileInputStream(new File(filename2));

        PowerMockito.whenNew(Connection.class).withAnyArguments().thenReturn(connection);
        PowerMockito.when(connection.connect(any(), anyInt(), anyInt())).thenReturn(connectionInfo);
        PowerMockito.when(connection.getRemainingAuthMethods(anyString())).thenReturn(authMethods);
        PowerMockito.when(connection.authenticateWithPassword(anyString(), anyString())).thenReturn(true);

        when(connection.openSession()).thenReturn(session);
        doNothing().when(session).execCommand(command);
        when(session.getStdout()).thenReturn(inputStream);
        when(session.getStderr()).thenReturn(inputStream);

        RemoteHost remoteHost = new RemoteHost();
        String responseConnection = remoteHost.executeCommand(ip, username, pwd, command);
        Assert.assertTrue(responseConnection.contains("POWEROFF_VM=com.vmware.mangle.utils.clients.vcenter.VMOperations"));
        verify(connection, times(1)).openSession();
        verify(session, times(1)).getStdout();
        verify(session, times(1)).getStderr();
        verify(connection, times(1)).getRemainingAuthMethods(anyString());
        verify(connection, times(1)).connect(any(), anyInt(), anyInt());
        verify(connection, times(1)).authenticateWithPassword(any(), any());
    }

    @Test(description = "Test to verify the execution of a command on the remote host")
    public void testMakeConnection1() throws Exception {
        InputStream inputStream = new FileInputStream(new File(filename2));

        PowerMockito.whenNew(Connection.class).withAnyArguments().thenReturn(connection);
        PowerMockito.when(connection.connect(any(), anyInt(), anyInt())).thenReturn(connectionInfo);
        PowerMockito.when(connection.getRemainingAuthMethods(anyString())).thenReturn(authKeyboard);
        PowerMockito.when(connection.authenticateWithKeyboardInteractive(anyString(), any())).thenReturn(true);

        when(connection.openSession()).thenReturn(session);
        doNothing().when(session).execCommand(command);
        when(session.getStdout()).thenReturn(inputStream);
        when(session.getStderr()).thenReturn(inputStream);

        RemoteHost remoteHost = new RemoteHost();
        String responseConnection = remoteHost.executeCommand(ip, username, pwd, command);
        Assert.assertTrue(responseConnection.contains("POWEROFF_VM=com.vmware.mangle.utils.clients.vcenter.VMOperations"));
        verify(connection, times(1)).openSession();
        verify(session, times(1)).getStdout();
        verify(session, times(1)).getStderr();
        verify(connection, times(1)).getRemainingAuthMethods(anyString());
        verify(connection, times(1)).connect(any(), anyInt(), anyInt());
        verify(connection, times(1)).authenticateWithKeyboardInteractive(any(), any());
    }

    @Test(description = "Test to verify the execution of a command, and verify the behavior when there are no authentication methods")
    public void testMakeConnection2() throws Exception {
        InputStream inputStream = new FileInputStream(new File(filename2));

        PowerMockito.whenNew(Connection.class).withAnyArguments().thenReturn(connection);
        PowerMockito.when(connection.connect(any(), anyInt(), anyInt())).thenReturn(connectionInfo);
        PowerMockito.when(connection.getRemainingAuthMethods(anyString())).thenReturn(new String[] {});

        RemoteHost remoteHost = new RemoteHost();
        String responseConnection = remoteHost.executeCommand(ip, username, pwd, command);
        Assert.assertTrue(
                responseConnection.startsWith("Exception") && responseConnection.contains("Authentication failed"));
        verify(connection, times(1)).getRemainingAuthMethods(anyString());
        verify(connection, times(1)).connect(any(), anyInt(), anyInt());
    }


    @Test(description = "Test to verify the execution of a command on the remote host when the authenticateWithKeyboardInteractive method return false")
    public void testMakeConnection3() throws Exception {
        InputStream inputStream = new FileInputStream(new File(filename2));

        PowerMockito.whenNew(Connection.class).withAnyArguments().thenReturn(connection);
        PowerMockito.when(connection.connect(any(), anyInt(), anyInt())).thenReturn(connectionInfo);
        PowerMockito.when(connection.getRemainingAuthMethods(anyString())).thenReturn(authKeyboard);
        PowerMockito.when(connection.authenticateWithKeyboardInteractive(anyString(), any())).thenReturn(false);

        RemoteHost remoteHost = new RemoteHost();
        String responseConnection = remoteHost.executeCommand(ip, username, pwd, command);
        Assert.assertTrue(
                responseConnection.startsWith("Exception") && responseConnection.contains("Authentication failed"));
        verify(connection, times(1)).getRemainingAuthMethods(anyString());
        verify(connection, times(1)).connect(any(), anyInt(), anyInt());
        verify(connection, times(1)).authenticateWithKeyboardInteractive(any(), any());
    }

    @Test
    public void testFromRemoteToNewFile() throws Exception {
        PowerMockito.whenNew(Connection.class).withAnyArguments().thenReturn(connection);
        PowerMockito.whenNew(FileOutputStream.class).withAnyArguments().thenReturn(fileOutputStream);
        PowerMockito.when(connection.connect(any(), anyInt(), anyInt())).thenReturn(connectionInfo);
        PowerMockito.when(connection.getRemainingAuthMethods(anyString())).thenReturn(authMethods);
        PowerMockito.when(connection.authenticateWithPassword(anyString(), anyString())).thenReturn(true);

        PowerMockito.whenNew(SCPClient.class).withAnyArguments().thenReturn(scpClient);
        PowerMockito.doNothing().when(scpClient).get(any(), (OutputStream) any());

        RemoteHost remoteHost = new RemoteHost();
        int response = remoteHost.fromRemoteToNewFile(ip, username, pwd, filename2, filename2);
        Assert.assertEquals(response, 1);
    }

    @Test(description = "Test to verify the behaviour of the method fromRemoteToNewFile when the file.exists() method return false, which should trigger creation of the new file with the given name and should copy the data to that file")
    public void testFromRemoteToNewFile1() throws Exception {
        PowerMockito.whenNew(Connection.class).withAnyArguments().thenReturn(connection);
        PowerMockito.whenNew(FileOutputStream.class).withAnyArguments().thenReturn(fileOutputStream);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(file);
        PowerMockito.when(file.exists()).thenReturn(false);
        PowerMockito.when(file.createNewFile()).thenReturn(true);
        PowerMockito.when(connection.connect(any(), anyInt(), anyInt())).thenReturn(connectionInfo);
        PowerMockito.when(connection.getRemainingAuthMethods(anyString())).thenReturn(authMethods);
        PowerMockito.when(connection.authenticateWithPassword(anyString(), anyString())).thenReturn(true);

        PowerMockito.whenNew(SCPClient.class).withAnyArguments().thenReturn(scpClient);
        PowerMockito.doNothing().when(scpClient).get(any(), (OutputStream) any());

        RemoteHost remoteHost = new RemoteHost();
        int response = remoteHost.fromRemoteToNewFile(ip, username, pwd, filename2, filename2);
        Assert.assertEquals(response, 1);

        verify(connection, times(1)).getRemainingAuthMethods(anyString());
        verify(connection, times(1)).connect(any(), anyInt(), anyInt());
        verify(connection, times(1)).authenticateWithPassword(any(), any());
    }

    @Test(description = "Test to verify the behaviour of the executeCommand method when the authenticateWithKeyboardInteractive method return false")
    public void testFromRemoteToNewFile2() throws Exception {
        PowerMockito.whenNew(Connection.class).withAnyArguments().thenReturn(connection);
        PowerMockito.when(connection.connect(any(), anyInt(), anyInt())).thenReturn(connectionInfo);
        PowerMockito.when(connection.getRemainingAuthMethods(anyString())).thenReturn(authKeyboard);
        PowerMockito.when(connection.authenticateWithKeyboardInteractive(anyString(), any())).thenReturn(false);

        RemoteHost remoteHost = new RemoteHost();
        int response = remoteHost.fromRemoteToNewFile(ip, username, pwd, filename2, filename2);
        Assert.assertEquals(response, -1);
        verify(connection, times(1)).getRemainingAuthMethods(anyString());
        verify(connection, times(1)).connect(any(), anyInt(), anyInt());
        verify(connection, times(1)).authenticateWithKeyboardInteractive(any(), any());
    }
}
