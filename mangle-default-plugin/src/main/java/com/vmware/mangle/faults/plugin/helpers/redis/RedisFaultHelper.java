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

package com.vmware.mangle.faults.plugin.helpers.redis;

import static com.vmware.mangle.utils.constants.FaultConstants.OPERATION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisDelayFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisDropConnectionFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisReturnErrorFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.KnownFailuresHelper;
import com.vmware.mangle.services.enums.RedisFaultName;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.skeletons.ICommandExecutionFaultHelper;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.redis.RedisProxyCommandConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Implementation of {@link ICommandExecutionFaultHelper} to support Redis db specific faults.
 *
 * @author kumargautam
 */
public class RedisFaultHelper implements ICommandExecutionFaultHelper {

    private EndpointClientFactory endpointClientFactory;

    @Autowired
    public RedisFaultHelper(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;
    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec faultSpec) throws MangleException {
        return (ICommandExecutor) endpointClientFactory.getEndPointClient(faultSpec.getCredentials(),
                faultSpec.getEndpoint());
    }

    @Override
    public List<CommandInfo> getInjectionCommandInfoList(ICommandExecutor executor, CommandExecutionFaultSpec faultSpec)
            throws MangleException {
        String operation = faultSpec.getArgs().get(OPERATION);
        List<CommandInfo> commandList = null;
        switch (RedisFaultName.valueOf(operation.toUpperCase())) {
        case REDISDBDELAYFAULT:
            commandList = getDelayInjectionCommandList(faultSpec);
            break;
        case REDISDBRETURNERRORFAULT:
            commandList = getReturnErrorInjectionCommandList(faultSpec);
            break;
        case REDISDBRETURNEMPTYFAULT:
            commandList = getReturnEmptyInjectionCommandList(faultSpec);
            break;
        case REDISDBDROPCONNECTIONFAULT:
            commandList = getDropConnectionInjectionCommandList(faultSpec);
            break;
        default:
            throw new MangleException(ErrorCode.UNSUPPORTED_FAULT, operation);
        }
        return commandList;
    }

