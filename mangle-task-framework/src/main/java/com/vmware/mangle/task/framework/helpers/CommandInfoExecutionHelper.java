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

package com.vmware.mangle.task.framework.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.TaskTroubleShootingInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author hkilari
 *
 */
@Log4j2
@Component
public class CommandInfoExecutionHelper {


    /**
     * Utility Class to execute list of the commands Defined in Fault for Injection, Remediation and
     * Test Machine Preperation
     *
     * @param commandInfos
     */
    public void runCommands(ICommandExecutor executor, List<CommandInfo> commandInfos,
            TaskTroubleShootingInfo taskTroubleShootingInfo, Map<String, String> args) throws MangleException {
        String latestCommandOutput = null;
        if (!CollectionUtils.isEmpty(commandInfos)) {
            for (CommandInfo commandInfo : commandInfos) {
                latestCommandOutput = executeRetriableCommand(executor, commandInfo, taskTroubleShootingInfo, args,
                        latestCommandOutput).getCommandOutput();

                if (commandInfo.getCommandOutputProcessingInfoList() != null) {
                    extractFieldsFromCommandResult(commandInfo.getCommandOutputProcessingInfoList(),
                            taskTroubleShootingInfo, latestCommandOutput);
                }
            }
        }
    }

    private CommandExecutionResult executeRetriableCommand(ICommandExecutor executor, CommandInfo commandInfo,
            TaskTroubleShootingInfo taskTroubleShootingInfo, Map<String, String> args, String latestCommandOutput)
            throws MangleException {
        CommandExecutionResult commandExecutionResult = null;
        if (commandInfo.getNoOfRetries() > 0) {
            for (int i = 0; i <= commandInfo.getNoOfRetries(); i++) {
                try {
                    commandExecutionResult =
                            executeCommand(executor, commandInfo, taskTroubleShootingInfo, args, latestCommandOutput);
                    return commandExecutionResult;
                } catch (MangleException e) {
                    log.error("Command Execution Attempt: " + i + " Failed. Reason:", e.getMessage());
                    CommonUtils.delayInSeconds(Constants.COMMAND_EXECUTION_RETRY_INTERVAL);
                }
            }
            throw new MangleException(ErrorCode.COMMAND_EXEC_ERROR, commandInfo.getCommand());

        } else {
            commandExecutionResult =
                    executeCommand(executor, commandInfo, taskTroubleShootingInfo, args, latestCommandOutput);
        }
        return commandExecutionResult;

    }

    private CommandExecutionResult executeCommand(ICommandExecutor executor, CommandInfo commandInfo,
            TaskTroubleShootingInfo taskTroubleShootingInfo, Map<String, String> args, String latestCommandOutput)
            throws MangleException {
        CommandExecutionResult commandExecutionResult = executor
                .executeCommand(getAbsoluteCommand(commandInfo, taskTroubleShootingInfo, args, latestCommandOutput));
        // Condition to validate if the Command Execution completed with
        // valid exit code
        if (!commandInfo.isIgnoreExitValueCheck() && commandExecutionResult.getExitCode() != 0) {
            verifyExpectedFailures(commandInfo, commandExecutionResult);
            throw new MangleException(commandExecutionResult.getCommandOutput(), ErrorCode.COMMAND_EXEC_EXIT_CODE_ERROR,
                    commandInfo.getCommand(), commandExecutionResult.getExitCode(),
                    commandExecutionResult.getCommandOutput());
        }
        // Condition to validate the command execution by verification
        // of command execution result
        boolean isOutputMatched = false;
        if (!CollectionUtils.isEmpty(commandInfo.getExpectedCommandOutputList())) {
            for (String expectedoutput : commandInfo.getExpectedCommandOutputList()) {
                if (commandExecutionResult.getCommandOutput().contains(expectedoutput)) {
                    isOutputMatched = true;
                    break;
                }
            }
            if (!isOutputMatched) {
                throw new MangleException(ErrorCode.COMMAND_EXEC_OUTPUT_ERROR,
                        commandInfo.getExpectedCommandOutputList(), commandExecutionResult.getCommandOutput());
            }
        }
        return commandExecutionResult;
    }

    private void verifyExpectedFailures(CommandInfo commandInfo, CommandExecutionResult commandExecutionResult)
            throws MangleException {
        if (!CollectionUtils.isEmpty(commandInfo.getKnownFailureMap())) {
            for (Map.Entry<String, String> entry : commandInfo.getKnownFailureMap().entrySet()) {
                if (!StringUtils.isEmpty(entry.getValue())
                        && commandExecutionResult.getCommandOutput().contains(entry.getKey())) {
                    throw new MangleException(commandExecutionResult.getCommandOutput(),
                            ErrorCode.COMMAND_EXEC_EXIT_CODE_ERROR, commandInfo.getCommand(),
                            commandExecutionResult.getExitCode(), entry.getValue());
                }
            }

        }
    }


