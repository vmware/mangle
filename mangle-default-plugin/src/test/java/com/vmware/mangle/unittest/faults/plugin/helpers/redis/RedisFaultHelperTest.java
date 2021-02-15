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

package com.vmware.mangle.unittest.faults.plugin.helpers.redis;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisDelayFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisReturnErrorFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.redis.RedisFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.clients.redis.RedisProxyClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit test cases for {@link RedisFaultHelper}.
 *
 * @author kumargautam
 */
class RedisFaultHelperTest {

    @Mock
    private EndpointClientFactory endpointClientFactory;
    private RedisFaultHelper redisFaultHelper;
    private FaultsMockData faultsMockData;
    private RedisProxyClient commandExecutor;

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        redisFaultHelper = spy(new RedisFaultHelper(endpointClientFactory));
        faultsMockData = new FaultsMockData();
        commandExecutor = RedisProxyClient.getClient();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.redis.RedisFaultHelper#getExecutor(com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetExecutor() throws MangleException {
        RedisDelayFaultSpec delayFaultSpec = faultsMockData.getRedisDelayFaultSpec();
        when(endpointClientFactory.getEndPointClient(any(CredentialsSpec.class), any(EndpointSpec.class)))
                .thenReturn((EndpointClient) commandExecutor);
        redisFaultHelper.getAgentFaultInjectionScripts();
        assertTrue(redisFaultHelper.getExecutor(delayFaultSpec) instanceof RedisProxyClient);
        verify(endpointClientFactory, times(1)).getEndPointClient(any(CredentialsSpec.class), any(EndpointSpec.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.redis.RedisFaultHelper#getInjectionCommandInfoList(com.vmware.mangle.utils.ICommandExecutor, com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetInjectionCommandInfoListForRedisDelayFault() throws MangleException {
        RedisDelayFaultSpec delayFaultSpec = faultsMockData.getRedisDelayFaultSpec();
        List<CommandInfo> commandList = redisFaultHelper.getInjectionCommandInfoList(commandExecutor, delayFaultSpec);
        assertTrue(!commandList.isEmpty());
        String actualCommand = commandList.get(0).getCommand();
        assertEquals(actualCommand,
                "ruleadd redisDbDelayFault_" + getRuleName(delayFaultSpec) + " delay=1000 percentage=50");
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.redis.RedisFaultHelper#getRemediationCommandInfoList(com.vmware.mangle.utils.ICommandExecutor, com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetRemediationCommandInfoListForRedisDelayFault() throws MangleException {
        RedisDelayFaultSpec delayFaultSpec = faultsMockData.getRedisDelayFaultSpec();
        List<CommandInfo> commandList = redisFaultHelper.getRemediationCommandInfoList(commandExecutor, delayFaultSpec);
        assertTrue(!commandList.isEmpty());
        String actualCommand = commandList.get(0).getCommand();
        assertEquals(actualCommand, "ruledel redisDbDelayFault_" + getRuleName(delayFaultSpec));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.redis.RedisFaultHelper#getInjectionCommandInfoList(com.vmware.mangle.utils.ICommandExecutor, com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetInjectionCommandInfoListForRedisReturnErrorFault() throws MangleException {
        RedisReturnErrorFaultSpec faultSpec = faultsMockData.getRedisReturnErrorFaultSpec();
        List<CommandInfo> commandList = redisFaultHelper.getInjectionCommandInfoList(commandExecutor, faultSpec);
        assertTrue(!commandList.isEmpty());
        String actualCommand = commandList.get(0).getCommand();
        assertEquals(actualCommand,
                "ruleadd redisDbReturnErrorFault_" + getRuleName(faultSpec) + " return_err=CON percentage=50");
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.redis.RedisFaultHelper#getRemediationCommandInfoList(com.vmware.mangle.utils.ICommandExecutor, com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetRemediationCommandInfoListForRedisReturnErrorFault() throws MangleException {
        RedisReturnErrorFaultSpec faultSpec = faultsMockData.getRedisReturnErrorFaultSpec();
        List<CommandInfo> commandList = redisFaultHelper.getRemediationCommandInfoList(commandExecutor, faultSpec);
        assertTrue(!commandList.isEmpty());
        String actualCommand = commandList.get(0).getCommand();
        assertEquals(actualCommand, "ruledel redisDbReturnErrorFault_" + getRuleName(faultSpec));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.redis.RedisFaultHelper#getInjectionCommandInfoList(com.vmware.mangle.utils.ICommandExecutor, com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetInjectionCommandInfoListForRedisReturnEmptyFault() throws MangleException {
        RedisFaultSpec faultSpec = faultsMockData.getRedisFaultSpec();
        List<CommandInfo> commandList = redisFaultHelper.getInjectionCommandInfoList(commandExecutor, faultSpec);
        assertTrue(!commandList.isEmpty());
        String actualCommand = commandList.get(0).getCommand();
        assertEquals(actualCommand,
                "ruleadd redisDbReturnEmptyFault_" + getRuleName(faultSpec) + " return_empty=true percentage=25");
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.redis.RedisFaultHelper#getRemediationCommandInfoList(com.vmware.mangle.utils.ICommandExecutor, com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetRemediationCommandInfoListForRedisReturnEmptyFault() throws MangleException {
        RedisFaultSpec faultSpec = faultsMockData.getRedisFaultSpec();
        List<CommandInfo> commandList = redisFaultHelper.getRemediationCommandInfoList(commandExecutor, faultSpec);
        assertTrue(!commandList.isEmpty());
        String actualCommand = commandList.get(0).getCommand();
        assertEquals(actualCommand, "ruledel redisDbReturnEmptyFault_" + getRuleName(faultSpec));
    }


    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.redis.RedisFaultHelper#getInjectionCommandInfoList(com.vmware.mangle.utils.ICommandExecutor, com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetInjectionCommandInfoListForRedisDropConnectionFault() throws MangleException {
        RedisFaultSpec faultSpec = faultsMockData.getRedisDropConnectionFaultSpec();
        List<CommandInfo> commandList = redisFaultHelper.getInjectionCommandInfoList(commandExecutor, faultSpec);
        assertTrue(!commandList.isEmpty());
        String actualCommand = commandList.get(0).getCommand();
        assertEquals(actualCommand,
                "ruleadd redisDbDropConnectionFault_" + getRuleName(faultSpec) + " drop=true percentage=25");
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.redis.RedisFaultHelper#getRemediationCommandInfoList(com.vmware.mangle.utils.ICommandExecutor, com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetRemediationCommandInfoListForRedisDropConnectionFault() throws MangleException {
        RedisFaultSpec faultSpec = faultsMockData.getRedisDropConnectionFaultSpec();
        List<CommandInfo> commandList = redisFaultHelper.getRemediationCommandInfoList(commandExecutor, faultSpec);
        assertTrue(!commandList.isEmpty());
        String actualCommand = commandList.get(0).getCommand();
        assertEquals(actualCommand, "ruledel redisDbDropConnectionFault_" + getRuleName(faultSpec));
    }

