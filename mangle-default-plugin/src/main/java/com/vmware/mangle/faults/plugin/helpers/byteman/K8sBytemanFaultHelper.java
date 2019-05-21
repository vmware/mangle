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

import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_JAR_EXTENSION;
import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_NAME;
import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_NOT_AVAILABLE_STRING;
import static com.vmware.mangle.utils.constants.FaultConstants.BYTEMAN_AGENT_INSTALLATION_RETRY_MESSAGE;
import static com.vmware.mangle.utils.constants.FaultConstants.ENABLE_TROUBLESHOOTING_RETRY_MESSAGE;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_COMPLETION_STRING;
import static com.vmware.mangle.utils.constants.FaultConstants.FORWARD_SLASH;
import static com.vmware.mangle.utils.constants.FaultConstants.KUBE_FAULT_EXEC_STRING;
import static com.vmware.mangle.utils.constants.FaultConstants.PID_K8S_ATTACH_MXBEANS_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.PROCESS;
import static com.vmware.mangle.utils.constants.FaultConstants.REMEDIATION_REQUEST_SUCCESSFUL_STRING;
import static com.vmware.mangle.utils.constants.FaultConstants.SUBMIT_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.SUCCESSFUL_BYTEMAN_AGENT_INSTALLATION_MESSAGE;
import static com.vmware.mangle.utils.constants.FaultConstants.SUCCESSFUL_TROUBLESHOOTING_ENABLED_MESSAGE;
import static com.vmware.mangle.utils.constants.FaultConstants.TASK_ID;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
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
import com.vmware.mangle.faults.plugin.utils.PluginUtils;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author hkilari
 *
 */
@Log4j2
public class K8sBytemanFaultHelper extends BytemanFaultHelper {
    private EndpointClientFactory endpointClientFactory;
    private JavaAgentFaultUtils javaAgentFaultUtils;

    private PluginUtils pluginUtils;


    @Autowired(required = true)
    public K8sBytemanFaultHelper(EndpointClientFactory endpointClientFactory, JavaAgentFaultUtils javaAgentFaultUtils,
            PluginUtils pluginUtils) {
        this.endpointClientFactory = endpointClientFactory;
        this.javaAgentFaultUtils = javaAgentFaultUtils;
        this.pluginUtils = pluginUtils;
    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec jvmAgentFaultSpec) throws MangleException {
        return (KubernetesCommandLineClient) endpointClientFactory.getEndPointClient(jvmAgentFaultSpec.getCredentials(),
                jvmAgentFaultSpec.getEndpoint());
    }


