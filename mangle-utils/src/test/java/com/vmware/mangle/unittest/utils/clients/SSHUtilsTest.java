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

package com.vmware.mangle.unittest.utils.clients;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit Test Case for SSHUtils.
 *
 * @author kumargautam
 */
@PrepareForTest(value = { SSHUtils.class, FileUtils.class })
@PowerMockIgnore( { "com.sun.org.apache.xalan.internal.xsltc.trax.*" } )
public class SSHUtilsTest extends PowerMockTestCase {

    private SSHUtils sshUtils;
    @Mock
    private JSch jSch;
    @Mock
    private Session session;
    @Mock
    private ChannelExec channelExec;
    @Mock
    private ChannelSftp channelSftp;
    private final String host = "10.134.211.215";
    private final String userName = "root";
    private final int port = 22;


    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(FileUtils.class);
        this.sshUtils = new SSHUtils(host, userName, "vmwaee", port, 1000);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        validateMockitoUsage();
    }

    /**
     * Test method for {@link com.vmware.mangle.clients.ssh.SSHUtils#login()}.
     *
     * @throws Exception
     */
    @Test(priority = 1)
    public void testLogin() throws Exception {
        testLoginBefore();
        Assert.assertTrue(sshUtils.login());
        testLoginAfter();
    }

    /**
     * Test method for {@link com.vmware.mangle.clients.ssh.SSHUtils#runCommand(java.lang.String)}.
     *
     * @throws Exception
     */
    @Test(priority = 3)
    public void testRunCommandReturningResultString() throws Exception {
        testRunCommandReturningResultBefore();
        CommandExecutionResult commandExecutionResult = sshUtils.runCommand("test");
        Assert.assertEquals(commandExecutionResult.getExitCode(), 0);
        testRunCommandReturningResultAfter();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.ssh.SSHUtils#putFile(java.lang.String, java.lang.String)}.
     *
     * @throws Exception
     */
    @Test(priority = 4)
    public void testPutFile() throws Exception {
        testsBeforePutFile();
        doNothing().when(channelSftp).cd(anyString());
        doNothing().when(channelSftp).put(any(InputStream.class), anyString());
        doNothing().when(channelSftp).exit();
        doNothing().when(channelSftp).disconnect();
        doNothing().when(session).disconnect();
        Assert.assertTrue(sshUtils.putFile("src/test/resources/mock_command.txt", "/tmp"));
        verificationAfterPutFile();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.ssh.SSHUtils#getFile(java.lang.String, java.lang.String, java.lang.String)}.
     *
     * @throws Exception
     */
    @Test(priority = 5)
    public void testGetFile() throws Exception {
        PowerMockito.whenNew(JSch.class).withNoArguments().thenReturn(jSch);
        PowerMockito.when(jSch.getSession(anyString(), anyString(), anyInt())).thenReturn(session);
        doNothing().when(jSch).addIdentity(anyString(), any(), any(), any());
        doNothing().when(session).setConfig(any(Properties.class));
        doNothing().when(session).setPassword(anyString());
        doNothing().when(session).setPort(anyInt());
        doNothing().when(session).setTimeout(anyInt());
        doNothing().when(session).connect();
        when(session.openChannel(anyString())).thenReturn(channelSftp);
        doNothing().when(channelSftp).connect();
        doNothing().when(channelSftp).cd(anyString());
        doNothing().when(channelSftp).put(any(InputStream.class), anyString());
        InputStream commandAsStream = SSHUtilsTest.class.getResourceAsStream("/mock_command.txt");
        when(channelSftp.get(anyString())).thenReturn(commandAsStream);
        doNothing().when(channelSftp).exit();
        doNothing().when(channelSftp).disconnect();
        doNothing().when(session).disconnect();

        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.doNothing().when(FileUtils.class);
        FileUtils.copyInputStreamToFile(any(InputStream.class), any(File.class));
        Assert.assertTrue(sshUtils.getFile("src/test/resources/mock_command.txt", "/mock_command.txt", "/tmp"));
        verify(session, times(1)).openChannel(anyString());
        verify(channelSftp, times(1)).connect();
        verify(channelSftp, times(1)).disconnect();
        verify(channelSftp, times(1)).exit();
        PowerMockito.verifyNew(JSch.class, times(1)).withNoArguments();
        verify(jSch, times(1)).getSession(anyString(), anyString(), anyInt());
        verify(session, times(1)).connect();
        verify(session, times(1)).disconnect();

        PowerMockito.verifyStatic(FileUtils.class, times(1));
        FileUtils.copyInputStreamToFile(any(InputStream.class), any(File.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.ssh.SSHUtils#executeCommand(java.lang.String)}.
     *
     * @throws Exception
     */
    @Test(priority = 6)
    public void testExecuteCommand() throws Exception {
        testRunCommandReturningResultBefore();
        CommandExecutionResult commandExecutionResult = sshUtils.executeCommand("test6");
        Assert.assertEquals(commandExecutionResult.getExitCode(), 0);
        testRunCommandReturningResultAfter();
    }

    /**
     * Test method for {@link com.vmware.mangle.clients.ssh.SSHUtils#testConnection()}.
     *
     * @throws Exception
     */
    @Test(priority = 7)
    public void testTestConnection() throws Exception {
        testLoginBefore();
        when(session.openChannel(anyString())).thenReturn(channelSftp);
        Assert.assertTrue(sshUtils.testConnection());
        testLoginAfter();
    }

    /**
     * Test method for {@link com.vmware.mangle.clients.ssh.SSHUtils#testConnection()}.
     *
     * @throws Exception
     */
    @Test(priority = 8)
    public void testTestConnectionInvalidCredentialsFailure() throws Exception {
        try {
            testLoginBefore();
            JSchException exception = new JSchException(ErrorConstants.RM_AUTH_FAIL);
            doThrow(exception).when(session).connect();
            when(session.openChannel(anyString())).thenReturn(channelSftp);
            sshUtils.testConnection();
        } catch (MangleException exception) {
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.RM_INVALID_CREDENTIALS);
            PowerMockito.verifyNew(JSch.class, times(1)).withNoArguments();
            verify(jSch, times(1)).getSession(anyString(), anyString(), anyInt());
            verify(session, times(1)).connect();
        }
    }

    /**
     * Test method for {@link com.vmware.mangle.clients.ssh.SSHUtils#testConnection()}.
     *
     * @throws Exception
     */
    @Test(priority = 9)
    public void testTestConnectionConnectionRefusedFailure() throws Exception {
        try {
            testLoginBefore();
            JSchException exception = new JSchException(ErrorConstants.RM_CONNECTION_REFUSED);
            doThrow(exception).when(session).connect();
            when(session.openChannel(anyString())).thenReturn(channelSftp);
            sshUtils.testConnection();
        } catch (MangleException exception) {
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.RM_CONNECTION_REFUSED);
            PowerMockito.verifyNew(JSch.class, times(1)).withNoArguments();
            verify(jSch, times(1)).getSession(anyString(), anyString(), anyInt());
            verify(session, times(1)).connect();
        }
    }

    /**
     * Test method for {@link com.vmware.mangle.clients.ssh.SSHUtils#testConnection()}.
     *
     * @throws Exception
     */
    @Test(priority = 10)
    public void testTestConnectionSftpNotEnabledFailure() throws Exception {
        try {
            testLoginBefore();
            doThrow(JSchException.class).when(session).openChannel(anyString());
            sshUtils.testConnection();
        } catch (MangleException exception) {
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.RM_SFTP_NOT_ENABLED);
            PowerMockito.verifyNew(JSch.class, times(1)).withNoArguments();
            verify(jSch, times(1)).getSession(anyString(), anyString(), anyInt());
            verify(session, times(1)).connect();
        }
    }

    /**
     * Test method for {@link com.vmware.mangle.clients.ssh.SSHUtils#testConnection()}.
     *
     * @throws Exception
     */
    @Test(priority = 11)
    public void testTestGenericConnectionConnectionFailure() throws Exception {
        try {
            testLoginBefore();
            JSchException exception = new JSchException("Connection Failure");
            doThrow(exception).when(session).connect();
            when(session.openChannel(anyString())).thenReturn(channelSftp);
            sshUtils.testConnection();
        } catch (MangleException exception) {
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.RM_CONNECTION_EXCEPTION);
            PowerMockito.verifyNew(JSch.class, times(1)).withNoArguments();
            verify(jSch, times(1)).getSession(anyString(), anyString(), anyInt());
            verify(session, times(1)).connect();
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.ssh.SSHUtils#putFile(java.lang.String, java.lang.String)}.
     *
     * @throws Exception
     */
    @Test(priority = 12)
    public void testPutFileDirectoryNotFoundError() throws Exception {
        testsBeforePutFile();
        doThrow(new SftpException(1, "directory not found")).when(channelSftp).cd(anyString());
        doNothing().when(channelSftp).put(any(InputStream.class), anyString());
        doNothing().when(channelSftp).exit();
        doNothing().when(channelSftp).disconnect();
        doNothing().when(session).disconnect();
        boolean exceptionCalled = false;
        try {
            sshUtils.putFile("src/test/resources/mock_command.txt", "/tmp");
        } catch (Exception e) {
            exceptionCalled = true;
        }
        Assert.assertTrue(exceptionCalled);
        verify(session, times(1)).openChannel(anyString());
        verify(channelSftp, times(1)).connect();
        verify(channelSftp, times(1)).disconnect();
        verify(channelSftp, times(1)).exit();
        PowerMockito.verifyNew(JSch.class, times(1)).withNoArguments();
        verify(jSch, times(1)).getSession(anyString(), anyString(), anyInt());
        verify(session, times(1)).connect();
        verify(session, times(1)).disconnect();

    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.ssh.SSHUtils#putFile(java.lang.String, java.lang.String)}.
     *
     * @throws Exception
     */
    @Test(priority = 13)
    public void testPutFileGenericFileTransferError() throws Exception {
        testsBeforePutFile();
        doNothing().when(channelSftp).cd(anyString());
        doThrow(Exception.class).when(channelSftp).put(any(InputStream.class), anyString());
        doNothing().when(channelSftp).exit();
        doNothing().when(channelSftp).disconnect();
        doNothing().when(session).disconnect();
        boolean exceptionCalled = false;
        try {
            sshUtils.putFile("src/test/resources/mock_command.txt", "/tmp");
        } catch (Exception e) {
            exceptionCalled = true;
        }
        Assert.assertTrue(exceptionCalled);
        verificationAfterPutFile();

    }

    private void testsBeforePutFile() throws Exception {
        PowerMockito.whenNew(JSch.class).withNoArguments().thenReturn(jSch);
        PowerMockito.when(jSch.getSession(anyString(), anyString(), anyInt())).thenReturn(session);
        doNothing().when(jSch).addIdentity(anyString(), any(), any(), any());
        doNothing().when(session).setConfig(any(Properties.class));
        doNothing().when(session).setPassword(anyString());
        doNothing().when(session).setPort(anyInt());
        doNothing().when(session).setTimeout(anyInt());
        doNothing().when(session).connect();
        when(session.openChannel(anyString())).thenReturn(channelSftp);
        doNothing().when(channelSftp).connect();

    }

    private void verificationAfterPutFile() throws Exception {
        verify(session, times(1)).openChannel(anyString());
        verify(channelSftp, times(1)).connect();
        verify(channelSftp, times(1)).disconnect();
        verify(channelSftp, times(1)).exit();
        PowerMockito.verifyNew(JSch.class, times(1)).withNoArguments();
        verify(jSch, times(1)).getSession(anyString(), anyString(), anyInt());
        verify(session, times(1)).connect();
        verify(session, times(1)).disconnect();
    }

    private void testLoginBefore() throws Exception {
        PowerMockito.whenNew(JSch.class).withNoArguments().thenReturn(jSch);
        PowerMockito.when(jSch.getSession(anyString(), anyString(), anyInt())).thenReturn(session);
        doNothing().when(jSch).addIdentity(anyString(), any(), any(), any());
        doNothing().when(session).setConfig(any(Properties.class));
        doNothing().when(session).setPassword(anyString());
        doNothing().when(session).setPort(anyInt());
        doNothing().when(session).setTimeout(anyInt());
        doNothing().when(session).connect();
        doNothing().when(session).disconnect();
    }

    private void testLoginAfter() throws Exception {
        PowerMockito.verifyNew(JSch.class, times(1)).withNoArguments();
        verify(jSch, times(1)).getSession(anyString(), anyString(), anyInt());
        verify(session, times(1)).connect();
        verify(session, times(1)).disconnect();
    }

    private void testRunCommandReturningResultBefore() throws Exception {
        PowerMockito.whenNew(JSch.class).withNoArguments().thenReturn(jSch);
        PowerMockito.when(jSch.getSession(anyString(), anyString(), anyInt())).thenReturn(session);
        doNothing().when(jSch).addIdentity(anyString(), any(), any(), any());
        doNothing().when(session).setConfig(any(Properties.class));
        doNothing().when(session).setPassword(anyString());
        doNothing().when(session).setPort(anyInt());
        doNothing().when(session).setTimeout(anyInt());
        doNothing().when(session).connect();
        when(session.openChannel(anyString())).thenReturn(channelExec);
        doNothing().when(channelExec).setCommand(anyString());
        doNothing().when(channelExec).setErrStream(any(ByteArrayOutputStream.class));
        InputStream commandAsStream = SSHUtilsTest.class.getResourceAsStream("/mock_command.txt");
        when(channelExec.getInputStream()).thenReturn(commandAsStream);
        when(channelExec.getExtInputStream()).thenReturn(commandAsStream);
        doNothing().when(channelExec).connect();
        when(channelExec.isClosed()).thenReturn(true);
        doNothing().when(session).disconnect();
        doNothing().when(channelExec).disconnect();
    }

    private void testRunCommandReturningResultAfter() throws Exception {
        PowerMockito.verifyNew(JSch.class, times(1)).withNoArguments();
        verify(jSch, times(1)).getSession(anyString(), anyString(), anyInt());
        verify(session, times(1)).connect();
        verify(session, times(1)).openChannel(anyString());
        verify(channelExec, times(1)).connect();
    }


}
