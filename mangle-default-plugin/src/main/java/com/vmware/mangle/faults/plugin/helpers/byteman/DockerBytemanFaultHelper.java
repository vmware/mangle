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

package com.vmware.mangle.faults.plugin.helpers.byteman;

import static com.vmware.mangle.utils.constants.Constants.NO_OF_RETRIES;
import static com.vmware.mangle.utils.constants.Constants.RETRY_COUNT;
import static com.vmware.mangle.utils.constants.Constants.SET_JAVA_HOME_CMD;
import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_JAR_EXTENSION;
import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_NAME;
import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_NOT_AVAILABLE_STRING;
import static com.vmware.mangle.utils.constants.FaultConstants.BYTEMAN_AGENT_INSTALLATION_RETRY_MESSAGE;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_COMPLETION_STRING;
import static com.vmware.mangle.utils.constants.FaultConstants.JAVA_HOME_PATH;
import static com.vmware.mangle.utils.constants.FaultConstants.PID_ATTACH_MXBEANS_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.PROCESS;
import static com.vmware.mangle.utils.constants.FaultConstants.REMEDIATION_REQUEST_SUCCESSFUL_STRING;
import static com.vmware.mangle.utils.constants.FaultConstants.SUBMIT_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.SUCCESSFUL_BYTEMAN_AGENT_INSTALLATION_MESSAGE;
import static com.vmware.mangle.utils.constants.FaultConstants.TASK_ID;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.faults.plugin.helpers.JavaAgentFaultUtils;
import com.vmware.mangle.faults.plugin.helpers.KnownFailuresHelper;
import com.vmware.mangle.faults.plugin.helpers.docker.DockerCommandUtils;
import com.vmware.mangle.faults.plugin.utils.PluginUtils;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam
 *
 */

@Log4j2
public class DockerBytemanFaultHelper extends BytemanFaultHelper {

    private EndpointClientFactory endpointClientFactory;
    private JavaAgentFaultUtils javaAgentFaultUtils;
    private PluginUtils pluginUtils;

    @Autowired
    public void setEndpointClientFactory(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;
    }

    @Autowired
    public void setJavaAgentFaultUtils(JavaAgentFaultUtils javaAgentFaultUtils) {
        this.javaAgentFaultUtils = javaAgentFaultUtils;
    }

    @Autowired
    public void setPluginUtils(PluginUtils pluginUtils) {
        this.pluginUtils = pluginUtils;
    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec jvmAgentFaultSpec) throws MangleException {
        return new DockerCommandUtils(jvmAgentFaultSpec, endpointClientFactory);
    }

