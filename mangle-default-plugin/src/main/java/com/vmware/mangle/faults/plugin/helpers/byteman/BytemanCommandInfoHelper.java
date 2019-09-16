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
import java.util.Collections;
import java.util.List;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
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
 * @author hkilari
 *
 */
@UtilityClass
@Log4j2
public class BytemanCommandInfoHelper {

    static CommandInfo getK8sJavaAgentInstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo baseCommandInfo = getBaseJavaAgentInstallationCommandInfo();
        if (!StringUtils.isEmpty(jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH))) {
            baseCommandInfo.setCommand(String.format(PID_K8S_ATTACH_MXBEANS_COMMAND_WITH_PORT_JAVA_HOME,
                    jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                    jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                    SET_JAVA_HOME_CMD + jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH) + " && "
                            + jvmAgentFaultSpec.getInjectionHomeDir(),
                    ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort(),
                    jvmAgentFaultSpec.getArgs().get(PROCESS)));
        } else {
            baseCommandInfo.setCommand(String.format(PID_K8S_ATTACH_MXBEANS_COMMAND_WITH_PORT,
                    jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                    jvmAgentFaultSpec.getK8sArguments().getContainerName(), jvmAgentFaultSpec.getInjectionHomeDir(),
                    ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort(),
                    jvmAgentFaultSpec.getArgs().get(PROCESS)));
        }
        baseCommandInfo.setNoOfRetries(10);
        baseCommandInfo.setRetryInterval(5);
        return baseCommandInfo;
    }

    static CommandInfo getDockerJavaAgentInstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo baseCommandInfo = getBaseJavaAgentInstallationCommandInfo();
        if (!StringUtils.isEmpty(jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH))) {
            baseCommandInfo.setCommand(String.format(
                    SET_JAVA_HOME_CMD + jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH) + "&&"
                            + PID_ATTACH_MXBEANS_COMMAND_WITH_PORT,
                    jvmAgentFaultSpec.getInjectionHomeDir(),
                    ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort(),
                    jvmAgentFaultSpec.getArgs().get(PROCESS)));
        } else {
            baseCommandInfo.setCommand(
                    String.format(PID_ATTACH_MXBEANS_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(),
                            ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort(),
                            jvmAgentFaultSpec.getArgs().get(PROCESS)));
        }
        log.debug("CommandInfo object of attachBeansCommandInfo :" + baseCommandInfo);
        return baseCommandInfo;
    }

    static CommandInfo getLinuxJavaAgentInstallCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        // Byteman install command
        CommandInfo baseCommandInfo = getBaseJavaAgentInstallationCommandInfo();
        baseCommandInfo.setCommand(getLinuxJavaAgentInstallationCommand(jvmAgentFaultSpec));
        return baseCommandInfo;
    }

    private static CommandInfo getBaseJavaAgentInstallationCommandInfo() {
        CommandInfo baseCommandInfo = new CommandInfo();
        baseCommandInfo.setIgnoreExitValueCheck(true);
        baseCommandInfo.setExpectedCommandOutputList(
                Arrays.asList(SUCCESSFUL_BYTEMAN_AGENT_INSTALLATION_MESSAGE, BYTEMAN_AGENT_INSTALLATION_RETRY_MESSAGE));
        baseCommandInfo.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentInstallationRequest());
        return baseCommandInfo;
    }

    static CommandInfo getK8sJavaAgentFaultInjectionCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec,
            JavaAgentFaultUtils javaAgentFaultUtils) {
        CommandInfo baseCommandInfo = getBaseJavaAgentFaultInjectionCommandInfo();

        baseCommandInfo
                .setCommand(String.format(KUBE_FAULT_EXEC_STRING, jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                        jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                        javaAgentFaultUtils.buildInjectionCommand(jvmAgentFaultSpec.getArgs(),
                                jvmAgentFaultSpec.getInjectionHomeDir(),
                                String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()))));
        baseCommandInfo.setNoOfRetries(10);
        baseCommandInfo.setRetryInterval(5);
        return baseCommandInfo;
    }

    static CommandInfo getDockerJavaAgentFaultInjectionCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec,
            JavaAgentFaultUtils javaAgentFaultUtils) {
        CommandInfo baseCommandInfo = getBaseJavaAgentFaultInjectionCommandInfo();
        if (!StringUtils.isEmpty(jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH))) {
            baseCommandInfo.setCommand(String.format(SET_JAVA_HOME_CMD + jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH)
                    + "&&"
                    + javaAgentFaultUtils.buildInjectionCommand(jvmAgentFaultSpec.getArgs(),
                            jvmAgentFaultSpec.getInjectionHomeDir(),
                            String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()))));
        } else {
            baseCommandInfo.setCommand(javaAgentFaultUtils.buildInjectionCommand(jvmAgentFaultSpec.getArgs(),
                    jvmAgentFaultSpec.getInjectionHomeDir(),
                    String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())));
        }
        log.debug("CommandInfo object of InjectFaultCommandInfo :" + baseCommandInfo);

        return baseCommandInfo;
    }

    static CommandInfo getLinuxJavaAgentFaultInjectionCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        // TODO Validating Fault arguments
        CommandInfo baseCommandInfo = getBaseJavaAgentFaultInjectionCommandInfo();
        baseCommandInfo.setCommand(getBytemanSubmitCommand(jvmAgentFaultSpec).replace("%s",
                "-if " + CommonUtils.convertMaptoDelimitedString(jvmAgentFaultSpec.getArgs(), " ")));
        baseCommandInfo.setIgnoreExitValueCheck(true);
        return baseCommandInfo;
    }

    private static CommandInfo getBaseJavaAgentFaultInjectionCommandInfo() {
        CommandInfo baseCommandInfo = new CommandInfo();
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfo.setExtractedPropertyName("faultId");
        commandOutputProcessingInfo.setRegExpression("[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);

        baseCommandInfo.setCommandOutputProcessingInfoList(commandOutputProcessingInfoList);
        baseCommandInfo.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentFaultInjectionRequest());
        baseCommandInfo.setIgnoreExitValueCheck(false);
        baseCommandInfo.setExpectedCommandOutputList(Arrays.asList(Constants.SUCESSFUL_FAULT_CREATION_MESSAGE));

        return baseCommandInfo;
    }

    static CommandInfo getK8sJavaAgentFaultRemediationRequestCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec,
            JavaAgentFaultUtils javaAgentFaultUtils) {
        CommandInfo baseCommandInfo = getBaseJavaAgentFaultRemediationRequestCommandInfo();
        baseCommandInfo
                .setCommand(String.format(KUBE_FAULT_EXEC_STRING, jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                        jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                        javaAgentFaultUtils.buildRemediationCommand(jvmAgentFaultSpec.getInjectionHomeDir(),
                                String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()))));
        ;
        return baseCommandInfo;
    }

    static CommandInfo getDockerJavaAgentFaultRemediateRequestCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec,
            JavaAgentFaultUtils javaAgentFaultUtils) {
        CommandInfo baseCommandInfo = getBaseJavaAgentFaultRemediationRequestCommandInfo();
        baseCommandInfo.setCommand(javaAgentFaultUtils.buildRemediationCommand(jvmAgentFaultSpec.getInjectionHomeDir(),
                String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())));
        log.debug("CommandInfo object RemediationCommandInfo:" + baseCommandInfo);
        return baseCommandInfo;
    }

    static CommandInfo getLinuxJavaAgentFaultRemediationRequestCommandInfo(
            CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo baseCommandInfo = getBaseJavaAgentFaultRemediationRequestCommandInfo();
        baseCommandInfo
                .setCommand(getBytemanSubmitCommand(jvmAgentFaultSpec).replace("%s", "-rf " + FI_ADD_INFO_FAULTID));
        return baseCommandInfo;
    }

    private static CommandInfo getBaseJavaAgentFaultRemediationRequestCommandInfo() {
        CommandInfo baseCommandInfo = new CommandInfo();
        baseCommandInfo.setIgnoreExitValueCheck(false);
        baseCommandInfo.setExpectedCommandOutputList(Arrays.asList(REMEDIATION_REQUEST_SUCCESSFUL_STRING));
        baseCommandInfo.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentFaultRemediationRequest());
        return baseCommandInfo;
    }

    static CommandInfo getK8sJavaAgentFaultRemediationVerificationCommandInfo(
            CommandExecutionFaultSpec jvmAgentFaultSpec, JavaAgentFaultUtils javaAgentFaultUtils) {
        CommandInfo baseCommandInfo = getBaseJavaAgentFaultRemediationVerificationCommandInfo();
        baseCommandInfo
                .setCommand(String.format(KUBE_FAULT_EXEC_STRING, jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                        jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                        javaAgentFaultUtils.buildGetFaultCommand(jvmAgentFaultSpec.getInjectionHomeDir(),
                                String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()))));
        return baseCommandInfo;
    }

    static CommandInfo getDockerJavaAgentFaultRemediationVerificationCommandInfo(
            CommandExecutionFaultSpec jvmAgentFaultSpec, JavaAgentFaultUtils javaAgentFaultUtils) {
        CommandInfo baseCommandInfo = getBaseJavaAgentFaultRemediationVerificationCommandInfo();
        baseCommandInfo.setCommand(javaAgentFaultUtils.buildGetFaultCommand(jvmAgentFaultSpec.getInjectionHomeDir(),
                String.valueOf(((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())));
        log.debug("CommandInfo object FaultCommandInfo :" + baseCommandInfo);
        return baseCommandInfo;
    }

    static CommandInfo getLinuxJavaAgentFaultRemediationVerificationCommandInfo(
            CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo baseCommandInfo = getBaseJavaAgentFaultRemediationVerificationCommandInfo();
        baseCommandInfo
                .setCommand(getBytemanSubmitCommand(jvmAgentFaultSpec).replace("%s", "-gf " + FI_ADD_INFO_FAULTID));
        return baseCommandInfo;
    }

    private static CommandInfo getBaseJavaAgentFaultRemediationVerificationCommandInfo() {
        CommandInfo baseCommandInfo = new CommandInfo();
        baseCommandInfo.setNoOfRetries(NO_OF_RETRIES);
        baseCommandInfo.setRetryInterval(RETRY_WAIT_INTERVAL);
        baseCommandInfo.setIgnoreExitValueCheck(true);
        baseCommandInfo
                .setExpectedCommandOutputList(Arrays.asList(FAULT_COMPLETION_STRING, AGENT_NOT_AVAILABLE_STRING));
        return baseCommandInfo;
    }

    static CommandInfo getK8sJavaAgentRuleInstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec,
            String ruleFileName) {
        CommandInfo baseCommandInfo = getBaseJavaAgentRuleInstallationCommandInfo(jvmAgentFaultSpec);
        baseCommandInfo
                .setCommand(String.format(KUBE_FAULT_EXEC_STRING, jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                        jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                        String.format(SUBMIT_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(),
                                ((JVMCodeLevelFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())
                                + new StringBuilder().append(jvmAgentFaultSpec.getInjectionHomeDir()).append(AGENT_NAME)
                                        .append(FORWARD_SLASH).append(ruleFileName)));

        baseCommandInfo.setNoOfRetries(10);
        baseCommandInfo.setRetryInterval(5);
        return baseCommandInfo;
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
        CommandInfo k8sCopyBytemanRuleFileCommandInfo = new CommandInfo();
        StringBuilder ruleFilepath = new StringBuilder().append(jvmAgentFaultSpec.getInjectionHomeDir())
                .append(AGENT_NAME).append(FORWARD_SLASH).append(ruleFileName);
        k8sCopyBytemanRuleFileCommandInfo
                .setCommand("cp " + ruleFile.getPath() + " " + jvmAgentFaultSpec.getK8sArguments().getPodInAction()
                        + ":" + ruleFilepath + " -c " + jvmAgentFaultSpec.getK8sArguments().getContainerName());
        k8sCopyBytemanRuleFileCommandInfo.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentCopyOnK8sPod());
        return k8sCopyBytemanRuleFileCommandInfo;
    }

    static CommandInfo getDockerJavaAgentRuleInstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo baseCommandInfo = getBaseJavaAgentRuleInstallationCommandInfo(jvmAgentFaultSpec);
        baseCommandInfo.setCommand(String.format(SUBMIT_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(),
                ((JVMCodeLevelFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())
                + jvmAgentFaultSpec.getInjectionHomeDir() + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm");
        return baseCommandInfo;
    }

    static CommandInfo getLinuxJavaAgentRuleInstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo baseCommandInfo = getBaseJavaAgentRuleInstallationCommandInfo(jvmAgentFaultSpec);
        // Byteman Submit or injection command
        baseCommandInfo.setCommand(getBytemanSubmitCommand(jvmAgentFaultSpec).replace("%s",
                jvmAgentFaultSpec.getInjectionHomeDir() + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm"));
        return baseCommandInfo;
    }

    private static CommandInfo getBaseJavaAgentRuleInstallationCommandInfo(
            CommandExecutionFaultSpec jvmAgentFaultSpec) {
        List<String> expectedCommandInfolist = new ArrayList<String>();
        expectedCommandInfolist.add("install rule " + jvmAgentFaultSpec.getArgs().get(TASK_ID));
        expectedCommandInfolist.add("redefine rule " + jvmAgentFaultSpec.getArgs().get(TASK_ID));
        CommandInfo baseCommandInfo = new CommandInfo();
        baseCommandInfo.setIgnoreExitValueCheck(false);
        baseCommandInfo.setExpectedCommandOutputList(expectedCommandInfolist);
        return baseCommandInfo;
    }

    static CommandInfo getK8sJavaAgentRuleUninstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        String ruleFileName = jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm";
        CommandInfo baseCommandInfo = getBaseJavaAgentRuleUninstallationCommandInfo(jvmAgentFaultSpec);
        baseCommandInfo.setCommand(String.format(String.format(KUBE_FAULT_EXEC_STRING,
                jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                String.format(SUBMIT_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(), new StringBuilder()
                        .append(((JVMCodeLevelFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()).append(" -u ")
                        .append(new StringBuilder().append(jvmAgentFaultSpec.getInjectionHomeDir()).append(AGENT_NAME)
                                .append(FORWARD_SLASH).append(ruleFileName))
                        .toString()))));
        baseCommandInfo.setIgnoreExitValueCheck(true);
        return baseCommandInfo;
    }

    static CommandInfo getDockerJavaAgentRuleUninstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        String ruleFileName = jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm";
        CommandInfo baseCommandInfo = getBaseJavaAgentRuleUninstallationCommandInfo(jvmAgentFaultSpec);
        baseCommandInfo.setCommand(String.format(SUBMIT_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(),
                ((JVMCodeLevelFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort()) + "-u "
                + jvmAgentFaultSpec.getInjectionHomeDir() + ruleFileName);
        baseCommandInfo.setIgnoreExitValueCheck(true);
        log.debug("CommandInfo object for RemediationCommandInfo :" + baseCommandInfo);
        return baseCommandInfo;
    }

    static CommandInfo getJavaAgentRuleDeleteCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        String ruleFileName = jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm";
        CommandInfo deleteRulefileCommandInfo = new CommandInfo();
        if (jvmAgentFaultSpec.getEndpoint().getEndPointType() == EndpointType.K8S_CLUSTER) {
            deleteRulefileCommandInfo
                    .setCommand(DELETE_COMMAND + new StringBuilder().append(jvmAgentFaultSpec.getInjectionHomeDir())
                            .append(AGENT_NAME).append(FORWARD_SLASH).append(ruleFileName));
        } else {
            deleteRulefileCommandInfo.setCommand(DELETE_COMMAND + jvmAgentFaultSpec.getInjectionHomeDir()
                    + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm");
        }
        deleteRulefileCommandInfo.setIgnoreExitValueCheck(true);
        log.debug("CommandInfo object for Delete Rule file CommandInfo :" + deleteRulefileCommandInfo);
        return deleteRulefileCommandInfo;
    }

    static CommandInfo getLinuxJavaAgentRuleUninstallationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo baseCommandInfo = getBaseJavaAgentRuleUninstallationCommandInfo(jvmAgentFaultSpec);
        baseCommandInfo.setCommand(getBytemanSubmitCommand(jvmAgentFaultSpec).replace("%s",
                "-u " + jvmAgentFaultSpec.getInjectionHomeDir() + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm"));
        return baseCommandInfo;
    }

    private static CommandInfo getBaseJavaAgentRuleUninstallationCommandInfo(
            CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo baseCommandInfo = new CommandInfo();
        baseCommandInfo.setIgnoreExitValueCheck(false);
        baseCommandInfo.setExpectedCommandOutputList(
                Arrays.asList("uninstall RULE " + jvmAgentFaultSpec.getArgs().get(TASK_ID)));
        return baseCommandInfo;
    }

    static CommandInfo getK8sJavaAgentEnableTrobleshootingCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {

        CommandInfo enableTrobleshootingCommandInfo = new CommandInfo();
        enableTrobleshootingCommandInfo
                .setCommand(String.format(KUBE_FAULT_EXEC_STRING, jvmAgentFaultSpec.getK8sArguments().getPodInAction(),
                        jvmAgentFaultSpec.getK8sArguments().getContainerName(),
                        String.format(SUBMIT_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(),
                                ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort())
                                + "-enableTroubleshooting"));
        enableTrobleshootingCommandInfo.setIgnoreExitValueCheck(true);
        enableTrobleshootingCommandInfo.setExpectedCommandOutputList(
                Arrays.asList(SUCCESSFUL_TROUBLESHOOTING_ENABLED_MESSAGE, ENABLE_TROUBLESHOOTING_RETRY_MESSAGE));
        enableTrobleshootingCommandInfo.setNoOfRetries(10);
        enableTrobleshootingCommandInfo.setRetryInterval(5);
        return enableTrobleshootingCommandInfo;
    }

    static CommandInfo getK8sJavaAgentCopyCommand(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo k8sCopyCommandInfo = new CommandInfo();
        k8sCopyCommandInfo.setCommand(new StringBuilder().append("cp ")
                .append(ConstantsUtils.getMangleSupportScriptDirectory()).append(AGENT_NAME).append(" ")
                .append(jvmAgentFaultSpec.getK8sArguments().getPodInAction() + ":")
                .append(jvmAgentFaultSpec.getInjectionHomeDir()).append(AGENT_NAME)
                .append(Constants.K8S_CONTAINER_OPTION).append(jvmAgentFaultSpec.getK8sArguments().getContainerName())
                .toString());
        k8sCopyCommandInfo.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentCopyOnK8sPod());
        return k8sCopyCommandInfo;
    }

    static CommandInfo getDockerCreateBytemanRuleCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec, String rule) {
        CommandInfo createBytemanRuleCommandInfo = new CommandInfo();
        createBytemanRuleCommandInfo.setExpectedCommandOutputList(Collections.emptyList());
        createBytemanRuleCommandInfo.setCommand("echo \"" + rule + "\" > " + jvmAgentFaultSpec.getInjectionHomeDir()
                + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm");
        return createBytemanRuleCommandInfo;
    }


    static CommandInfo getLinuxJavAgentExtractCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        CommandInfo extractAgentCommandInfo = new CommandInfo();
        // Tar extraction command
        extractAgentCommandInfo.setCommand(String.format(EXTRACT_AGENT_COMMAND, jvmAgentFaultSpec.getInjectionHomeDir(),
                AGENT_NAME + AGENT_JAR_EXTENSION));
        extractAgentCommandInfo.setIgnoreExitValueCheck(true);
        extractAgentCommandInfo.setExpectedCommandOutputList(Arrays.asList(""));
        return extractAgentCommandInfo;
    }


    static CommandInfo getLinuxJavaAgentScriptsPermissionsUpdateCommandInfo(
            CommandExecutionFaultSpec jvmAgentFaultSpec) {
        String agentPathInTargetMachine = jvmAgentFaultSpec.getInjectionHomeDir() + AGENT_NAME;
        CommandInfo changePermissionCommandInfo = new CommandInfo();
        // change permission command
        changePermissionCommandInfo.setCommand(
                "chmod -R 777 " + agentPathInTargetMachine + ";chmod -R 777 " + agentPathInTargetMachine + "/*");
        changePermissionCommandInfo.setIgnoreExitValueCheck(true);
        changePermissionCommandInfo.setExpectedCommandOutputList(Arrays.asList(""));
        return changePermissionCommandInfo;
    }

    static CommandInfo getLinuxJavaAgentRuleCreationCommandInfo(CommandExecutionFaultSpec jvmAgentFaultSpec,
            String rule) {
        CommandInfo createBytemanRuleCommandInfo = new CommandInfo();
        // Creating Byteman Rule Command
        createBytemanRuleCommandInfo.setExpectedCommandOutputList(Arrays.asList(""));
        createBytemanRuleCommandInfo.setCommand("echo \"" + rule + "\" > " + jvmAgentFaultSpec.getInjectionHomeDir()
                + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm");
        return createBytemanRuleCommandInfo;
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
