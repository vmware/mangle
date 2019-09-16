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

package com.vmware.mangle.unittest.faults.plugin.helpers.docker;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.utils.DockerCommandUtils;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 * Unit Tests for DockerFaultHelper class
 *
 * @author rpraveen,jayasankarr
 */


public class DockerCommandUtilsTest {
    private String command = "DOCKER_PAUSE:--containerName container";

    private FaultsMockData mockData = new FaultsMockData();

    @Mock
    private EndpointClientFactory factory;

    private CommandExecutionFaultSpec jvmAgentFaultSpec;

    @Mock
    private CustomDockerClient customDockerClient;


    private DockerCommandUtils dockerCommandUtils;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);


    }


    @Test
    public void testRunCommand() throws MangleException {

        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        commandExecutionResult.setCommandOutput("Precheck:failed");
        commandExecutionResult.setExitCode(127);


        jvmAgentFaultSpec = mockData.getDockerPauseFaultSpec();
        jvmAgentFaultSpec.setDockerArguments(mockData.getDockerSpecificArguments());

        CustomDockerClient customDockerClient = Mockito.mock(CustomDockerClient.class);

        Mockito.when(factory.getEndPointClient(any(), any())).thenReturn(customDockerClient);

        Mockito.when(customDockerClient.execCommandInContainerByName(any(), any())).thenReturn(commandExecutionResult);
        dockerCommandUtils = new DockerCommandUtils(jvmAgentFaultSpec, factory);
        CommandExecutionResult actualResult = dockerCommandUtils.executeCommand(command);
        Assert.assertEquals(actualResult, commandExecutionResult);
    }

    @Test
    public void testRunCommandFailure() throws MangleException {
        String command = "DOCKER_PAUSE:--containerName container";
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        commandExecutionResult.setCommandOutput("Precheck:failed");
        commandExecutionResult.setExitCode(127);
        jvmAgentFaultSpec = mockData.getDockerPauseFaultSpec();
        jvmAgentFaultSpec.setDockerArguments(mockData.getDockerSpecificArguments());
        CustomDockerClient customDockerClient = Mockito.mock(CustomDockerClient.class);
        Mockito.when(factory.getEndPointClient(any(), any())).thenReturn(customDockerClient);
        doThrow(new MangleException(ErrorConstants.DOCKER_CONNECTION_FAILURE, ErrorCode.DOCKER_CONNECTION_FAILURE))
                .when(customDockerClient).execCommandInContainerByName(any(), any());
        dockerCommandUtils = new DockerCommandUtils(jvmAgentFaultSpec, factory);
        CommandExecutionResult actualResult = dockerCommandUtils.executeCommand(command);
        Assert.assertEquals(actualResult.getExitCode(), 1, "Run command succeeded but mangle exception is expected");
        Assert.assertEquals(actualResult.getCommandOutput(), ErrorConstants.DOCKER_CONNECTION_FAILURE);
    }


}
