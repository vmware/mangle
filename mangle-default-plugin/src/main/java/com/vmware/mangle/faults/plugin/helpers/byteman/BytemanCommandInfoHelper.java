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
import static com.vmware.mangle.utils.constants.Constants.RETRY_WAIT_INTERVAL;
import static com.vmware.mangle.utils.constants.Constants.SET_JAVA_HOME_CMD;
import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_JAR_EXTENSION;
import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_NAME;
import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_NOT_AVAILABLE_STRING;
import static com.vmware.mangle.utils.constants.FaultConstants.BYTEMAN_AGENT_INSTALLATION_RETRY_MESSAGE;
import static com.vmware.mangle.utils.constants.FaultConstants.DELETE_COMMAND;
import static com.vmware.mangle.utils.constants.FaultConstants.ENABLE_TROUBLESHOOTING_RETRY_MESSAGE;
import static com.vmware.mangle.utils.constants.FaultConstants.EXTRACT_AGENT_COMMAND;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_COMPLETION_STRING;
import static com.vmware.mangle.utils.constants.FaultConstants.FI_ADD_INFO_FAULTID;
import static com.vmware.mangle.utils.constants.FaultConstants.FORWARD_SLASH;
import static com.vmware.mangle.utils.constants.FaultConstants.JAVA_HOME_PATH;
import static com.vmware.mangle.utils.constants.FaultConstants.KUBE_FAULT_EXEC_STRING;
import static com.vmware.mangle.utils.constants.FaultConstants.PID_ATTACH_MXBEANS_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.PID_K8S_ATTACH_MXBEANS_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.PID_K8S_ATTACH_MXBEANS_COMMAND_WITH_PORT_JAVA_HOME;
import static com.vmware.mangle.utils.constants.FaultConstants.PROCESS;
import static com.vmware.mangle.utils.constants.FaultConstants.REMEDIATION_REQUEST_SUCCESSFUL_STRING;
import static com.vmware.mangle.utils.constants.FaultConstants.SUBMIT_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.SUCCESSFUL_BYTEMAN_AGENT_INSTALLATION_MESSAGE;
import static com.vmware.mangle.utils.constants.FaultConstants.SUCCESSFUL_TROUBLESHOOTING_ENABLED_MESSAGE;
import static com.vmware.mangle.utils.constants.FaultConstants.TASK_ID;
import static com.vmware.mangle.utils.constants.FaultConstants.USER;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.faults.plugin.helpers.FaultConstants;
import com.vmware.mangle.faults.plugin.helpers.JavaAgentFaultUtils;
import com.vmware.mangle.faults.plugin.helpers.KnownFailuresHelper;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.enums.AgentFaultName;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.constants.Constants;

/**
 * @author hkilari, dbhat
 *
 */
@UtilityClass
@Log4j2
public class BytemanCommandInfoHelper {

