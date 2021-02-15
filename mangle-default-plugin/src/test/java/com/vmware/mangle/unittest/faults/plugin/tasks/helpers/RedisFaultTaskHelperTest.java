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

package com.vmware.mangle.unittest.faults.plugin.tasks.helpers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import static com.vmware.mangle.utils.constants.FaultConstants.OPERATION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisDelayFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskTroubleShootingInfo;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.KnownFailuresHelper;
import com.vmware.mangle.faults.plugin.helpers.redis.RedisFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.RedisFaultTaskHelper;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.redis.RedisProxyCommandConstants;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Unit test cases for {@link RedisFaultTaskHelper}
 *
 * @author kumargautam
 */
@Log4j2
public class RedisFaultTaskHelperTest {

    @Mock
    private RedisFaultHelper redisFaultHelper;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private CommandInfoExecutionHelper commandInfoExecutionHelper;
    @InjectMocks
    private RedisFaultTaskHelper<RedisDelayFaultSpec> redisFaultTaskHelper;
    private FaultsMockData faultsMockData;

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
        redisFaultTaskHelper.setEventPublisher(publisher);
        redisFaultTaskHelper.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.tasks.helpers.RedisFaultTaskHelper#init(com.vmware.mangle.cassandra.model.redis.faults.specs.RedisFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testInit() throws MangleException {
        RedisDelayFaultSpec delayFaultSpec = faultsMockData.getRedisDelayFaultSpec();
        Task<RedisDelayFaultSpec> task = redisFaultTaskHelper.init(delayFaultSpec);
        assertTrue(task.isInitialized());
        assertEquals(task.getTaskType(), TaskType.INJECTION);
        assertEquals(task.getTaskDescription(),
                "Executing Fault: redisDbDelayFault on endpoint: redisProxyTest. More Details: [ RedisDelayFaultSpec(super=RedisFaultSpec(percentage="
                        + delayFaultSpec.getPercentage() + "), delay=" + delayFaultSpec.getDelay() + ") ]");

        task.getTriggers().add(new TaskTrigger());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.tasks.helpers.RedisFaultTaskHelper#executeTask(com.vmware.mangle.cassandra.model.tasks.Task)}.
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteTask() throws MangleException {
        RedisDelayFaultSpec delayFaultSpec = faultsMockData.getRedisDelayFaultSpec();
        ICommandExecutor commandExecutor = mock(ICommandExecutor.class);
        when(redisFaultHelper.getExecutor(any(CommandExecutionFaultSpec.class))).thenReturn(commandExecutor);
        when(redisFaultHelper.getInjectionCommandInfoList(any(ICommandExecutor.class),
                any(CommandExecutionFaultSpec.class))).thenReturn(getDelayInjectionCommandList(delayFaultSpec));
        when(redisFaultHelper.getRemediationCommandInfoList(any(ICommandExecutor.class),
                any(CommandExecutionFaultSpec.class))).thenReturn(getRemediationCommandList(delayFaultSpec));

        Task<RedisDelayFaultSpec> task = redisFaultTaskHelper.init(delayFaultSpec);
        assertTrue(task.isInitialized());
        assertEquals(task.getTaskType(), TaskType.INJECTION);
        task.getTriggers().add(new TaskTrigger());

        doNothing().when(publisher).publishEvent(Mockito.any());
        when(commandInfoExecutionHelper.runCommands(any(ICommandExecutor.class), anyList(),
                any(TaskTroubleShootingInfo.class), anyMap())).thenReturn("");
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        commandExecutionResult.setCommandOutput("OK");
        commandExecutionResult.setExitCode(0);
        when(commandExecutor.executeCommand(anyString())).thenReturn(commandExecutionResult);

        redisFaultTaskHelper.executeTask(task);
        log.info(RestTemplateWrapper.objectToJson(task));
        verify(redisFaultHelper, times(4)).getExecutor(any(CommandExecutionFaultSpec.class));
        verify(redisFaultHelper, times(1)).getInjectionCommandInfoList(any(ICommandExecutor.class),
                any(CommandExecutionFaultSpec.class));
        verify(redisFaultHelper, times(1)).getRemediationCommandInfoList(any(ICommandExecutor.class),
                any(CommandExecutionFaultSpec.class));
        assertEquals(task.getTaskSubstage(), TaskStatus.COMPLETED.toString());
        assertEquals(task.getTaskData().getInjectionCommandInfoList(), getDelayInjectionCommandList(delayFaultSpec));
        assertEquals(task.getTaskData().getRemediationCommandInfoList(), getRemediationCommandList(delayFaultSpec));
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
        return (faultSpec.getArgs().get(OPERATION).toLowerCase() + "_" + faultSpec.getId());
    }
}
