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

package com.vmware.mangle.unittest.utils.clients.docker;


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.ProcessingException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CopyArchiveToContainerCmd;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.InspectExecCmd;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.PingCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.command.TopContainerCmd;
import com.github.dockerjava.api.command.TopContainerResponse;
import com.github.dockerjava.api.command.UnpauseContainerCmd;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerPort;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 * Unit Test Case for CustomDockerClient.
 *
 * @author kumargautam
 *
 */
@PrepareForTest(value = { DockerClientBuilder.class, CustomDockerClient.class, ExposedPort.class })
@PowerMockIgnore(value = { "javax.net.ssl.*", "com.sun.org.apache.xalan.internal.xsltc.trax.*" })
public class CustomDockerClientTest extends PowerMockTestCase {

    @InjectMocks
    private CustomDockerClient customDockerClient;
    @Mock
    private DockerClient dockerClient;
    @Mock
    private DockerClientBuilder dockerClientBuilder;
    private final String host = "10.134.211.2";
    private final Integer port = 2375;
    private final String containerName = "test";
    private final String containerId = "test1";

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        PowerMockito.mockStatic(DockerClientBuilder.class);
        MockitoAnnotations.initMocks(this);
        new CustomDockerClient(host, port, true, null);
        customDockerClient.setDockerClient(dockerClient);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.dockerClientBuilder = null;
        this.dockerClient = null;
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        validateMockitoUsage();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#getDockerClient()}.
     */
    @Test
    public void testGetDockerClient() {
        Assert.assertNotNull(customDockerClient.getDockerClient());
    }

