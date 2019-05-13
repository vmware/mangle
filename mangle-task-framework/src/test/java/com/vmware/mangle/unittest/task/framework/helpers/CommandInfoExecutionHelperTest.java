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

package com.vmware.mangle.unittest.task.framework.helpers;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.mockdata.CommandResultUtils;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author hkilari
 */
@Log4j2
public class CommandInfoExecutionHelperTest {
    CommandInfoExecutionHelper commandInfoExecutionHelper;

    @Mock
    CommandUtils commandUtils;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        commandInfoExecutionHelper = new CommandInfoExecutionHelper();
        Mockito.reset(commandUtils);
    }

    @Test(enabled = false, priority = 0)
    public void testRunCommands() {
        MockCommandExecutionTask<CommandExecutionFaultSpec> taskHelper = new MockCommandExecutionTask<>();
        FaultTask<CommandExecutionFaultSpec> task = taskHelper.init(new CommandExecutionFaultSpec(), null);
        Map<String, String> args = task.getTaskData().getArgs();
        args.put("id", "12345");
        try {
            commandInfoExecutionHelper.runCommands(new CommandUtils(), taskHelper.getInjectionExecutionInfo(task),
                    task.getTaskTroubleShootingInfo(), args);
        } catch (MangleException e) {
            log.info(e);
            Assert.assertTrue(false);
        }
    }

    @Test(priority = 1, expectedExceptions = { MangleException.class })
    public void testRunCommandsWithCommandExecutionFailureOnOutput() throws MangleException {
        MockCommandExecutionTask<CommandExecutionFaultSpec> taskHelper = new MockCommandExecutionTask<>();
        FaultTask<CommandExecutionFaultSpec> task = taskHelper.init(new CommandExecutionFaultSpec(), null);
        Map<String, String> args = task.getTaskData().getArgs();
        args.put("id", "12345");
        Mockito.when(commandUtils.executeCommand("echo Injecting Fault"))
                .thenReturn(CommandResultUtils.getCommandResult(""));
        try {
            commandInfoExecutionHelper.runCommands(commandUtils, taskHelper.getInjectionExecutionInfo(task),
                    task.getTaskTroubleShootingInfo(), args);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.COMMAND_EXEC_EXIT_CODE_ERROR);
            Mockito.verify(commandUtils, Mockito.times(3)).executeCommand("echo Injecting Fault");
            throw e;
        }
    }

    @Test(priority = 2, expectedExceptions = { MangleException.class })
    public void testRunCommandsWithCommandExecutionFailureOnExitCode() throws MangleException {
        MockCommandExecutionTask<CommandExecutionFaultSpec> taskHelper = new MockCommandExecutionTask<>();
        FaultTask<CommandExecutionFaultSpec> task = taskHelper.init(new CommandExecutionFaultSpec(), null);
        Map<String, String> args = task.getTaskData().getArgs();
        args.put("id", "12345");
        CommandExecutionResult result = CommandResultUtils.getCommandResult("");
        result.setExitCode(-1);
        Mockito.when(commandUtils.executeCommand("echo Injecting Fault")).thenReturn(result);
        try {
            commandInfoExecutionHelper.runCommands(commandUtils, taskHelper.getInjectionExecutionInfo(task),
                    task.getTaskTroubleShootingInfo(), args);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.COMMAND_EXEC_EXIT_CODE_ERROR);
            Mockito.verify(commandUtils, Mockito.times(3)).executeCommand("echo Injecting Fault");
            throw e;
        }
    }

    @Test(priority = 3, expectedExceptions = { MangleException.class })
    public void testRunCommandsWithCommandExecutionFailureWithMissingArgReferences() throws MangleException {
        MockCommandExecutionTask<CommandExecutionFaultSpec> taskHelper = new MockCommandExecutionTask<>();
        FaultTask<CommandExecutionFaultSpec> task = taskHelper.init(new CommandExecutionFaultSpec(), null);
        Map<String, String> args = task.getTaskData().getArgs();
        Mockito.when(commandUtils.executeCommand("echo Injecting Fault"))
                .thenReturn(CommandResultUtils.getCommandResult("Injecting Fault"));
        try {
            commandInfoExecutionHelper.runCommands(commandUtils, taskHelper.getInjectionExecutionInfo(task),
                    task.getTaskTroubleShootingInfo(), args);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.MISSING_REFERENCE_VALUES);
            Mockito.verify(commandUtils, Mockito.times(1)).executeCommand("echo Injecting Fault");
            throw e;
        }
    }

    @Test(priority = 4)
    public void testMakeExecutable() {
        MockCommandExecutionTask<CommandExecutionFaultSpec> taskHelper = new MockCommandExecutionTask<>();
        FaultTask<CommandExecutionFaultSpec> task = taskHelper.init(new CommandExecutionFaultSpec(), null);
        Map<String, String> args = task.getTaskData().getArgs();
        args.put("id", "12345");
        Mockito.when(commandUtils.executeCommand("chmod u+x /mangle.config"))
                .thenReturn(CommandResultUtils.getCommandResult(""));
        try {
            commandInfoExecutionHelper.makeExecutable(commandUtils, getFaultInjectionScriptsList().get(0));
        } catch (MangleException e) {
            Assert.assertTrue(false, "Filed due to unexpected Exception");
            log.error(e);
        }
        Mockito.verify(commandUtils, Mockito.times(1)).executeCommand("chmod u+x /mangle.config");
    }

    public List<SupportScriptInfo> getFaultInjectionScriptsList() {
        List<SupportScriptInfo> supportScriptInfos = new ArrayList<>();
        SupportScriptInfo supportScriptInfo = new SupportScriptInfo();
        supportScriptInfo.setClassPathResource(true);
        supportScriptInfo.setExecutable(true);
        supportScriptInfo.setScriptFileName("mangle.config");
        supportScriptInfo.setTargetDirectoryPath("");
        supportScriptInfos.add(supportScriptInfo);

        return supportScriptInfos;
    }
}