    @Override
    public List<CommandInfo> getInjectionCommandInfoList(CommandExecutionFaultSpec jvmAgentFaultSpec)
            throws MangleException {
        if (jvmAgentFaultSpec instanceof JVMCodeLevelFaultSpec) {
            List<CommandInfo> commandInfoList = new ArrayList<>();
            CommandInfo attachBeansCommandInfo = new CommandInfo();
            CommandInfo injectFaultCommandInfo = new CommandInfo();
            CommandInfo createBytemanRuleCommandInfo = new CommandInfo();

            //Recheck Creating the BytemanRule Code
            createBytemanRuleCommandInfo.setExpectedCommandOutputList(Collections.emptyList());
            createBytemanRuleCommandInfo.setCommand("echo \"" + generateRule(jvmAgentFaultSpec) + "\" > "
                    + jvmAgentFaultSpec.getInjectionHomeDir() + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm");

            attachBeansCommandInfo.setCommand(
                    String.format(PID_ATTACH_MXBEANS_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(),
                            ((JVMCodeLevelFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort(),
                            jvmAgentFaultSpec.getArgs().get(PROCESS)));
            attachBeansCommandInfo.setIgnoreExitValueCheck(true);
            attachBeansCommandInfo.setExpectedCommandOutputList(Arrays
                    .asList(SUCCESSFUL_BYTEMAN_AGENT_INSTALLATION_MESSAGE, BYTEMAN_AGENT_INSTALLATION_RETRY_MESSAGE));

            injectFaultCommandInfo
                    .setCommand(String.format(SUBMIT_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(),
                            ((JVMCodeLevelFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())
                            + jvmAgentFaultSpec.getInjectionHomeDir() + jvmAgentFaultSpec.getArgs().get(TASK_ID)
                            + ".btm");
            injectFaultCommandInfo.setIgnoreExitValueCheck(false);
            injectFaultCommandInfo.setExpectedCommandOutputList(
                    Arrays.asList("install rule " + jvmAgentFaultSpec.getArgs().get(TASK_ID)));

            commandInfoList.add(createBytemanRuleCommandInfo);
            commandInfoList.add(attachBeansCommandInfo);
            commandInfoList.add(injectFaultCommandInfo);
            return commandInfoList;
        } else {
            List<CommandInfo> commandInfoList = new ArrayList<>();
            CommandInfo installAgentCommandInfo = new CommandInfo();
            CommandInfo injectFaultCommandInfo = new CommandInfo();
            List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
            if (!StringUtils.isEmpty(jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH))) {
                installAgentCommandInfo.setCommand(String.format(
                        SET_JAVA_HOME_CMD + jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH) + "&&"
                                + PID_ATTACH_MXBEANS_COMMAND_WITH_PORT,
                        jvmAgentFaultSpec.getInjectionHomeDir(),
                        ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort(),
                        jvmAgentFaultSpec.getArgs().get(PROCESS)));
            } else {
                installAgentCommandInfo.setCommand(
                        String.format(PID_ATTACH_MXBEANS_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(),
                                ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort(),
                                jvmAgentFaultSpec.getArgs().get(PROCESS)));
            }
            installAgentCommandInfo.setIgnoreExitValueCheck(true);
            installAgentCommandInfo.setExpectedCommandOutputList(Arrays
                    .asList(SUCCESSFUL_BYTEMAN_AGENT_INSTALLATION_MESSAGE, BYTEMAN_AGENT_INSTALLATION_RETRY_MESSAGE));
            installAgentCommandInfo
                    .setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentInstallationRequest());
            log.debug("CommandInfo object of attachBeansCommandInfo :" + installAgentCommandInfo);

            if (!StringUtils.isEmpty(jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH))) {
                injectFaultCommandInfo.setCommand(String.format(SET_JAVA_HOME_CMD
                        + jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH) + "&&"
                        + javaAgentFaultUtils.buildInjectionCommand(jvmAgentFaultSpec.getArgs(),
                                jvmAgentFaultSpec.getInjectionHomeDir(),
                                String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()))));
            } else {
                injectFaultCommandInfo.setCommand(javaAgentFaultUtils.buildInjectionCommand(jvmAgentFaultSpec.getArgs(),
                        jvmAgentFaultSpec.getInjectionHomeDir(),
                        String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())));
            }
            injectFaultCommandInfo.setIgnoreExitValueCheck(false);
            injectFaultCommandInfo
                    .setExpectedCommandOutputList(Arrays.asList(Constants.SUCESSFUL_FAULT_CREATION_MESSAGE));
            injectFaultCommandInfo
                    .setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentFaultInjectionRequest());
            log.debug("CommandInfo object of InjectFaultCommandInfo :" + injectFaultCommandInfo);

            CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
            commandOutputProcessingInfo.setExtractedPropertyName("faultId");
            commandOutputProcessingInfo.setRegExpression("[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
            commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
            injectFaultCommandInfo.setCommandOutputProcessingInfoList(commandOutputProcessingInfoList);

            commandInfoList.add(installAgentCommandInfo);
            commandInfoList.add(injectFaultCommandInfo);
            return commandInfoList;
        }
    }

    @Override
    public List<CommandInfo> getRemediationCommandInfoList(CommandExecutionFaultSpec jvmAgentFaultSpec)
            throws MangleException {
        if (jvmAgentFaultSpec instanceof JVMCodeLevelFaultSpec) {
            List<CommandInfo> commandInfoList = new ArrayList<>();
            CommandInfo commandInfo = new CommandInfo();
            commandInfo.setCommand(String.format(SUBMIT_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(),
                    ((JVMCodeLevelFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()) + "-u "
                    + jvmAgentFaultSpec.getInjectionHomeDir() + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm");
            commandInfo.setIgnoreExitValueCheck(true);
            commandInfo.setExpectedCommandOutputList(
                    Arrays.asList("uninstall RULE " + jvmAgentFaultSpec.getArgs().get(TASK_ID)));
            commandInfoList.add(commandInfo);
            log.debug("CommandInfo object for RemediationCommandInfo :" + commandInfo);
            return commandInfoList;
        } else {
            List<CommandInfo> commandInfoList = new ArrayList<>();
            CommandInfo remediationRequestCommandInfo = new CommandInfo();
            remediationRequestCommandInfo
                    .setCommand(javaAgentFaultUtils.buildRemediationCommand(jvmAgentFaultSpec.getInjectionHomeDir(),
                            String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())));
            remediationRequestCommandInfo.setIgnoreExitValueCheck(false);
            remediationRequestCommandInfo
                    .setExpectedCommandOutputList(Arrays.asList(REMEDIATION_REQUEST_SUCCESSFUL_STRING));
            remediationRequestCommandInfo
                    .setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentFaultRemediationRequest());
            commandInfoList.add(remediationRequestCommandInfo);
            log.debug("CommandInfo object RemediationCommandInfo:" + remediationRequestCommandInfo);

            CommandInfo remediationVerificationCommnadInfo = new CommandInfo();
            remediationVerificationCommnadInfo
                    .setCommand(javaAgentFaultUtils.buildGetFaultCommand(jvmAgentFaultSpec.getInjectionHomeDir(),
                            String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())));
            remediationVerificationCommnadInfo.setNoOfRetries(NO_OF_RETRIES);
            remediationVerificationCommnadInfo.setRetryInterval(RETRY_COUNT);
            remediationVerificationCommnadInfo.setIgnoreExitValueCheck(true);
            remediationVerificationCommnadInfo
                    .setExpectedCommandOutputList(Arrays.asList(FAULT_COMPLETION_STRING, AGENT_NOT_AVAILABLE_STRING));
            commandInfoList.add(remediationVerificationCommnadInfo);
            log.debug("CommandInfo object FaultCommandInfo :" + remediationVerificationCommnadInfo);
            return commandInfoList;
        }
    }

    @Override
    public List<SupportScriptInfo> getAgentFaultInjectionScripts(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        return getAgentFaultScripts(jvmAgentFaultSpec.getInjectionHomeDir(), AGENT_NAME);
    }

    @Override
    public void checkTaskSpecificPrerequisites() throws MangleException {
        String filePath = ConstantsUtils.getMangleSupportScriptDirectory() + AGENT_NAME;
        File srcFile = new File(filePath);
        log.debug("Checking if the Agent Jar is already available in Mangle Support Script Folder");
        if (!srcFile.exists()) {
            pluginUtils.copyFileFromJarToDestination("/" + AGENT_NAME + AGENT_JAR_EXTENSION,
                    filePath + AGENT_JAR_EXTENSION);
            CommandExecutionResult result =
                    CommandUtils.runCommand("cd " + ConstantsUtils.getMangleSupportScriptDirectory() + " && tar -zxvf "
                            + AGENT_NAME + AGENT_JAR_EXTENSION + " ", 100);
            if (result.getExitCode() != 0) {
                throw new MangleException(ErrorCode.AGENT_EXTRACTION_FAILED, result.getCommandOutput());
            }
        }

    }

}

