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

package com.vmware.mangle.unittest.utils.clients.redis;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.RedisProxyConnectionProperties;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.clients.redis.RedisProxyClient;
import com.vmware.mangle.utils.clients.redis.RedisProxyCommandConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit test cases for {@link RedisProxyClient}.
 *
 * @author kumargautam
 */
@PrepareForTest(value = { CommandUtils.class })
public class RedisProxyClientTest extends PowerMockTestCase {

    private RedisProxyClient redisProxyClient;
    private CommandExecutionResult commandExecutionResult;

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(CommandUtils.class);
        this.redisProxyClient = RedisProxyClient.getClient();
    }

    /**
     * Test method for {@link com.vmware.mangle.utils.clients.redis.RedisProxyClient#getClient()}.
     */
    @Test
    public void testGetClient() {
        RedisProxyClient client = RedisProxyClient.getClient();
        assertNotNull(client);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.clients.redis.RedisProxyClient#testConnection()}.
     *
     * @throws MangleException
     */
    @Test
    public void testTestConnection() throws MangleException {
        mockTest();
        PowerMockito.mockStatic(CommandUtils.class);
        PowerMockito.when(CommandUtils.runCommand(anyString())).thenReturn(commandExecutionResult);
        assertTrue(redisProxyClient.testConnection());
        PowerMockito.verifyStatic(CommandUtils.class, times(1));
        CommandUtils.runCommand(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.clients.redis.RedisProxyClient#testConnection()}.
     *
     */
    @Test
    public void testTestConnectionForMangleException() {
        mockTest();
        this.commandExecutionResult.setExitCode(127);
        PowerMockito.mockStatic(CommandUtils.class);
        PowerMockito.when(CommandUtils.runCommand(anyString())).thenReturn(commandExecutionResult);
        try {
            redisProxyClient.testConnection();
            fail("test testConnection() for MangleException failed");
        } catch (MangleException e) {
            assertEquals(e.getErrorCode(), ErrorCode.REDIS_PROXY_CONN_ERROR);
        }
        PowerMockito.verifyStatic(CommandUtils.class, times(1));
        CommandUtils.runCommand(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.utils.clients.redis.RedisProxyClient#executeCommand(java.lang.String)}.
     */
    @Test
    public void testExecuteCommand() {
        mockTest();
        this.commandExecutionResult.setExitCode(0);
        PowerMockito.mockStatic(CommandUtils.class);
        PowerMockito.when(CommandUtils.runCommand(anyString())).thenReturn(commandExecutionResult);
        assertEquals(redisProxyClient.executeCommand(RedisProxyCommandConstants.RULE_LIST), commandExecutionResult);
        PowerMockito.verifyStatic(CommandUtils.class, times(1));
        CommandUtils.runCommand(anyString());
    }

    private void mockTest() {
        RedisProxyConnectionProperties connectionProperties = new RedisProxyConnectionProperties();
        connectionProperties.setHost("10.3.45.67");
        connectionProperties.setPort(6380);
        this.redisProxyClient.setRedisProxyConnectionProperties(connectionProperties);
        this.commandExecutionResult = new CommandExecutionResult();
        this.commandExecutionResult.setCommandOutput("OK");
        this.commandExecutionResult.setExitCode(0);
    }
}