    private void extractFieldsFromCommandResult(List<CommandOutputProcessingInfo> commandOutputProcessingInfoList,
            TaskTroubleShootingInfo taskTroubleShootingInfo, String latestCommandOutput) throws MangleException {
        for (CommandOutputProcessingInfo commandOutputProcessingInfo : commandOutputProcessingInfoList) {
            String fieldValue = commandOutputProcessingInfo.getRegExpression() == null ? latestCommandOutput
                    : CommonUtils.extractField(latestCommandOutput, commandOutputProcessingInfo.getRegExpression());

            if (StringUtils.isNotEmpty(commandOutputProcessingInfo.getExtractedPropertyName())
                    && StringUtils.isNotEmpty(fieldValue)) {
                taskTroubleShootingInfo.getAdditionalInfo().put(commandOutputProcessingInfo.getExtractedPropertyName(),
                        fieldValue);
            } else {
                throw new MangleException(ErrorCode.COMMAND_OUTPUT_PROCESSING_ERROR, commandOutputProcessingInfo);
            }
        }
    }

    /**
     * Method to Replace Arguments Specified in Command with absolute values provided as Task Inputs
     *
     * @param commandInfo
     * @param latestCommandOutput
     * @return
     * @throws MangleException
     */
    private String getAbsoluteCommand(CommandInfo commandInfo, TaskTroubleShootingInfo taskTroubleShootingInfo,
            Map<String, String> args, String latestCommandOutput) throws MangleException {
        // Replace all the arg references
        if (!CollectionUtils.isEmpty(args)) {
            processReferences(commandInfo, args, Constants.FIAACO_CMD_ARG_EXPRESSION);
        }

        // Replace all the additional Information references
        if (taskTroubleShootingInfo != null) {
            processReferences(commandInfo, taskTroubleShootingInfo.getAdditionalInfo(),
                    Constants.FIAACO_CMD_ADD_INFO_EXPRESSION);
        }
        // Condition to replace Reference to Previous Command result in the present Command
        if (commandInfo.getCommand().contains(Constants.FIAACO_CMD_STACK_EXPRESSION) && latestCommandOutput != null) {
            commandInfo.setCommand(commandInfo.getCommand().replaceAll("\\" + Constants.FIAACO_CMD_STACK_EXPRESSION,
                    latestCommandOutput.trim()));
        }
        log.info(commandInfo.getCommand());
        return commandInfo.getCommand();
    }

    /**
     * @param commandInfo
     * @param referedValues
     * @throws MangleException
     */
    private void processReferences(CommandInfo commandInfo, Map<String, String> referedValues, String expression)
            throws MangleException {
        if (commandInfo.getCommand().contains(expression) && referedValues != null) {
            for (Entry<String, String> entry : referedValues.entrySet()) {
                if (commandInfo.getCommand().contains(expression + entry.getKey()) && entry.getValue() != null) {
                    commandInfo.setCommand(
                            commandInfo.getCommand().replaceAll("\\" + expression + entry.getKey(), entry.getValue()));
                }
            }
        }
        if (commandInfo.getCommand().contains(expression)) {
            throw new MangleException(ErrorCode.MISSING_REFERENCE_VALUES, commandInfo.getCommand());
        }
    }


    /**
     * Utility method to make a Script File executable
     *
     * @param faultInjectionScriptInfo
     * @throws MangleException
     */
    public void makeExecutable(ICommandExecutor executor, SupportScriptInfo faultInjectionScriptInfo)
            throws MangleException {
        // Checking if the file permissions need to be changed to support
        // invocation of the script
        if (faultInjectionScriptInfo.isExecutable()) {
            List<CommandInfo> commandInfos = new ArrayList<>();
            CommandInfo commandInfo = new CommandInfo();
            commandInfo.setCommand("chmod u+x " + faultInjectionScriptInfo.getTargetDirectoryPath() + "/"
                    + faultInjectionScriptInfo.getScriptFileName());
            commandInfo.setIgnoreExitValueCheck(false);
            commandInfo.setExpectedCommandOutputList(Arrays.asList(""));
            commandInfos.add(commandInfo);
            runCommands(executor, commandInfos, null, null);
        }
    }
}
