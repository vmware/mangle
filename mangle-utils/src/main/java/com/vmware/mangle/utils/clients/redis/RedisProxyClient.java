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

package com.vmware.mangle.utils.clients.redis;

import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.cassandra.model.endpoint.RedisProxyConnectionProperties;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Client class for RedisProxy.
 *
 * @author kumargautam
 */
@Log4j2
public class RedisProxyClient implements EndpointClient, ICommandExecutor {

    private static final String SPACE_DELIMITER = " ";
    private String redisCli;
    private RedisProxyConnectionProperties redisProxyConnectionProperties;

    private RedisProxyClient() {
        this.redisCli = "redis-cli";
    }

    public static RedisProxyClient getClient() {
        return new RedisProxyClient();
    }

    public void setRedisProxyConnectionProperties(RedisProxyConnectionProperties redisProxyConnectionProperties) {
        this.redisProxyConnectionProperties = redisProxyConnectionProperties;
    }

    private String getRedisCliCommand() {
        return this.redisCli + " -h " + this.redisProxyConnectionProperties.getHost() + " -p "
                + this.redisProxyConnectionProperties.getPort();
    }

    @Override
    public boolean testConnection() throws MangleException {
        CommandExecutionResult output = executeCommand(RedisProxyCommandConstants.RULE_LIST);
        if (output.getExitCode() != 0) {
            throw new MangleException(ErrorCode.REDIS_PROXY_CONN_ERROR, output.getCommandOutput());
        }
        return true;
    }

    @Override
    public CommandExecutionResult executeCommand(String command) {
        CommandExecutionResult result = CommandUtils.runCommand(getRedisCliCommand() + SPACE_DELIMITER + command);
        log.trace("Executed command: " + redisCli + SPACE_DELIMITER + command + "Command execution Result: " + result);
        return result;
    }
}