    @Override
    public List<CommandInfo> getRemediationCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec faultSpec) throws MangleException {
        String operation = faultSpec.getArgs().get(OPERATION);
        List<CommandInfo> commandList = null;
        switch (RedisFaultName.valueOf(operation.toUpperCase())) {
        case REDISDBDELAYFAULT:
            commandList = getRemediationCommandList(faultSpec);
            break;
        case REDISDBRETURNERRORFAULT:
            commandList = getRemediationCommandList(faultSpec);
            break;
        case REDISDBRETURNEMPTYFAULT:
            commandList = getRemediationCommandList(faultSpec);
            break;
        case REDISDBDROPCONNECTIONFAULT:
            commandList = getRemediationCommandList(faultSpec);
            break;
        default:
            throw new MangleException(ErrorCode.UNSUPPORTED_FAULT, operation);
        }
        return commandList;
    }

    @Override
    public void checkTaskSpecificPrerequisites() throws MangleException {
        // Right not identified any task specific prerequisites to implement
    }

    @Override
    public List<SupportScriptInfo> getAgentFaultInjectionScripts() {
        // Right now no Injection scripts required for this task
        return Collections.emptyList();
    }

    private List<CommandInfo> getDelayInjectionCommandList(CommandExecutionFaultSpec commandExecutionFaultSpec) {
        RedisDelayFaultSpec faultSpec = (RedisDelayFaultSpec) commandExecutionFaultSpec;
        List<CommandInfo> commandInfos = new ArrayList<>();
        CommandInfo commandInfo = CommandInfo
                .builder(String.format(RedisProxyCommandConstants.RULE_ADD_FOR_DELAY, getRuleName(faultSpec),
                        faultSpec.getDelay(), faultSpec.getPercentage()))
                .expectedCommandOutputList(Arrays.asList("OK"))
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfRedisDbFaultInjectionRequest()).build();
        commandInfos.add(commandInfo);
        return commandInfos;
    }

    private List<CommandInfo> getReturnErrorInjectionCommandList(CommandExecutionFaultSpec commandExecutionFaultSpec) {
        RedisReturnErrorFaultSpec faultSpec = (RedisReturnErrorFaultSpec) commandExecutionFaultSpec;
        List<CommandInfo> commandInfos = new ArrayList<>();
        CommandInfo commandInfo = CommandInfo
                .builder(String.format(RedisProxyCommandConstants.RULE_ADD_FOR_RETURN_ERROR, getRuleName(faultSpec),
                        faultSpec.getErrorType(), faultSpec.getPercentage()))
                .expectedCommandOutputList(Arrays.asList("OK"))
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfRedisDbFaultInjectionRequest()).build();
        commandInfos.add(commandInfo);
        return commandInfos;
    }

    private List<CommandInfo> getRemediationCommandList(CommandExecutionFaultSpec faultSpec) {
        List<CommandInfo> commandInfos = new ArrayList<>();
        CommandInfo commandInfo = CommandInfo
                .builder(String.format(RedisProxyCommandConstants.REMEDIATION_COMMAND, getRuleName(faultSpec)))
                .expectedCommandOutputList(Arrays.asList("OK"))
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfRedisDbFaultRemediationRequest()).build();
        commandInfos.add(commandInfo);
        return commandInfos;
    }

    private String getRuleName(CommandExecutionFaultSpec faultSpec) {
        return (faultSpec.getArgs().get(OPERATION) + "_"
                + faultSpec.getEndpoint().getRedisProxyConnectionProperties().getHost());
    }

    private List<CommandInfo> getReturnEmptyInjectionCommandList(CommandExecutionFaultSpec commandExecutionFaultSpec) {
        RedisFaultSpec faultSpec = (RedisFaultSpec) commandExecutionFaultSpec;
        List<CommandInfo> commandInfos = new ArrayList<>();
        CommandInfo commandInfo = CommandInfo
                .builder(String.format(RedisProxyCommandConstants.RULE_ADD_FOR_RETURN_EMPTY, getRuleName(faultSpec),
                        faultSpec.getPercentage()))
                .expectedCommandOutputList(Arrays.asList("OK"))
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfRedisDbFaultInjectionRequest()).build();
        commandInfos.add(commandInfo);
        return commandInfos;
    }

    private List<CommandInfo> getDropConnectionInjectionCommandList(
            CommandExecutionFaultSpec commandExecutionFaultSpec) {
        RedisDropConnectionFaultSpec faultSpec = (RedisDropConnectionFaultSpec) commandExecutionFaultSpec;
        List<CommandInfo> commandInfos = new ArrayList<>();
        CommandInfo commandInfo = CommandInfo
                .builder(String.format(RedisProxyCommandConstants.RULE_ADD_FOR_DROP_CONNECTION, getRuleName(faultSpec),
                        faultSpec.getPercentage()))
                .expectedCommandOutputList(Arrays.asList("OK"))
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfRedisDbFaultInjectionRequest()).build();
        commandInfos.add(commandInfo);
        return commandInfos;
    }

    public void validateExistingRedisFault(ICommandExecutor executor) throws MangleException {
        String command = RedisProxyCommandConstants.RULE_LIST;
        CommandExecutionResult result = executor.executeCommand(command);
        if (result.getExitCode() == 0 && StringUtils.hasText(result.getCommandOutput())) {
            throw new MangleException(String.format(RedisProxyCommandConstants.REDIS_FAULT_PARALLEL_EXECUTION_ERROR,
                    result.getCommandOutput()), ErrorCode.REDIS_FAULT_PARALLEL_EXECUTION_ERROR);
        } else if (result.getExitCode() != 0) {
            throw new MangleException(ErrorCode.COMMAND_EXEC_EXIT_CODE_ERROR, command, result.getExitCode(),
                    result.getCommandOutput());
        }
    }
}
