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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author hkilari, dbhat
 *
 */
@Log4j2
public class LinuxBytemanFaultHelper extends BytemanFaultHelper {

    private EndpointClientFactory endpointClientFactory;

    @Autowired
    public void setEndpointClientFactory(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;

    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec spec) throws MangleException {
        return (SSHUtils) endpointClientFactory.getEndPointClient(spec.getCredentials(), spec.getEndpoint());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.mangle.services.helpers.IAgentFaultHelper#
     * getInjectedCommandInfoList()
     */
    @Override
    public List<CommandInfo> getInjectionCommandInfoList(CommandExecutionFaultSpec jvmAgentFaultSpec)
            throws MangleException {
        // Condition to check for Byteman rule based fault
        if (jvmAgentFaultSpec instanceof JVMCodeLevelFaultSpec) {
            List<CommandInfo> commandInfoList = new ArrayList<>();
            // Adding all the commandinfos to list
            commandInfoList.add(BytemanCommandInfoHelper.getLinuxJavAgentExtractCommandInfo(jvmAgentFaultSpec));
            commandInfoList.add(
                    BytemanCommandInfoHelper.getLinuxJavaAgentScriptsPermissionsUpdateCommandInfo(jvmAgentFaultSpec));
            commandInfoList.add(BytemanCommandInfoHelper.getLinuxJavaAgentInstallCommandInfo(jvmAgentFaultSpec));
            commandInfoList.add(BytemanCommandInfoHelper.getLinuxJavaAgentRuleCreationCommandInfo(jvmAgentFaultSpec,
                    generateRule(jvmAgentFaultSpec)));
            commandInfoList
                    .add(BytemanCommandInfoHelper.getLinuxJavaAgentRuleInstallationCommandInfo(jvmAgentFaultSpec));
            return commandInfoList;
        } else {
            List<CommandInfo> commandInfoList = new ArrayList<>();

            commandInfoList.add(BytemanCommandInfoHelper.getLinuxJavAgentExtractCommandInfo(jvmAgentFaultSpec));
            commandInfoList.add(
                    BytemanCommandInfoHelper.getLinuxJavaAgentScriptsPermissionsUpdateCommandInfo(jvmAgentFaultSpec));
            commandInfoList.add(BytemanCommandInfoHelper.getLinuxJavaAgentInstallCommandInfo(jvmAgentFaultSpec));
            commandInfoList.add(BytemanCommandInfoHelper.getLinuxJavaAgentFaultInjectionCommandInfo(jvmAgentFaultSpec));
            return commandInfoList;

        }
    }

    @Override
    public List<CommandInfo> getRemediationCommandInfoList(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        // Condition to check for Byteman rule based fault
        if (jvmAgentFaultSpec instanceof JVMCodeLevelFaultSpec) {
            List<CommandInfo> commandInfoList = new ArrayList<>();
            commandInfoList
                    .add(BytemanCommandInfoHelper.getLinuxJavaAgentRuleUninstallationCommandInfo(jvmAgentFaultSpec));
            commandInfoList.add(BytemanCommandInfoHelper.getJavaAgentRuleDeleteCommandInfo(jvmAgentFaultSpec));
            return commandInfoList;
        } else {
            if (!BytemanCommandInfoHelper.isManualRemediationSupported(jvmAgentFaultSpec.getFaultName())) {
                return Collections.emptyList();
            }
            List<CommandInfo> commandInfoList = new ArrayList<>();
            commandInfoList.add(
                    BytemanCommandInfoHelper.getLinuxJavaAgentFaultRemediationRequestCommandInfo(jvmAgentFaultSpec));
            commandInfoList.add(BytemanCommandInfoHelper
                    .getLinuxJavaAgentFaultRemediationVerificationCommandInfo(jvmAgentFaultSpec));
            return commandInfoList;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.mangle.services.helpers.IAgentFaultHelper#
     * getAgentFaultInjectionScripts()
     */
    @Override
    public List<SupportScriptInfo> getAgentFaultInjectionScripts(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        return getAgentFaultScripts(jvmAgentFaultSpec, jvmAgentFaultSpec.getInjectionHomeDir(), AGENT_NAME + AGENT_JAR_EXTENSION);
    }

    @Override
    public void checkTaskSpecificPrerequisites() throws MangleException {
        // Found No Task Specific Requirements
    }
}
