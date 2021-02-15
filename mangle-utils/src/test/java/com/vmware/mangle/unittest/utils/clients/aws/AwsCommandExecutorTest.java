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

package com.vmware.mangle.unittest.utils.clients.aws;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.model.aws.AwsService;
import com.vmware.mangle.utils.ICommandClientExecutor;
import com.vmware.mangle.utils.clients.aws.AWSCommandExecutor;
import com.vmware.mangle.utils.clients.aws.CustomAwsClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author bkaranam
 */
public class AwsCommandExecutorTest {

    @Mock
    private CustomAwsClient awsClient;

    private String command = "STOP_INSTANCES:--instancesIds i-100,i-101";

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAwsCommandExecutorInstantiation() {
        AWSCommandExecutor executor = new AWSCommandExecutor(awsClient, AwsService.EC2);
        Assert.assertNotNull(executor);
        executor = new AWSCommandExecutor(awsClient, AwsService.RDS);
        Assert.assertNotNull(executor);
    }

    @Test
    public void testExecuteCommand() throws MangleException {
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        commandExecutionResult.setExitCode(0);
        AWSCommandExecutor executor = spy(new AWSCommandExecutor(awsClient, AwsService.EC2));
        doReturn(commandExecutionResult).when((ICommandClientExecutor) executor).callOperation(any(), any());
        CommandExecutionResult result = executor.executeCommand(command);
        Assert.assertEquals(result.getExitCode(), 0);
    }

    @Test
    public void testExecuteCommandFailure() throws MangleException {
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        commandExecutionResult.setExitCode(0);
        AWSCommandExecutor executor = spy(new AWSCommandExecutor(awsClient, AwsService.EC2));
        doThrow(new MangleException(ErrorCode.GENERIC_ERROR)).when((ICommandClientExecutor) executor)
                .callOperation(any(), any());
        CommandExecutionResult result = executor.executeCommand(command);
        Assert.assertEquals(result.getExitCode(), 1);
    }
}
