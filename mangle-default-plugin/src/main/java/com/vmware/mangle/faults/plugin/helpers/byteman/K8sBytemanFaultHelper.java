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
import static com.vmware.mangle.utils.constants.FaultConstants.FORWARD_SLASH;
import static com.vmware.mangle.utils.constants.FaultConstants.TASK_ID;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.JavaAgentFaultUtils;
import com.vmware.mangle.faults.plugin.utils.PluginUtils;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
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
        if (jvmAgentFaultSpec instanceof JVMCodeLevelFaultSpec) {
            List<CommandInfo> commandInfoList = new ArrayList<>();
            commandInfoList.add(BytemanCommandInfoHelper.getK8sJavaAgentCopyCommand(jvmAgentFaultSpec));
            commandInfoList.add(BytemanCommandInfoHelper.getK8sJavaAgentInstallationCommandInfo(jvmAgentFaultSpec));
            commandInfoList
                    .add(BytemanCommandInfoHelper.getK8sJavaAgentEnableTrobleshootingCommandInfo(jvmAgentFaultSpec));
            String ruleFileName = jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm";

            commandInfoList.add(BytemanCommandInfoHelper.getK8SCopyBytemanRuleFileInfo(generateRule(jvmAgentFaultSpec),
                    ruleFileName, jvmAgentFaultSpec));
            commandInfoList.add(BytemanCommandInfoHelper.getK8sJavaAgentRuleInstallationCommandInfo(jvmAgentFaultSpec,
                    ruleFileName));
            return commandInfoList;
        } else {
            List<CommandInfo> commandInfoList = new ArrayList<>();
            commandInfoList.add(BytemanCommandInfoHelper.getK8sJavaAgentCopyCommand(jvmAgentFaultSpec));
            commandInfoList.add(BytemanCommandInfoHelper.getK8sJavaAgentInstallationCommandInfo(jvmAgentFaultSpec));
            commandInfoList
                    .add(BytemanCommandInfoHelper.getK8sJavaAgentEnableTrobleshootingCommandInfo(jvmAgentFaultSpec));
            commandInfoList.add(BytemanCommandInfoHelper.getK8sJavaAgentFaultInjectionCommandInfo(jvmAgentFaultSpec,
                    javaAgentFaultUtils));
            return commandInfoList;
        }
    }

    @Override
    public List<CommandInfo> getRemediationCommandInfoList(CommandExecutionFaultSpec jvmAgentFaultSpec)
            throws MangleException {
        if (jvmAgentFaultSpec instanceof JVMCodeLevelFaultSpec) {
            List<CommandInfo> commandInfoList = new ArrayList<>();
            commandInfoList
                    .add(BytemanCommandInfoHelper.getK8sJavaAgentRuleUninstallationCommandInfo(jvmAgentFaultSpec));
            commandInfoList.add(BytemanCommandInfoHelper.getJavaAgentRuleDeleteCommandInfo(jvmAgentFaultSpec));
            return commandInfoList;
        } else {
            if (!BytemanCommandInfoHelper.isManualRemediationSupported(jvmAgentFaultSpec.getFaultName())) {
                return Collections.emptyList();
            }
            List<CommandInfo> commandInfoList = new ArrayList<>();
            commandInfoList.add(BytemanCommandInfoHelper
                    .getK8sJavaAgentFaultRemediationRequestCommandInfo(jvmAgentFaultSpec, javaAgentFaultUtils));
            commandInfoList.add(BytemanCommandInfoHelper
                    .getK8sJavaAgentFaultRemediationVerificationCommandInfo(jvmAgentFaultSpec, javaAgentFaultUtils));
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
