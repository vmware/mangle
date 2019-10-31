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
import com.vmware.mangle.task.framework.utils.DockerCommandUtils;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam, dbhat
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
            BytemanCommandInfoHelper.createDockerRuleFile(jvmAgentFaultSpec, generateRule(jvmAgentFaultSpec));
            commandInfoList.add(BytemanCommandInfoHelper.getDockerJavaAgentInstallationCommandInfo(jvmAgentFaultSpec));
            commandInfoList
                    .add(BytemanCommandInfoHelper.getDockerJavaAgentRuleInstallationCommandInfo(jvmAgentFaultSpec));
            return commandInfoList;
        } else {
            List<CommandInfo> commandInfoList = new ArrayList<>();
            commandInfoList.add(BytemanCommandInfoHelper.getDockerJavaAgentInstallationCommandInfo(jvmAgentFaultSpec));
            commandInfoList.add(BytemanCommandInfoHelper.getDockerJavaAgentFaultInjectionCommandInfo(jvmAgentFaultSpec,
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
                    .add(BytemanCommandInfoHelper.getDockerJavaAgentRuleUninstallationCommandInfo(jvmAgentFaultSpec));
            commandInfoList.add(BytemanCommandInfoHelper.getJavaAgentRuleDeleteCommandInfo(jvmAgentFaultSpec));
            return commandInfoList;
        } else {
            if (!BytemanCommandInfoHelper.isManualRemediationSupported(jvmAgentFaultSpec.getFaultName())) {
                return Collections.emptyList();
            }
            List<CommandInfo> commandInfoList = new ArrayList<>();
            commandInfoList.add(BytemanCommandInfoHelper
                    .getDockerJavaAgentFaultRemediateRequestCommandInfo(jvmAgentFaultSpec, javaAgentFaultUtils));
            commandInfoList.add(BytemanCommandInfoHelper
                    .getDockerJavaAgentFaultRemediationVerificationCommandInfo(jvmAgentFaultSpec, javaAgentFaultUtils));
            return commandInfoList;
        }
    }

    @Override
    public List<SupportScriptInfo> getAgentFaultInjectionScripts(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        return getAgentFaultScripts(jvmAgentFaultSpec, jvmAgentFaultSpec.getInjectionHomeDir(), AGENT_NAME);
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