    /**
     * Test method for
     * {@link RedisFaultHelper#validateExistingRedisFault(com.vmware.mangle.utils.ICommandExecutor)}.
     *
     * @throws MangleException
     */
    @Test
    public void testValidateExistingRedisFault() throws MangleException {
        RedisProxyClient commandExecutor = mock(RedisProxyClient.class);
        CommandExecutionResult result = new CommandExecutionResult();
        result.setExitCode(0);
        when(commandExecutor.executeCommand(anyString())).thenReturn(result);
        redisFaultHelper.validateExistingRedisFault(commandExecutor);
        verify(commandExecutor, times(1)).executeCommand(anyString());
    }

    /**
     * Test method for
     * {@link RedisFaultHelper#validateExistingRedisFault(com.vmware.mangle.utils.ICommandExecutor)}.
     *
     * @throws MangleException
     */
    @Test
    public void testValidateExistingRedisFaultForParallelExecutionError() {
        RedisProxyClient commandExecutor = mock(RedisProxyClient.class);
        CommandExecutionResult result = new CommandExecutionResult();
        result.setExitCode(0);
        result.setCommandOutput("test");
        when(commandExecutor.executeCommand(anyString())).thenReturn(result);

        try {
            redisFaultHelper.validateExistingRedisFault(commandExecutor);
        } catch (MangleException e) {
            assertEquals(e.getErrorCode(), ErrorCode.REDIS_FAULT_PARALLEL_EXECUTION_ERROR);
            verify(commandExecutor, times(1)).executeCommand(anyString());
        }
    }

    /**
     * Test method for
     * {@link RedisFaultHelper#validateExistingRedisFault(com.vmware.mangle.utils.ICommandExecutor)}.
     *
     * @throws MangleException
     */
    @Test
    public void testValidateExistingRedisFaultForMangleException() {
        RedisProxyClient commandExecutor = mock(RedisProxyClient.class);
        CommandExecutionResult result = new CommandExecutionResult();
        result.setExitCode(1);
        result.setCommandOutput("test");
        when(commandExecutor.executeCommand(anyString())).thenReturn(result);

        try {
            redisFaultHelper.validateExistingRedisFault(commandExecutor);
        } catch (MangleException e) {
            assertEquals(e.getErrorCode(), ErrorCode.COMMAND_EXEC_EXIT_CODE_ERROR);
            verify(commandExecutor, times(1)).executeCommand(anyString());
        }
    }

    private String getRuleName(CommandExecutionFaultSpec faultSpec) {
        return faultSpec.getEndpoint().getRedisProxyConnectionProperties().getHost();
    }
}