    /**
     * Test method for {@link CustomDockerClient}
     */
    @Test(description = "verify the creation of mangle docker client")
    public void testInstantiateClient() {
        CustomDockerClient customDockerClient = new CustomDockerClient(host, port, false, null);
        Assert.assertNotNull(customDockerClient.getDockerClient());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#execCreateCmd(String)}
     */
    @Test(description = "verify the execution of the create command")
    public void testExecCreateCmd() {
        String containerId = UUID.randomUUID().toString();
        ExecCreateCmd execCreateCmd = Mockito.mock(ExecCreateCmd.class);
        when(dockerClient.execCreateCmd(anyString())).thenReturn(execCreateCmd);
        ExecCreateCmd execCreateCmd1 = customDockerClient.execCreateCmd(containerId);
        Assert.assertEquals(execCreateCmd1, execCreateCmd);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#execCreateCmdByContainerName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testExecCreateCmdByContainerName() throws MangleException {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);
        ExecCreateCmd execCreateCmd = mock(ExecCreateCmd.class);
        when(execCreateCmd.getContainerId()).thenReturn(containerId);
        when(dockerClient.execCreateCmd(anyString())).thenReturn(execCreateCmd);
        ExecCreateCmd actualResult = customDockerClient.execCreateCmdByContainerName(containerName);
        Assert.assertEquals(actualResult.getContainerId(), containerId);
        verify(dockerClient, times(1)).listContainersCmd();
        verify(dockerClient, times(1)).execCreateCmd(anyString());
    }


    @Test(description = "verify ProcessingException thrown through findContainerId")
    public void testExecCreateCmdByContainerNametoThrowProcessingException() throws MangleException {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        ProcessingException processingException = mock(ProcessingException.class);
        doThrow(processingException).when(listContainersCmd).exec();

        when(container.getNames()).thenReturn(new String[] { containerName });
        when(container.getId()).thenReturn(containerId);
        try {
            customDockerClient.execCreateCmdByContainerName(containerName);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DOCKER_CONNECTION_FAILURE);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#execCommandInContainerByName(java.lang.String, java.lang.String)}.
     *
     * @throws Exception
     */
    @Test
    public void testExecCommandInContainerByName() throws Exception {
        ExecCreateCmd execCreateCmd = mock(ExecCreateCmd.class);
        when(dockerClient.execCreateCmd(anyString())).thenReturn(execCreateCmd);
        when(execCreateCmd.withCmd(anyString(), anyString(), anyString())).thenReturn(execCreateCmd);
        when(execCreateCmd.withTty(anyBoolean())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdin(anyBoolean())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdout(anyBoolean())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStderr(anyBoolean())).thenReturn(execCreateCmd);
        ExecCreateCmdResponse exec = mock(ExecCreateCmdResponse.class);
        when(execCreateCmd.exec()).thenReturn(exec);
        when(exec.getId()).thenReturn(containerId);
        ByteArrayOutputStream byteArrayOutputStream = mock(ByteArrayOutputStream.class);
        PowerMockito.whenNew(ByteArrayOutputStream.class).withNoArguments().thenReturn(byteArrayOutputStream);

        ExecStartCmd execStartCmd = mock(ExecStartCmd.class);
        when(dockerClient.execStartCmd(anyString())).thenReturn(execStartCmd);
        when(execStartCmd.withDetach(anyBoolean())).thenReturn(execStartCmd);
        when(execStartCmd.withTty(anyBoolean())).thenReturn(execStartCmd);
        ExecStartResultCallback execStartResultCallback = mock(ExecStartResultCallback.class);
        PowerMockito.whenNew(ExecStartResultCallback.class)
                .withArguments(any(OutputStream.class), any(OutputStream.class)).thenReturn(execStartResultCallback);
        when(execStartCmd.exec(execStartResultCallback)).thenReturn(execStartResultCallback);
        when(execStartResultCallback.awaitCompletion()).thenReturn(execStartResultCallback);
        PowerMockito.when(byteArrayOutputStream.toString()).thenReturn(containerId).thenReturn("");


        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);

        InspectExecResponse inspectExecResponse = mock(InspectExecResponse.class);
        when(inspectExecResponse.getContainerID()).thenReturn(containerId);

        when(inspectExecResponse.getExitCode().intValue()).thenReturn(0);
        InspectExecCmd inspectExecCmd = mock(InspectExecCmd.class);
        when(inspectExecCmd.exec()).thenReturn(inspectExecResponse);
        when(dockerClient.inspectExecCmd(anyString())).thenReturn(inspectExecCmd);

        CommandExecutionResult actualResult = customDockerClient.execCommandInContainerByName(containerName, "cp test");
        Assert.assertEquals(actualResult.getCommandOutput(), containerId);
        Assert.assertEquals(actualResult.getExitCode(), 0);
        verify(dockerClient, times(1)).execCreateCmd(anyString());
        verify(dockerClient, times(1)).execStartCmd(anyString());
        PowerMockito.verifyNew(ByteArrayOutputStream.class, times(2)).withNoArguments();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#execCommandInContainerByName(java.lang.String, java.lang.String)}.
     *
     * @throws Exception
     */
    @Test(description = "verify the execution of command on the container")
    public void testExecCommandInContainerByName1() throws Exception {
        ExecCreateCmd execCreateCmd = mock(ExecCreateCmd.class);
        when(dockerClient.execCreateCmd(anyString())).thenReturn(execCreateCmd);
        when(execCreateCmd.withCmd(anyString(), anyString(), anyString())).thenReturn(execCreateCmd);
        when(execCreateCmd.withTty(anyBoolean())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdin(anyBoolean())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdout(anyBoolean())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStderr(anyBoolean())).thenReturn(execCreateCmd);
        ExecCreateCmdResponse exec = mock(ExecCreateCmdResponse.class);
        when(execCreateCmd.exec()).thenReturn(exec);
        when(exec.getId()).thenReturn(containerId);
        ByteArrayOutputStream byteArrayOutputStream = mock(ByteArrayOutputStream.class);
        PowerMockito.whenNew(ByteArrayOutputStream.class).withNoArguments().thenReturn(byteArrayOutputStream);
        ExecStartCmd execStartCmd = mock(ExecStartCmd.class);
        when(dockerClient.execStartCmd(anyString())).thenReturn(execStartCmd);
        when(execStartCmd.withDetach(anyBoolean())).thenReturn(execStartCmd);
        when(execStartCmd.withTty(anyBoolean())).thenReturn(execStartCmd);
        ExecStartResultCallback execStartResultCallback = mock(ExecStartResultCallback.class);
        PowerMockito.whenNew(ExecStartResultCallback.class)
                .withArguments(any(OutputStream.class), any(OutputStream.class)).thenReturn(execStartResultCallback);
        when(execStartCmd.exec(execStartResultCallback)).thenReturn(execStartResultCallback);
        doThrow(new InterruptedException()).when(execStartResultCallback).awaitCompletion();
        PowerMockito.when(byteArrayOutputStream.toString()).thenReturn(containerId);


        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);

        InspectExecResponse inspectExecResponse = mock(InspectExecResponse.class);
        when(inspectExecResponse.getContainerID()).thenReturn(containerId);
        InspectExecCmd inspectExecCmd = mock(InspectExecCmd.class);
        when(inspectExecCmd.exec()).thenReturn(inspectExecResponse);
        when(dockerClient.inspectExecCmd(anyString())).thenReturn(inspectExecCmd);

        CommandExecutionResult actualResult = customDockerClient.execCommandInContainerByName(containerName, "cp test");
        Assert.assertEquals(actualResult.getCommandOutput(), null);
        verify(dockerClient, times(1)).execCreateCmd(anyString());
        verify(dockerClient, times(1)).execStartCmd(anyString());
        PowerMockito.verifyNew(ByteArrayOutputStream.class, times(2)).withNoArguments();
    }


    @Test(description = "verify the execution of command on the container--To verify the RuntimeException")
    public void testExecCommandInContainerByNametoCatchRunTimeException() throws Exception {
        ExecCreateCmd execCreateCmd = mock(ExecCreateCmd.class);
        when(dockerClient.execCreateCmd(anyString())).thenReturn(execCreateCmd);
        when(execCreateCmd.withCmd(anyString(), anyString(), anyString())).thenReturn(execCreateCmd);
        when(execCreateCmd.withTty(anyBoolean())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdin(anyBoolean())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdout(anyBoolean())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStderr(anyBoolean())).thenReturn(execCreateCmd);
        ExecCreateCmdResponse exec = mock(ExecCreateCmdResponse.class);
        when(execCreateCmd.exec()).thenReturn(exec);
        when(exec.getId()).thenReturn(containerId);
        ByteArrayOutputStream byteArrayOutputStream = mock(ByteArrayOutputStream.class);
        PowerMockito.whenNew(ByteArrayOutputStream.class).withNoArguments().thenReturn(byteArrayOutputStream);
        ExecStartCmd execStartCmd = mock(ExecStartCmd.class);
        when(dockerClient.execStartCmd(anyString())).thenReturn(execStartCmd);
        when(execStartCmd.withDetach(anyBoolean())).thenReturn(execStartCmd);
        when(execStartCmd.withTty(anyBoolean())).thenReturn(execStartCmd);
        ExecStartResultCallback execStartResultCallback = mock(ExecStartResultCallback.class);
        PowerMockito.whenNew(ExecStartResultCallback.class)
                .withArguments(any(OutputStream.class), any(OutputStream.class)).thenReturn(execStartResultCallback);
        when(execStartCmd.exec(execStartResultCallback)).thenReturn(execStartResultCallback);
        doThrow(new RuntimeException()).when(execStartResultCallback).awaitCompletion();

        PowerMockito.when(byteArrayOutputStream.toString()).thenReturn(containerId);

        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);

        InspectExecResponse inspectExecResponse = mock(InspectExecResponse.class);
        when(inspectExecResponse.getContainerID()).thenReturn(containerId);
        InspectExecCmd inspectExecCmd = mock(InspectExecCmd.class);
        when(inspectExecCmd.exec()).thenReturn(inspectExecResponse);
        when(dockerClient.inspectExecCmd(anyString())).thenReturn(inspectExecCmd);

        try {
            customDockerClient.execCommandInContainerByName(containerName, "cp test");
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DOCKER_CONNECTION_FAILURE);
        }
        verify(dockerClient, times(1)).execCreateCmd(anyString());
        verify(dockerClient, times(1)).execStartCmd(anyString());
    }

    @Test(description = "verify the execution of command on the container---To verify the Processing Exception")
    public void testExecCommandInContainerByNametoCatchProcessException() throws Exception {
        ExecCreateCmd execCreateCmd = mock(ExecCreateCmd.class);
        when(dockerClient.execCreateCmd(anyString())).thenReturn(execCreateCmd);
        when(execCreateCmd.withCmd(anyString(), anyString(), anyString())).thenReturn(execCreateCmd);
        when(execCreateCmd.withTty(anyBoolean())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdin(anyBoolean())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdout(anyBoolean())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStderr(anyBoolean())).thenReturn(execCreateCmd);
        ExecCreateCmdResponse exec = mock(ExecCreateCmdResponse.class);
        when(execCreateCmd.exec()).thenReturn(exec);

        ProcessingException processingException = mock(ProcessingException.class);
        doThrow(processingException).when(execCreateCmd).exec();

        when(exec.getId()).thenReturn(containerId);
        ByteArrayOutputStream byteArrayOutputStream = mock(ByteArrayOutputStream.class);
        PowerMockito.whenNew(ByteArrayOutputStream.class).withNoArguments().thenReturn(byteArrayOutputStream);
        ExecStartCmd execStartCmd = mock(ExecStartCmd.class);
        when(dockerClient.execStartCmd(anyString())).thenReturn(execStartCmd);
        when(execStartCmd.withDetach(anyBoolean())).thenReturn(execStartCmd);
        when(execStartCmd.withTty(anyBoolean())).thenReturn(execStartCmd);
        ExecStartResultCallback execStartResultCallback = mock(ExecStartResultCallback.class);
        PowerMockito.whenNew(ExecStartResultCallback.class)
                .withArguments(any(OutputStream.class), any(OutputStream.class)).thenReturn(execStartResultCallback);
        when(execStartCmd.exec(execStartResultCallback)).thenReturn(execStartResultCallback);
        doThrow(new RuntimeException()).when(execStartResultCallback).awaitCompletion();
        PowerMockito.when(byteArrayOutputStream.toString()).thenReturn(containerId);


        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);

        InspectExecResponse inspectExecResponse = mock(InspectExecResponse.class);
        when(inspectExecResponse.getContainerID()).thenReturn(containerId);
        InspectExecCmd inspectExecCmd = mock(InspectExecCmd.class);
        when(inspectExecCmd.exec()).thenReturn(inspectExecResponse);
        when(dockerClient.inspectExecCmd(anyString())).thenReturn(inspectExecCmd);

        try {
            customDockerClient.execCommandInContainerByName(containerName, "cp test");
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DOCKER_CONNECTION_FAILURE);
        }
        verify(dockerClient, times(1)).execCreateCmd(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#execStartCommand(com.github.dockerjava.api.command.ExecCreateCmdResponse)}.
     *
     * @throws Exception
     */
    @Test
    public void testExecStartCommand() throws Exception {
        ExecCreateCmdResponse exec = mock(ExecCreateCmdResponse.class);
        when(exec.getId()).thenReturn(containerId);
        ByteArrayOutputStream byteArrayOutputStream = mock(ByteArrayOutputStream.class);
        PowerMockito.whenNew(ByteArrayOutputStream.class).withNoArguments().thenReturn(byteArrayOutputStream);
        ExecStartCmd execStartCmd = mock(ExecStartCmd.class);
        when(dockerClient.execStartCmd(anyString())).thenReturn(execStartCmd);
        when(execStartCmd.withDetach(anyBoolean())).thenReturn(execStartCmd);
        when(execStartCmd.withTty(anyBoolean())).thenReturn(execStartCmd);
        ExecStartResultCallback execStartResultCallback = mock(ExecStartResultCallback.class);
        PowerMockito.whenNew(ExecStartResultCallback.class)
                .withArguments(any(OutputStream.class), any(OutputStream.class)).thenReturn(execStartResultCallback);
        when(execStartCmd.exec(execStartResultCallback)).thenReturn(execStartResultCallback);
        when(execStartResultCallback.awaitCompletion()).thenReturn(execStartResultCallback);
        PowerMockito.when(byteArrayOutputStream.toString()).thenReturn(containerId);

        String actualResult = customDockerClient.execStartCommand(exec);
        Assert.assertEquals(actualResult, containerId);
        verify(dockerClient, times(1)).execStartCmd(anyString());
        PowerMockito.verifyNew(ByteArrayOutputStream.class, times(1)).withNoArguments();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#execInspectCommand(com.github.dockerjava.api.command.ExecCreateCmdResponse)}.
     */
    @Test
    public void testExecInspectCommand() {
        ExecCreateCmdResponse exec = mock(ExecCreateCmdResponse.class);
        when(exec.getId()).thenReturn(containerId);
        InspectExecResponse inspectExecResponse = mock(InspectExecResponse.class);
        when(inspectExecResponse.getContainerID()).thenReturn(containerId);
        InspectExecCmd inspectExecCmd = mock(InspectExecCmd.class);
        when(inspectExecCmd.exec()).thenReturn(inspectExecResponse);
        when(dockerClient.inspectExecCmd(anyString())).thenReturn(inspectExecCmd);
        InspectExecResponse actualResult = customDockerClient.execInspectCommand(exec);
        verify(dockerClient, times(1)).inspectExecCmd(anyString());
        Assert.assertEquals(actualResult.getContainerID(), containerId);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#stopContainerByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = Exception.class, description = "verify the stopping of container by container name")
    public void testStopContainerByNameFailed() throws MangleException {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);
        StopContainerCmd stopContainerCmd = mock(StopContainerCmd.class);
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        doThrow(RuntimeException.class).when(stopContainerCmd).exec();
        customDockerClient.stopContainerByName(containerName);
        verify(dockerClient, times(1)).listContainersCmd();
        verify(dockerClient, times(1)).stopContainerCmd(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#stopContainerByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testStopContainerByName() throws MangleException {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);
        StopContainerCmd stopContainerCmd = mock(StopContainerCmd.class);
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        Void v = PowerMockito.mock(Void.class);
        when(stopContainerCmd.exec()).thenReturn(v);
        customDockerClient.stopContainerByName(containerName);
        verify(dockerClient, times(1)).listContainersCmd();
        verify(dockerClient, times(1)).stopContainerCmd(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#startContainerByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = Exception.class, description = "verify the failure of starting of a container by container name")
    public void testStartContainerByNameFailure() throws MangleException {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { containerName });
        when(container.getId()).thenReturn(containerId);
        StartContainerCmd startContainerCmd = mock(StartContainerCmd.class);
        when(dockerClient.startContainerCmd(anyString())).thenReturn(startContainerCmd);
        doThrow(RuntimeException.class).when(startContainerCmd).exec();
        customDockerClient.startContainerByName(containerName);
        verify(dockerClient, times(1)).listContainersCmd();
        verify(dockerClient, times(1)).startContainerCmd(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#startContainerByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testStartContainerByName() throws MangleException {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);
        StartContainerCmd startContainerCmd = mock(StartContainerCmd.class);
        when(dockerClient.startContainerCmd(anyString())).thenReturn(startContainerCmd);
        Void v = PowerMockito.mock(Void.class);
        when(startContainerCmd.exec()).thenReturn(v);
        customDockerClient.startContainerByName(containerName);
        verify(dockerClient, times(1)).listContainersCmd();
        verify(dockerClient, times(1)).startContainerCmd(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#stopAndDeleteContainerByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = Exception.class, description = "verify the failure of stopping and deletion of a container by name")
    public void testStopAndDeleteContainerByNameFailure() throws MangleException {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);
        StopContainerCmd stopContainerCmd = mock(StopContainerCmd.class);
        RemoveContainerCmd removeContainerCmd = mock(RemoveContainerCmd.class);
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        doThrow(RuntimeException.class).when(stopContainerCmd).exec();
        doThrow(RuntimeException.class).when(removeContainerCmd).exec();
        customDockerClient.stopAndDeleteContainerByName(containerName);
        verify(dockerClient, times(1)).listContainersCmd();
        verify(dockerClient, times(1)).stopContainerCmd(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#stopAndDeleteContainerByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testStopAndDeleteContainerByName() throws MangleException {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);
        StopContainerCmd stopContainerCmd = mock(StopContainerCmd.class);
        RemoveContainerCmd removeContainerCmd = mock(RemoveContainerCmd.class);
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        Void v = PowerMockito.mock(Void.class);
        when(stopContainerCmd.exec()).thenReturn(v);
        when(removeContainerCmd.exec()).thenReturn(v);

        customDockerClient.stopAndDeleteContainerByName(containerName);
        verify(dockerClient, times(1)).listContainersCmd();
        verify(dockerClient, times(1)).stopContainerCmd(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#unPauseAllContainers()}.
     */
    @Test(expectedExceptions = Exception.class, description = "verify unpause all container method")
    public void testUnPauseAllContainersFailure() {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { containerName });
        when(container.getId()).thenReturn(containerId);
        UnpauseContainerCmd unpauseContainerCmd = mock(UnpauseContainerCmd.class);
        when(dockerClient.unpauseContainerCmd(anyString())).thenReturn(unpauseContainerCmd);
        doThrow(RuntimeException.class).when(unpauseContainerCmd).exec();

        customDockerClient.unPauseAllContainers();

        verify(dockerClient, times(1)).listContainersCmd();
        verify(dockerClient, times(1)).unpauseContainerCmd(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#unPauseAllContainers()}.
     */
    @Test
    public void testUnPauseAllContainers() {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { containerName });
        when(container.getId()).thenReturn(containerId);
        UnpauseContainerCmd unpauseContainerCmd = mock(UnpauseContainerCmd.class);
        when(dockerClient.unpauseContainerCmd(anyString())).thenReturn(unpauseContainerCmd);

        Void v = PowerMockito.mock(Void.class);
        when(unpauseContainerCmd.exec()).thenReturn(v);

        customDockerClient.unPauseAllContainers();

        verify(dockerClient, times(1)).listContainersCmd();
        verify(dockerClient, times(1)).unpauseContainerCmd(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#listProcessesContainerByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testListProcessesContainerByName() throws MangleException {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);
        TopContainerCmd topContainerCmd = mock(TopContainerCmd.class);
        when(dockerClient.topContainerCmd(anyString())).thenReturn(topContainerCmd);
        TopContainerResponse topContainerResponse = mock(TopContainerResponse.class);
        when(topContainerCmd.exec()).thenReturn(topContainerResponse);
        String[][] res = { { "test", "test1" }, { "12121", "43434" } };
        when(topContainerResponse.getProcesses()).thenReturn(res);

        String[][] actualRes = customDockerClient.listProcessesContainerByName(containerName);
        Assert.assertEquals(actualRes[0][0], "test");
        verify(dockerClient, times(1)).listContainersCmd();
        verify(dockerClient, times(1)).topContainerCmd(anyString());
    }


    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#deleteAllMatchingContainer(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = Exception.class, description = "Verify the failure in the deletion of the matching container")
    public void testDeleteAllMatchingContainerFailure() throws MangleException {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);

        StopContainerCmd stopContainerCmd = mock(StopContainerCmd.class);
        RemoveContainerCmd removeContainerCmd = mock(RemoveContainerCmd.class);
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        doThrow(RuntimeException.class).when(stopContainerCmd).exec();
        doThrow(RuntimeException.class).when(removeContainerCmd).exec();
        customDockerClient.deleteAllMatchingContainer(containerName);
        verify(dockerClient, times(3)).listContainersCmd();
        verify(dockerClient, times(1)).stopContainerCmd(anyString());
    }


    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#deleteAllMatchingContainer(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testDeleteAllMatchingContainer() throws MangleException {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);

        StopContainerCmd stopContainerCmd = mock(StopContainerCmd.class);
        RemoveContainerCmd removeContainerCmd = mock(RemoveContainerCmd.class);
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);

        Void v = PowerMockito.mock(Void.class);
        when(stopContainerCmd.exec()).thenReturn(v);
        when(removeContainerCmd.exec()).thenReturn(v);

        customDockerClient.deleteAllMatchingContainer(containerName);
        verify(dockerClient, times(3)).listContainersCmd();
        verify(dockerClient, times(1)).stopContainerCmd(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#getAllDockerImages(com.github.dockerjava.api.DockerClient)}.
     */
    @Test
    public void testGetAllDockerImages() {
        ListImagesCmd listImagesCmd = mock(ListImagesCmd.class);
        when(dockerClient.listImagesCmd()).thenReturn(listImagesCmd);
        Image image = mock(Image.class);
        List<Image> imageList = new ArrayList<>();
        imageList.add(image);
        when(listImagesCmd.exec()).thenReturn(imageList);
        when(image.getId()).thenReturn("image1");

        List<Image> actualRes = customDockerClient.getAllDockerImages(dockerClient);
        Assert.assertEquals(actualRes.size(), 1);
        verify(dockerClient, times(1)).listImagesCmd();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#getAllContainers()}.
     */
    @Test
    public void testGetAllContainers() {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);

        List<Container> actualRes = customDockerClient.getAllContainers();
        Assert.assertEquals(actualRes.size(), 1);
        verify(dockerClient, times(1)).listContainersCmd();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#getAllDockerImageNames(com.github.dockerjava.api.DockerClient)}.
     */
    @Test
    public void testGetAllDockerImageNames() {
        ListImagesCmd listImagesCmd = mock(ListImagesCmd.class);
        when(dockerClient.listImagesCmd()).thenReturn(listImagesCmd);
        Image image = mock(Image.class);
        List<Image> imageList = new ArrayList<>();
        imageList.add(image);
        when(listImagesCmd.exec()).thenReturn(imageList);
        when(image.getId()).thenReturn("image1");

        List<Image> actualRes = customDockerClient.getAllDockerImageNames(dockerClient);
        Assert.assertEquals(actualRes.size(), 1);
        verify(dockerClient, times(1)).listImagesCmd();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#pullImage(java.lang.String)}.
     *
     * @throws Exception
     */
    @Test
    public void testPullImage() throws Exception {
        PullImageCmd pullImageCmd = mock(PullImageCmd.class);
        when(dockerClient.pullImageCmd(anyString())).thenReturn(pullImageCmd);
        PullImageResultCallback pullImageResultCallback = mock(PullImageResultCallback.class);
        PowerMockito.whenNew(PullImageResultCallback.class).withNoArguments().thenReturn(pullImageResultCallback);
        when(pullImageCmd.exec(pullImageResultCallback)).thenReturn(pullImageResultCallback);
        doNothing().when(pullImageResultCallback).awaitSuccess();
        customDockerClient.pullImage("image11");
        PowerMockito.verifyNew(PullImageResultCallback.class, times(1)).withNoArguments();
        verify(dockerClient, times(1)).pullImageCmd(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#getContainerByName(java.lang.String)}.
     */
    @Test
    public void testGetContainerByName() {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);
        Container actualRes = customDockerClient.getContainerByName(containerName);
        Assert.assertEquals(actualRes.getId(), containerId);
        verify(dockerClient, times(1)).listContainersCmd();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#getContainerByName(java.lang.String)}.
     */
    @Test(description = "verifying the getContainerByName, which should return when the containerName doesn't match of the running container")
    public void testGetContainerByName2() {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] {});
        when(container.getId()).thenReturn(containerId);
        Container actualRes = customDockerClient.getContainerByName(containerName);
        Assert.assertNull(actualRes);
        verify(dockerClient, times(1)).listContainersCmd();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#isContainerRunningByID(java.lang.String)}.
     */
    @Test
    public void testIsContainerRunningByID() {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { containerName });
        when(container.getId()).thenReturn(containerId);
        Assert.assertTrue(customDockerClient.isContainerRunningByID(containerId));
        verify(dockerClient, times(1)).listContainersCmd();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#isContainerRunningByID(java.lang.String)}.
     */
    @Test(description = "verify the failure of the getContainerByID, matching the given containerId")
    public void testIsContainerRunningByID2() {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] {});
        when(container.getId()).thenReturn("test2");
        Assert.assertFalse(customDockerClient.isContainerRunningByID(containerId));
        verify(dockerClient, times(1)).listContainersCmd();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#isContainerPaused(java.lang.String)}.
     */
    @Test
    public void testIsContainerPaused() {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { containerName });
        when(container.getId()).thenReturn(containerId);
        Assert.assertTrue(customDockerClient.isContainerPaused(containerId));
        verify(dockerClient, times(1)).listContainersCmd();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#isContainerPaused(java.lang.String)}.
     */
    @Test(description = "verify the failure of the container pausing")
    public void testIsContainerPaused2() {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { containerName });
        when(container.getId()).thenReturn("test2");
        Assert.assertFalse(customDockerClient.isContainerPaused(containerId));
        verify(dockerClient, times(1)).listContainersCmd();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#isPortAvailableToConsume(java.lang.Integer)}.
     */
    @Test
    public void testIsPortAvailableToConsume() {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { containerName });
        when(container.getId()).thenReturn(containerId);
        ContainerPort containerPort = mock(ContainerPort.class);
        when(containerPort.getPublicPort()).thenReturn(3434);
        ContainerPort[] arrContainerPort = { containerPort };
        when(container.getPorts()).thenReturn(arrContainerPort);
        Assert.assertFalse(customDockerClient.isPortAvailableToConsume(3434));
        verify(dockerClient, times(1)).listContainersCmd();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#isPortAvailableToConsume(java.lang.Integer)}.
     */
    @Test(description = "verify that the port availability check returns true")
    public void testIsPortAvailableToConsume2() {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { containerName });
        when(container.getId()).thenReturn(containerId);
        ContainerPort containerPort = mock(ContainerPort.class);
        when(containerPort.getPublicPort()).thenReturn(3436);
        ContainerPort[] arrContainerPort = { containerPort };
        when(container.getPorts()).thenReturn(arrContainerPort);
        Assert.assertTrue(customDockerClient.isPortAvailableToConsume(3434));
        verify(dockerClient, times(1)).listContainersCmd();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#copyFileToContainerByName(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCopyFileToContainerByNameDockerClientFailure() throws Exception {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);
        CopyArchiveToContainerCmd containerCmd = mock(CopyArchiveToContainerCmd.class);
        when(dockerClient.copyArchiveToContainerCmd(anyString())).thenReturn(containerCmd);
        when(containerCmd.withRemotePath(anyString())).thenReturn(containerCmd);
        when(containerCmd.withHostResource(anyString())).thenReturn(containerCmd);
        doThrow(DockerClientException.class).when(containerCmd).exec();
        boolean exceptionCalled = false;
        try {
            customDockerClient.copyFileToContainerByName(containerName, "/test", "/test");
        } catch (Exception e) {
            exceptionCalled = true;

        }
        Assert.assertTrue(exceptionCalled);
        verify(dockerClient, times(1)).listContainersCmd();
        verify(dockerClient, times(1)).copyArchiveToContainerCmd(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#copyFileToContainerByName(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCopyFileToContainerByNameNotFoundFailure() throws Exception {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);
        CopyArchiveToContainerCmd containerCmd = mock(CopyArchiveToContainerCmd.class);
        when(dockerClient.copyArchiveToContainerCmd(anyString())).thenReturn(containerCmd);
        when(containerCmd.withRemotePath(anyString())).thenReturn(containerCmd);
        when(containerCmd.withHostResource(anyString())).thenReturn(containerCmd);
        doThrow(new NotFoundException("Directory not found")).when(containerCmd).exec();
        boolean exceptionCalled = false;
        try {
            customDockerClient.copyFileToContainerByName(containerName, "/test", "/test");
        } catch (Exception e) {
            exceptionCalled = true;

        }
        Assert.assertTrue(exceptionCalled);
        verify(dockerClient, times(1)).listContainersCmd();
        verify(dockerClient, times(1)).copyArchiveToContainerCmd(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#copyFileToContainerByName(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCopyFileToContainerByNameclientProtocolfailure() throws Exception {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);
        CopyArchiveToContainerCmd containerCmd = mock(CopyArchiveToContainerCmd.class);
        when(dockerClient.copyArchiveToContainerCmd(anyString())).thenReturn(containerCmd);
        when(containerCmd.withRemotePath(anyString())).thenReturn(containerCmd);
        when(containerCmd.withHostResource(anyString())).thenReturn(containerCmd);
        doThrow(new ProcessingException("org.apache.http.client.ClientProtocolException")).when(containerCmd).exec();
        ProcessingException exception = mock(ProcessingException.class);
        when(exception.getMessage()).thenReturn("org.apache.http.client.ClientProtocolException");
        boolean exceptionCalled = false;
        try {
            customDockerClient.copyFileToContainerByName(containerName, "/test", "/test");
        } catch (Exception e) {
            exceptionCalled = true;
        }
        Assert.assertTrue(exceptionCalled);
        verify(dockerClient, times(1)).listContainersCmd();
        verify(dockerClient, times(1)).copyArchiveToContainerCmd(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#copyFileToContainerByName(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCopyFileToContainerByName() throws Exception {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withStatusFilter(any())).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);
        CopyArchiveToContainerCmd containerCmd = mock(CopyArchiveToContainerCmd.class);
        when(dockerClient.copyArchiveToContainerCmd(anyString())).thenReturn(containerCmd);
        when(containerCmd.withRemotePath(anyString())).thenReturn(containerCmd);
        when(containerCmd.withHostResource(anyString())).thenReturn(containerCmd);
        Assert.assertTrue(customDockerClient.copyFileToContainerByName(containerName, "/test", "/test"));
        verify(dockerClient, times(1)).listContainersCmd();
        verify(dockerClient, times(1)).copyArchiveToContainerCmd(anyString());
    }

    /**
     * Test method for {@link com.vmware.mangle.clients.docker.CustomDockerClient#testConnection()}.
     *
     * @throws MangleException
     */
    @Test(description = "verify the test connection to the docker host is failed")
    public void testTestConnectionFailure() throws MangleException {
        PingCmd pingCmd = mock(PingCmd.class);
        when(dockerClient.pingCmd()).thenReturn(pingCmd);
        doThrow(new DockerClientException("ping test failed")).when(pingCmd).exec();
        try {
            Assert.assertFalse(customDockerClient.testConnection());
        } catch (MangleException exception) {
            Assert.assertTrue(true);
            verify(dockerClient, times(1)).pingCmd();
        }
    }

    /**
     * Test method for {@link com.vmware.mangle.clients.docker.CustomDockerClient#testConnection()}.
     *
     * @throws MangleException
     */
    @Test(description = "verify the test connection to the docker host")
    public void testTestConnection() throws MangleException {
        PingCmd pingCmd = mock(PingCmd.class);
        when(dockerClient.pingCmd()).thenReturn(pingCmd);

        Void v = PowerMockito.mock(Void.class);
        when(pingCmd.exec()).thenReturn(v);

        doReturn(v).when(pingCmd).exec();

        Assert.assertTrue(customDockerClient.testConnection());
        verify(dockerClient, times(1)).pingCmd();
    }


    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#execCreateCmdByContainerName(java.lang.String)}.
     */
    @Test(description = "retrieve the container ip address for the given container id, should return null if the container id is not found")
    public void testGetDockerIp() {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        new Container();
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { containerName });
        when(container.getId()).thenReturn(containerId);
        when(container.getNetworkSettings()).thenReturn(null);

        String actualResult = customDockerClient.getDockerIP(containerId);
        Assert.assertNull(actualResult);
        verify(dockerClient, times(1)).listContainersCmd();
    }


    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#execCreateCmdByContainerName(java.lang.String)}.
     */
    @Test(description = "retrieve the container ip address for the given container id, should return null if the container id is not found")
    public void testGetDockerIp2() {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        new Container();
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { containerName });
        when(container.getId()).thenReturn("test2");
        when(container.getNetworkSettings()).thenReturn(null);

        String actualResult = customDockerClient.getDockerIP(containerId);
        Assert.assertNull(actualResult);
        verify(dockerClient, times(1)).listContainersCmd();
    }


    @Test(description = "retrieve the container ip address for the given container name, should return null if the container name is not found")
    public void testGetDockerIPByName() throws MangleException {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });
        when(container.getId()).thenReturn(containerId);

        String ip = customDockerClient.getDockerIPByName(containerName);
        Assert.assertNull(ip);
        verify(dockerClient, times(2)).listContainersCmd();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#getAllContainerNames()}.
     *
     * Description: Test case to validate the getAllcontainerNames method returning the list of
     * container names in Docker Host.
     *
     * @throws MangleException
     */
    @Test
    public void testGetAllContainerNames() throws MangleException {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        Container container = mock(Container.class);
        List<Container> allContainers = new ArrayList<>();
        allContainers.add(container);
        when(listContainersCmd.exec()).thenReturn(allContainers);
        when(container.getNames()).thenReturn(new String[] { "/" + containerName });

        List<String> expectedContainerNames = new ArrayList<String>();
        expectedContainerNames.add("test");
        List<String> resultContainerNames = customDockerClient.getAllContainerNames();

        verify(dockerClient, times(1)).listContainersCmd();
        Assert.assertEquals(resultContainerNames, expectedContainerNames);
        Assert.assertEquals(resultContainerNames.size(), 1);
    }


    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.CustomDockerClient#getAllContainerNames()}.
     *
     * Description: Test case to validate the ProcessingException thrown in getAllcontainerNames
     * method.
     *
     * @throws MangleException
     */
    @Test
    public void testGetAllContainerNames_DockerConnectionFailure() throws MangleException {
        ListContainersCmd listContainersCmd = mock(ListContainersCmd.class);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        ProcessingException processingException = mock(ProcessingException.class);
        doThrow(processingException).when(listContainersCmd).exec();
        try {
            customDockerClient.getAllContainerNames();
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DOCKER_CONNECTION_FAILURE);
            verify(dockerClient, times(1)).listContainersCmd();
        }
    }
}