    static CommandInfo getK8sJavaAgentInstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo.CommandInfoBuilder builder;
        if (!StringUtils.isEmpty(jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH))) {
            builder = CommandInfo.builder(String.format(PID_K8S_ATTACH_MXBEANS_COMMAND_WITH_PORT_JAVA_HOME,
                    jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                    jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                    SET_JAVA_HOME_CMD + jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH) + " && "
                            + jvmAgentFaultSpec.getInjectionHomeDir(),
                    ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort(),
                    jvmAgentFaultSpec.getArgs().get(PROCESS)));
        } else {
            builder = CommandInfo.builder(String.format(PID_K8S_ATTACH_MXBEANS_COMMAND_WITH_PORT,
                    jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                    jvmAgentFaultSpec.getK8sArguments().getContainerName(), jvmAgentFaultSpec.getInjectionHomeDir(),
                    ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort(),
                    jvmAgentFaultSpec.getArgs().get(PROCESS)));
        }
        getBaseJavaAgentInstallationCommandInfo(builder);
        builder.noOfRetries(10).retryInterval(5);
        return builder.build();
    }

    static CommandInfo getDockerJavaAgentInstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo.CommandInfoBuilder builder;
        if (!StringUtils.isEmpty(jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH))) {
            builder = CommandInfo.builder(String.format(
                    SET_JAVA_HOME_CMD + jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH) + "&&"
                            + PID_ATTACH_MXBEANS_COMMAND_WITH_PORT,
                    jvmAgentFaultSpec.getInjectionHomeDir(),
                    ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort(),
                    jvmAgentFaultSpec.getArgs().get(PROCESS)));
        } else {
            builder = CommandInfo.builder(
                    String.format(PID_ATTACH_MXBEANS_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(),
                            ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort(),
                            jvmAgentFaultSpec.getArgs().get(PROCESS)));
        }
        getBaseJavaAgentInstallationCommandInfo(builder);
        CommandInfo baseCommandInfo = builder.build();
        log.debug("CommandInfo object of attachBeansCommandInfo :" + baseCommandInfo);
        return baseCommandInfo;
    }

    static CommandInfo getLinuxJavaAgentInstallCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        // Byteman install command
        CommandInfo.CommandInfoBuilder builder =
                CommandInfo.builder(getLinuxJavaAgentInstallationCommand(jvmAgentFaultSpec));
        getBaseJavaAgentInstallationCommandInfo(builder);
        return builder.build();
    }

    private static CommandInfo.CommandInfoBuilder getBaseJavaAgentInstallationCommandInfo(
            CommandInfo.CommandInfoBuilder builder) {
        return builder.ignoreExitValueCheck(true)
                .expectedCommandOutputList(Arrays.asList(SUCCESSFUL_BYTEMAN_AGENT_INSTALLATION_MESSAGE,
                        BYTEMAN_AGENT_INSTALLATION_RETRY_MESSAGE))
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentInstallationRequest());
    }

    static CommandInfo getK8sJavaAgentFaultInjectionCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec,
            JavaAgentFaultUtils javaAgentFaultUtils) {
        CommandInfo.CommandInfoBuilder builder = CommandInfo
                .builder(String.format(KUBE_FAULT_EXEC_STRING, jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                        jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                        javaAgentFaultUtils.buildInjectionCommand(jvmAgentFaultSpec.getArgs(),
                                jvmAgentFaultSpec.getInjectionHomeDir(),
                                String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()))));
        getBaseJavaAgentFaultInjectionCommandInfo(builder);
        builder.noOfRetries(10).retryInterval(5);
        return builder.build();
    }

    static CommandInfo getDockerJavaAgentFaultInjectionCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec,
            JavaAgentFaultUtils javaAgentFaultUtils) {
        CommandInfo.CommandInfoBuilder builder;
        if (!StringUtils.isEmpty(jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH))) {
            builder = CommandInfo.builder(String.format(SET_JAVA_HOME_CMD
                    + jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH) + "&&"
                    + javaAgentFaultUtils.buildInjectionCommand(jvmAgentFaultSpec.getArgs(),
                            jvmAgentFaultSpec.getInjectionHomeDir(),
                            String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()))));
        } else {
            builder = CommandInfo.builder(javaAgentFaultUtils.buildInjectionCommand(jvmAgentFaultSpec.getArgs(),
                    jvmAgentFaultSpec.getInjectionHomeDir(),
                    String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())));
        }
        getBaseJavaAgentFaultInjectionCommandInfo(builder);
        CommandInfo baseCommandInfo = builder.build();
        log.debug("CommandInfo object of InjectFaultCommandInfo :" + baseCommandInfo);
        return baseCommandInfo;
    }

    static CommandInfo getLinuxJavaAgentFaultInjectionCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo.CommandInfoBuilder builder = CommandInfo.builder(getBytemanSubmitCommand(jvmAgentFaultSpec)
                .replace("%s", "-if " + CommonUtils.convertMaptoDelimitedString(jvmAgentFaultSpec.getArgs(), " ")));
        getBaseJavaAgentFaultInjectionCommandInfo(builder);
        return builder.ignoreExitValueCheck(true).build();
    }

    private static CommandInfo.CommandInfoBuilder getBaseJavaAgentFaultInjectionCommandInfo(
            CommandInfo.CommandInfoBuilder builder) {

        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfo.setExtractedPropertyName("faultId");
        commandOutputProcessingInfo.setRegExpression("[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        return builder.commandOutputProcessingInfoList(commandOutputProcessingInfoList)
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentFaultInjectionRequest())
                .ignoreExitValueCheck(false)
                .expectedCommandOutputList(Arrays.asList(Constants.SUCESSFUL_FAULT_CREATION_MESSAGE));
    }

    static CommandInfo getK8sJavaAgentFaultRemediationRequestCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec,
            JavaAgentFaultUtils javaAgentFaultUtils) {
        CommandInfo.CommandInfoBuilder builder = CommandInfo
                .builder(String.format(KUBE_FAULT_EXEC_STRING, jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                        jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                        javaAgentFaultUtils.buildRemediationCommand(jvmAgentFaultSpec.getInjectionHomeDir(),
                                String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()))));
        getBaseJavaAgentFaultRemediationRequestCommandInfo(builder);
        return builder.build();
    }

    static CommandInfo getDockerJavaAgentFaultRemediateRequestCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec,
            JavaAgentFaultUtils javaAgentFaultUtils) {
        CommandInfo.CommandInfoBuilder builder =
                CommandInfo.builder(javaAgentFaultUtils.buildRemediationCommand(jvmAgentFaultSpec.getInjectionHomeDir(),
                        String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())));
        getBaseJavaAgentFaultRemediationRequestCommandInfo(builder);
        CommandInfo baseCommandInfo = builder.build();
        log.debug("CommandInfo object RemediationCommandInfo:" + baseCommandInfo);
        return baseCommandInfo;
    }

    static CommandInfo getLinuxJavaAgentFaultRemediationRequestCommandInfo(
            CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo.CommandInfoBuilder builder = CommandInfo
                .builder(getBytemanSubmitCommand(jvmAgentFaultSpec).replace("%s", "-rf " + FI_ADD_INFO_FAULTID));
        return getBaseJavaAgentFaultRemediationRequestCommandInfo(builder).build();
    }

    private static CommandInfo.CommandInfoBuilder getBaseJavaAgentFaultRemediationRequestCommandInfo(
            CommandInfo.CommandInfoBuilder builder) {
        return builder.ignoreExitValueCheck(false)
                .expectedCommandOutputList(Arrays.asList(REMEDIATION_REQUEST_SUCCESSFUL_STRING))
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentFaultRemediationRequest());
    }

    static CommandInfo getK8sJavaAgentFaultRemediationVerificationCommandInfo(
            CommandExecutionFaultSpec jvmAgentFaultSpec, JavaAgentFaultUtils javaAgentFaultUtils) {
        CommandInfo.CommandInfoBuilder builder = CommandInfo
                .builder(String.format(KUBE_FAULT_EXEC_STRING, jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                        jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                        javaAgentFaultUtils.buildGetFaultCommand(jvmAgentFaultSpec.getInjectionHomeDir(),
                                String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()))));
        return getBaseJavaAgentFaultRemediationVerificationCommandInfo(builder).build();
    }

    static CommandInfo getDockerJavaAgentFaultRemediationVerificationCommandInfo(
            CommandExecutionFaultSpec jvmAgentFaultSpec, JavaAgentFaultUtils javaAgentFaultUtils) {
        CommandInfo.CommandInfoBuilder builder =
                CommandInfo.builder(javaAgentFaultUtils.buildGetFaultCommand(jvmAgentFaultSpec.getInjectionHomeDir(),
                        String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())));
        return getBaseJavaAgentFaultRemediationVerificationCommandInfo(builder).build();
    }

    static CommandInfo getLinuxJavaAgentFaultRemediationVerificationCommandInfo(
            CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo.CommandInfoBuilder builder = CommandInfo
                .builder(getBytemanSubmitCommand(jvmAgentFaultSpec).replace("%s", "-gf " + FI_ADD_INFO_FAULTID));
        return getBaseJavaAgentFaultRemediationVerificationCommandInfo(builder).build();
    }

    private static CommandInfo.CommandInfoBuilder getBaseJavaAgentFaultRemediationVerificationCommandInfo(
            CommandInfo.CommandInfoBuilder builder) {
        return builder.noOfRetries(NO_OF_RETRIES).retryInterval(RETRY_WAIT_INTERVAL).ignoreExitValueCheck(true)
                .expectedCommandOutputList(Arrays.asList(FAULT_COMPLETION_STRING, AGENT_NOT_AVAILABLE_STRING));
    }

    static CommandInfo getK8sJavaAgentRuleInstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec,
            String ruleFileName) {
        CommandInfo.CommandInfoBuilder builder = CommandInfo
                .builder(String.format(KUBE_FAULT_EXEC_STRING, jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                        jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                        String.format(SUBMIT_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(),
                                ((JVMCodeLevelFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())
                                + new StringBuilder().append(jvmAgentFaultSpec.getInjectionHomeDir()).append(AGENT_NAME)
                                        .append(FORWARD_SLASH).append(ruleFileName)));
        getBaseJavaAgentRuleInstallationCommandInfo(jvmAgentFaultSpec, builder);
        return builder.noOfRetries(10).retryInterval(5).build();
    }

    static CommandInfo getK8SCopyBytemanRuleFileInfo(String rule, String ruleFileName,
            CommandExecutionFaultSpec jvmAgentFaultSpec) {
        File ruleFile = new File(ConstantsUtils.getMangleSupportScriptDirectory() + ruleFileName);
        String rulereplaced = rule.replace("\\", "");
        log.debug("Byteman rule with escape characters removed.." + rulereplaced);
        try {
            FileUtils.write(ruleFile, rulereplaced, Charset.defaultCharset());
        } catch (IOException e) {
            log.error(e);
        }
        StringBuilder ruleFilepath = new StringBuilder().append(jvmAgentFaultSpec.getInjectionHomeDir())
                .append(AGENT_NAME).append(FORWARD_SLASH).append(ruleFileName);
        return CommandInfo
                .builder("cp " + ruleFile.getPath() + " " + jvmAgentFaultSpec.getK8sArguments().getPodInAction() + ":"
                        + ruleFilepath + " -c " + jvmAgentFaultSpec.getK8sArguments().getContainerName())
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentCopyOnK8sPod()).build();
    }

    static CommandInfo getDockerJavaAgentRuleInstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo.CommandInfoBuilder builder =
                CommandInfo.builder(String.format(SUBMIT_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(),
                        ((JVMCodeLevelFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())
                        + jvmAgentFaultSpec.getInjectionHomeDir() + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm");
        return getBaseJavaAgentRuleInstallationCommandInfo(jvmAgentFaultSpec, builder).build();
    }

    static CommandInfo getLinuxJavaAgentRuleInstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        // Byteman Submit or injection command
        CommandInfo.CommandInfoBuilder builder =
                CommandInfo.builder(getBytemanSubmitCommand(jvmAgentFaultSpec).replace("%s",
                        jvmAgentFaultSpec.getInjectionHomeDir() + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm"));
        return getBaseJavaAgentRuleInstallationCommandInfo(jvmAgentFaultSpec, builder).build();
    }

    private static CommandInfo.CommandInfoBuilder getBaseJavaAgentRuleInstallationCommandInfo(
            CommandExecutionFaultSpec jvmAgentFaultSpec, CommandInfo.CommandInfoBuilder builder) {
        List<String> expectedCommandInfolist = new ArrayList<>();
        expectedCommandInfolist.add("install rule " + jvmAgentFaultSpec.getArgs().get(TASK_ID));
        expectedCommandInfolist.add("redefine rule " + jvmAgentFaultSpec.getArgs().get(TASK_ID));
        return builder.ignoreExitValueCheck(false).expectedCommandOutputList(expectedCommandInfolist);
    }

    static CommandInfo getK8sJavaAgentRuleUninstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        String ruleFileName = jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm";
        CommandInfo.CommandInfoBuilder builder = CommandInfo.builder(String.format(String.format(KUBE_FAULT_EXEC_STRING,
                jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                String.format(SUBMIT_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(), new StringBuilder()
                        .append(((JVMCodeLevelFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()).append(" -u ")
                        .append(new StringBuilder().append(jvmAgentFaultSpec.getInjectionHomeDir()).append(AGENT_NAME)
                                .append(FORWARD_SLASH).append(ruleFileName))
                        .toString()))));
        getBaseJavaAgentRuleUninstallationCommandInfo(jvmAgentFaultSpec, builder);
        return builder.ignoreExitValueCheck(true).build();
    }

    static CommandInfo getDockerJavaAgentRuleUninstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        String ruleFileName = jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm";
        CommandInfo.CommandInfoBuilder builder =
                CommandInfo.builder(String.format(SUBMIT_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(),
                        ((JVMCodeLevelFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()) + "-u "
                        + jvmAgentFaultSpec.getInjectionHomeDir() + ruleFileName);
        getBaseJavaAgentRuleUninstallationCommandInfo(jvmAgentFaultSpec, builder);
        return builder.ignoreExitValueCheck(true).build();
    }

    static CommandInfo getJavaAgentRuleDeleteCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        String ruleFileName = jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm";
        CommandInfo.CommandInfoBuilder builder;
        if (jvmAgentFaultSpec.getEndpoint().getEndPointType() == EndpointType.K8S_CLUSTER) {
            builder = CommandInfo
                    .builder(DELETE_COMMAND + new StringBuilder().append(jvmAgentFaultSpec.getInjectionHomeDir())
                            .append(AGENT_NAME).append(FORWARD_SLASH).append(ruleFileName));
        } else {
            builder = CommandInfo.builder(DELETE_COMMAND + jvmAgentFaultSpec.getInjectionHomeDir()
                    + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm");
        }
        builder.ignoreExitValueCheck(true);
        CommandInfo deleteRulefileCommandInfo = builder.build();
        log.debug("CommandInfo object for Delete Rule file CommandInfo :" + deleteRulefileCommandInfo);
        return deleteRulefileCommandInfo;
    }

    static CommandInfo getLinuxJavaAgentRuleUninstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo.CommandInfoBuilder builder = CommandInfo.builder(getBytemanSubmitCommand(jvmAgentFaultSpec).replace(
                "%s",
                "-u " + jvmAgentFaultSpec.getInjectionHomeDir() + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm"));
        return getBaseJavaAgentRuleUninstallationCommandInfo(jvmAgentFaultSpec, builder).build();
    }

    private static CommandInfo.CommandInfoBuilder getBaseJavaAgentRuleUninstallationCommandInfo(
            CommandExecutionFaultSpec jvmAgentFaultSpec, CommandInfo.CommandInfoBuilder builder) {
        return builder.ignoreExitValueCheck(false)
                .expectedCommandOutputList(Arrays.asList("uninstall RULE " + jvmAgentFaultSpec.getArgs().get(TASK_ID)));
    }

    static CommandInfo getK8sJavaAgentEnableTrobleshootingCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        return CommandInfo
                .builder(String.format(KUBE_FAULT_EXEC_STRING, jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                        jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                        String.format(SUBMIT_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(),
                                ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())
                                + "-enableTroubleshooting"))
                .ignoreExitValueCheck(true)
                .expectedCommandOutputList(
                        Arrays.asList(SUCCESSFUL_TROUBLESHOOTING_ENABLED_MESSAGE, ENABLE_TROUBLESHOOTING_RETRY_MESSAGE))
                .noOfRetries(10).retryInterval(5).build();
    }

    static CommandInfo getK8sJavaAgentCopyCommand(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        return CommandInfo
                .builder(new StringBuilder().append("cp ").append(ConstantsUtils.getMangleSupportScriptDirectory())
                        .append(AGENT_NAME).append(" ")
                        .append(jvmAgentFaultSpec.getK8sArguments().getPodInAction() + ":")
                        .append(jvmAgentFaultSpec.getInjectionHomeDir()).append(AGENT_NAME)
                        .append(Constants.K8S_CONTAINER_OPTION)
                        .append(jvmAgentFaultSpec.getK8sArguments().getContainerName()).toString())
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentCopyOnK8sPod()).build();
    }

    static void createDockerRuleFile(CommandExecutionFaultSpec jvmAgentFaultSpec, String rule) {
        String ruleFile = jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm";

        String ruleFileName = writeRuleToFile(rule, ruleFile);
        updateSupportScriptInfo(jvmAgentFaultSpec,
                getSupportScriptInfo(ruleFileName, jvmAgentFaultSpec.getInjectionHomeDir()));
    }


    static CommandInfo getLinuxJavAgentExtractCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        // Tar extraction command
        return CommandInfo
                .builder(String.format(EXTRACT_AGENT_COMMAND, jvmAgentFaultSpec.getInjectionHomeDir(),
                        AGENT_NAME + AGENT_JAR_EXTENSION))
                .ignoreExitValueCheck(true).expectedCommandOutputList(Arrays.asList("")).build();
    }


    static CommandInfo getLinuxJavaAgentScriptsPermissionsUpdateCommandInfo(
            CommandExecutionFaultSpec jvmAgentFaultSpec) {
        String agentPathInTargetMachine = jvmAgentFaultSpec.getInjectionHomeDir() + AGENT_NAME;
        // change permission command
        return CommandInfo
                .builder(
                        "chmod -R 777 " + agentPathInTargetMachine + ";chmod -R 777 " + agentPathInTargetMachine + "/*")
                .ignoreExitValueCheck(true).expectedCommandOutputList(Arrays.asList("")).build();
    }

    static CommandInfo getLinuxJavaAgentRuleCreationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec,
            String rule) {
        String ruleFile = jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm";
        String ruleFileName = writeRuleToFile(rule, ruleFile);
        updateSupportScriptInfo(jvmAgentFaultSpec,
                getSupportScriptInfo(ruleFileName, jvmAgentFaultSpec.getInjectionHomeDir()));
        return CommandInfo.builder("chmod 777 " + jvmAgentFaultSpec.getInjectionHomeDir() + ruleFile)
                .expectedCommandOutputList(Arrays.asList("")).build();
    }

    private static String writeRuleToFile(String rule, String ruleFileName) {
        File ruleFile = new File(ConstantsUtils.getMangleSupportScriptDirectory() + ruleFileName);
        String rulereplaced = rule.replace("\\", "");
        log.debug("Byteman rule with escape characters removed.." + rulereplaced);
        try {
            FileUtils.write(ruleFile, rulereplaced, Charset.defaultCharset());
        } catch (IOException e) {
            log.error(e);
        }
        return ruleFile.getName();
    }

    private static SupportScriptInfo getSupportScriptInfo(String ruleFileName, String targetDirectory) {
        SupportScriptInfo scriptInfo = new SupportScriptInfo();
        scriptInfo.setClassPathResource(true);
        scriptInfo.setExecutable(false);
        scriptInfo.setScriptFileName(ruleFileName);
        scriptInfo.setTargetDirectoryPath(targetDirectory);
        return scriptInfo;
    }

    private static void updateSupportScriptInfo(CommandExecutionFaultSpec faultSpec, SupportScriptInfo scriptInfo) {
        if (CollectionUtils.isEmpty(faultSpec.getSupportScriptInfo())) {
            log.debug("Support Script list is empty. Creating and adding the byteman rule");
            ArrayList<SupportScriptInfo> scripts = new ArrayList<>();
            scripts.add(scriptInfo);
            faultSpec.setSupportScriptInfo(scripts);
        } else {
            log.debug("Updating the script info list with Byteman rule file");
            faultSpec.getSupportScriptInfo().add(scriptInfo);
        }
    }


    private static String getLinuxJavaAgentInstallationCommand(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        Integer localSocketPort = ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort();
        String bmInstallCommand = String.format(PID_ATTACH_MXBEANS_COMMAND_WITH_PORT,
                jvmAgentFaultSpec.getInjectionHomeDir(), localSocketPort, jvmAgentFaultSpec.getArgs().get(PROCESS));
        if (null != jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH)) {
            bmInstallCommand = Constants.SET_JAVA_HOME_CMD + jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH)
                    + ";export PATH=$JAVA_HOME/bin:$PATH;" + bmInstallCommand;
        }
        if (null != jvmAgentFaultSpec.getArgs().get(USER)) {

            bmInstallCommand =
                    "sudo -u " + jvmAgentFaultSpec.getArgs().get(USER) + " bash -c \"" + bmInstallCommand + "\"";
        }
        return bmInstallCommand;
    }

    private static String getBytemanSubmitCommand(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        Integer localSocketPort = ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort();
        String bmSubmitCommand =
                String.format(SUBMIT_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(), localSocketPort)
                        + "%s";
        if (null != jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH)) {
            bmSubmitCommand = Constants.SET_JAVA_HOME_CMD + jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH)
                    + ";export PATH=$JAVA_HOME/bin:$PATH;" + bmSubmitCommand;
        }
        if (null != jvmAgentFaultSpec.getArgs().get(USER)) {
            bmSubmitCommand =
                    "sudo -u " + jvmAgentFaultSpec.getArgs().get(USER) + " bash -c \"" + bmSubmitCommand + "\"";
        }
        return bmSubmitCommand;
    }

    public static boolean isManualRemediationSupported(String faultName) {
        if (faultName.equals(AgentFaultName.INJECT_FILE_HANDLER_FAULT.getValue())
                || faultName.equals(AgentFaultName.INJECT_THREAD_LEAK_FAULT.getValue())) {
            log.info(String.format(FaultConstants.MANUAL_REMEDIATION_NOT_SUPPORTED, faultName));
            return false;
        }
        return true;
    }
}