    @Override
    public List<CommandInfo> getInjectionCommandInfoList(CommandExecutionFaultSpec jvmAgentFaultSpec)
            throws MangleException {
        String scriptBasePath = jvmAgentFaultSpec.getInjectionHomeDir();
        scriptBasePath = scriptBasePath.endsWith(FORWARD_SLASH) ? scriptBasePath : scriptBasePath + FORWARD_SLASH;


        if (jvmAgentFaultSpec instanceof JVMCodeLevelFaultSpec) {

            List<CommandInfo> commandInfoList = new ArrayList<>();
            CommandInfo injectFaultCommandInfo = new CommandInfo();

            String ruleFileName = jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm";
            File ruleFile = new File(ConstantsUtils.getMangleSupportScriptDirectory() + ruleFileName);
            try {
                FileUtils.write(ruleFile, generateRule(jvmAgentFaultSpec), Charset.defaultCharset());
            } catch (IOException e) {
                log.error(e);
            }

            injectFaultCommandInfo.setCommand(
                    String.format(KUBE_FAULT_EXEC_STRING, jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                            jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                            String.format(SUBMIT_COMMAND_WITH_PORT, scriptBasePath,
                                    ((JVMCodeLevelFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())
                                    + new StringBuilder().append(scriptBasePath).append(AGENT_NAME)
                                            .append(FORWARD_SLASH).append(ruleFileName)));
            injectFaultCommandInfo.setIgnoreExitValueCheck(false);
            injectFaultCommandInfo.setExpectedCommandOutputList(
                    Arrays.asList("install rule " + jvmAgentFaultSpec.getArgs().get(TASK_ID)));
            injectFaultCommandInfo.setNoOfRetries(10);
            injectFaultCommandInfo.setRetryInterval(5);

            commandInfoList.add(getK8sCopyCommand(jvmAgentFaultSpec, scriptBasePath));
            commandInfoList.add(getInstallAgentCommandInfo(jvmAgentFaultSpec, scriptBasePath));
            commandInfoList.add(getEnableTrobleshootingCommandInfo(jvmAgentFaultSpec, scriptBasePath));
            commandInfoList.add(injectFaultCommandInfo);
            return commandInfoList;
        } else {
            List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
            List<CommandInfo> commandInfoList = new ArrayList<>();
            CommandInfo injectFaultCommandInfo = new CommandInfo();

            injectFaultCommandInfo.setCommand(String.format(KUBE_FAULT_EXEC_STRING,
                    jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                    jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                    javaAgentFaultUtils.buildInjectionCommand(jvmAgentFaultSpec.getArgs(), scriptBasePath,
                            String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()))));
            injectFaultCommandInfo
                    .setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentFaultInjectionRequest());

            CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
            commandOutputProcessingInfo.setExtractedPropertyName("faultId");
            commandOutputProcessingInfo.setRegExpression("[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
            commandOutputProcessingInfoList.add(commandOutputProcessingInfo);

            injectFaultCommandInfo.setCommandOutputProcessingInfoList(commandOutputProcessingInfoList);


            injectFaultCommandInfo.setIgnoreExitValueCheck(false);
            injectFaultCommandInfo
                    .setExpectedCommandOutputList(Arrays.asList(Constants.SUCESSFUL_FAULT_CREATION_MESSAGE));
            injectFaultCommandInfo.setNoOfRetries(10);
            injectFaultCommandInfo.setRetryInterval(5);
            commandInfoList.add(getK8sCopyCommand(jvmAgentFaultSpec, scriptBasePath));
            commandInfoList.add(getInstallAgentCommandInfo(jvmAgentFaultSpec, scriptBasePath));
            commandInfoList.add(getEnableTrobleshootingCommandInfo(jvmAgentFaultSpec, scriptBasePath));
            commandInfoList.add(injectFaultCommandInfo);

            return commandInfoList;
        }


    }

    private CommandInfo getInstallAgentCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec, String scriptBasePath) {
        CommandInfo installAgentCommandInfo = new CommandInfo();


        installAgentCommandInfo.setCommand(String.format(PID_K8S_ATTACH_MXBEANS_COMMAND_WITH_PORT,
                jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                jvmAgentFaultSpec.getK8sArguments().getContainerName(), scriptBasePath,
                ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort(),
                jvmAgentFaultSpec.getArgs().get(PROCESS)));
        installAgentCommandInfo.setIgnoreExitValueCheck(true);
        installAgentCommandInfo.setExpectedCommandOutputList(
                Arrays.asList(SUCCESSFUL_BYTEMAN_AGENT_INSTALLATION_MESSAGE, BYTEMAN_AGENT_INSTALLATION_RETRY_MESSAGE));
        installAgentCommandInfo.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentInstallationRequest());
        installAgentCommandInfo.setNoOfRetries(10);
        installAgentCommandInfo.setRetryInterval(5);
        return installAgentCommandInfo;
    }

    private CommandInfo getEnableTrobleshootingCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec,
            String scriptBasePath) {

        CommandInfo enableTrobleshootingCommandInfo = new CommandInfo();
        enableTrobleshootingCommandInfo
                .setCommand(String.format(KUBE_FAULT_EXEC_STRING, jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                        jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                        String.format(SUBMIT_COMMAND_WITH_PORT, scriptBasePath,
                                ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())
                                + "-enableTroubleshooting"));
        enableTrobleshootingCommandInfo.setIgnoreExitValueCheck(true);
        enableTrobleshootingCommandInfo.setExpectedCommandOutputList(
                Arrays.asList(SUCCESSFUL_TROUBLESHOOTING_ENABLED_MESSAGE, ENABLE_TROUBLESHOOTING_RETRY_MESSAGE));
        enableTrobleshootingCommandInfo.setNoOfRetries(10);
        enableTrobleshootingCommandInfo.setRetryInterval(5);
        return enableTrobleshootingCommandInfo;
    }

    private CommandInfo getK8sCopyCommand(CommandExecutionFaultSpec jvmAgentFaultSpec, String scriptBasePath) {
        CommandInfo k8sCopyCommandInfo = new CommandInfo();
        k8sCopyCommandInfo.setCommand(new StringBuilder().append("cp ")
                .append(ConstantsUtils.getMangleSupportScriptDirectory()).append(AGENT_NAME).append(" ")
                .append(jvmAgentFaultSpec.getK8sArguments().getPodInAction() + ":").append(scriptBasePath)
                .append(AGENT_NAME).append(Constants.K8S_CONTAINER_OPTION)
                .append(jvmAgentFaultSpec.getK8sArguments().getContainerName()).toString());
        k8sCopyCommandInfo.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentCopyOnK8sPod());
        return k8sCopyCommandInfo;
    }

    @Override
    public List<CommandInfo> getRemediationCommandInfoList(CommandExecutionFaultSpec jvmAgentFaultSpec)
            throws MangleException {
        String scriptBasePath = jvmAgentFaultSpec.getInjectionHomeDir();
        scriptBasePath = scriptBasePath.endsWith(FORWARD_SLASH) ? scriptBasePath : scriptBasePath + FORWARD_SLASH;

        if (jvmAgentFaultSpec instanceof JVMCodeLevelFaultSpec) {
            String ruleFileName = jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm";
            List<CommandInfo> commandInfoList = new ArrayList<>();
            CommandInfo commandInfo = new CommandInfo();
            commandInfo.setCommand(String
                    .format(String.format(KUBE_FAULT_EXEC_STRING, jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                            jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                            String.format(SUBMIT_COMMAND_WITH_PORT, scriptBasePath, new StringBuilder()
                                    .append(((JVMCodeLevelFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())
                                    .append(" -u ").append(new StringBuilder().append(scriptBasePath).append(AGENT_NAME)
                                            .append(FORWARD_SLASH).append(ruleFileName))
                                    .toString()))));
            commandInfo.setIgnoreExitValueCheck(true);
            commandInfo.setExpectedCommandOutputList(
                    Arrays.asList("uninstall RULE " + jvmAgentFaultSpec.getArgs().get(TASK_ID)));
            commandInfoList.add(commandInfo);
            return commandInfoList;
        } else {
            List<CommandInfo> commandInfoList = new ArrayList<>();
            CommandInfo remediationRequestCommandInfo = new CommandInfo();
            remediationRequestCommandInfo.setCommand(String.format(KUBE_FAULT_EXEC_STRING,
                    jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                    jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                    javaAgentFaultUtils.buildRemediationCommand(scriptBasePath,
                            String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()))));

            remediationRequestCommandInfo.setIgnoreExitValueCheck(false);
            remediationRequestCommandInfo
                    .setExpectedCommandOutputList(Arrays.asList(REMEDIATION_REQUEST_SUCCESSFUL_STRING));
            remediationRequestCommandInfo
                    .setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentFaultRemediationRequest());
            commandInfoList.add(remediationRequestCommandInfo);

            CommandInfo remediationVerificationCommnadInfo = new CommandInfo();
            remediationVerificationCommnadInfo.setCommand(String.format(KUBE_FAULT_EXEC_STRING,
                    jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                    jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                    javaAgentFaultUtils.buildGetFaultCommand(scriptBasePath,
                            String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()))));
            remediationVerificationCommnadInfo.setNoOfRetries(6);
            remediationVerificationCommnadInfo.setRetryInterval(10);
            remediationVerificationCommnadInfo.setIgnoreExitValueCheck(true);
            remediationVerificationCommnadInfo
                    .setExpectedCommandOutputList(Arrays.asList(FAULT_COMPLETION_STRING, AGENT_NOT_AVAILABLE_STRING));
            commandInfoList.add(remediationVerificationCommnadInfo);
            return commandInfoList;
        }
    }

    @Override
    public void checkTaskSpecificPrerequisites() throws MangleException {
        String filePath = ConstantsUtils.getMangleSupportScriptDirectory() + AGENT_NAME;
        File srcFile = new File(filePath);
        //Checking if the Script file is already available in Mangle Support Script Folder
        if (!srcFile.exists()) {
            pluginUtils.copyFileFromJarToDestination(FORWARD_SLASH + AGENT_NAME + AGENT_JAR_EXTENSION,
                    filePath + AGENT_JAR_EXTENSION);
            CommandExecutionResult result = CommandUtils.runCommand(new StringBuilder().append("tar -zxvf ")
                    .append(ConstantsUtils.getMangleSupportScriptDirectory()).append(AGENT_NAME + AGENT_JAR_EXTENSION)
                    .append(" --directory ").append(ConstantsUtils.getMangleSupportScriptDirectory()).toString(), 100);
            if (result.getExitCode() != 0) {
                throw new MangleException(ErrorCode.AGENT_EXTRACTION_FAILED, result.getCommandOutput());
            }
        }
    }

    @Override
    public List<SupportScriptInfo> getAgentFaultInjectionScripts(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        return Collections.emptyList();
    }
}
