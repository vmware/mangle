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

package com.vmware.mangle.faults.plugin.helpers.systemresource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.FaultConstants;
import com.vmware.mangle.faults.plugin.helpers.KnownFailuresHelper;
import com.vmware.mangle.services.enums.FaultName;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author jayasankarr
 *
 */

public abstract class SystemResourceFaultHelper {

    protected SystemResourceFaultUtils systemResourceFaultUtils;

    public SystemResourceFaultHelper(SystemResourceFaultUtils systemResourceFaultUtils) {
        this.systemResourceFaultUtils = systemResourceFaultUtils;
    }

    public abstract ICommandExecutor getExecutor(CommandExecutionFaultSpec faultSpec) throws MangleException;

    public List<CommandInfo> getInjectionCommandInfoList(CommandExecutionFaultSpec faultSpec) throws MangleException {
        String injectionCommand =
                systemResourceFaultUtils.buildInjectionCommand(faultSpec.getArgs(), faultSpec.getInjectionHomeDir());
        CommandInfo injectFaultCommandInfo = new CommandInfo();
        if (faultSpec.getFaultName().equals(FaultName.NETWORKFAULT.getValue())) {
            List<String> expectedCommandOutputList = new ArrayList<>();
            expectedCommandOutputList.add("socket is not established");
            expectedCommandOutputList.add("Injection output: Fault Injection Triggered");
            injectFaultCommandInfo = CommandInfo
                    .builder(String.format(FaultConstants.INFRA_AGENT_SUBMIT_COMMAND,
                            faultSpec.getInjectionHomeDir() + FaultConstants.INFRA_AGENT_NAME_FOLDER, injectionCommand,
                            faultSpec.getFaultName()))
                    .ignoreExitValueCheck(true)
                    .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultInjectionRequest())
                    .expectedCommandOutputList(expectedCommandOutputList).noOfRetries(3).retryInterval(2).build();
        } else {
            injectFaultCommandInfo = CommandInfo
                    .builder(String.format(FaultConstants.INFRA_AGENT_SUBMIT_COMMAND,
                            faultSpec.getInjectionHomeDir() + FaultConstants.INFRA_AGENT_NAME_FOLDER, injectionCommand,
                            faultSpec.getFaultName()))
                    .ignoreExitValueCheck(false)
                    .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultInjectionRequest())
                    .expectedCommandOutputList(Collections.emptyList()).noOfRetries(3).retryInterval(2).build();
        }
        List<CommandInfo> commandInfoList = new ArrayList<>();
        commandInfoList.add(systemResourceFaultUtils.getLinuxPythonAgentExtractCommandInfo(faultSpec));
        commandInfoList.add(systemResourceFaultUtils.getPythonAgentInstallCommandInfo(faultSpec));
        commandInfoList.add(injectFaultCommandInfo);
        return commandInfoList;

    }

    public List<CommandInfo> getRemediationcommandInfoList(CommandExecutionFaultSpec faultSpec) throws MangleException {
        if (!systemResourceFaultUtils.isManualRemediationSupported(faultSpec.getFaultName())) {
            return Collections.emptyList();
        }
        String remediationCommand =
                systemResourceFaultUtils.buildRemediationCommand(faultSpec.getArgs(), faultSpec.getInjectionHomeDir());
        List<CommandInfo> commandInfoList = new ArrayList<>();
        if (!StringUtils.isEmpty(remediationCommand)) {
            CommandInfo commandInfo = CommandInfo
                    .builder(String.format(FaultConstants.INFRA_AGENT_SUBMIT_COMMAND,
                            faultSpec.getInjectionHomeDir() + FaultConstants.INFRA_AGENT_NAME_FOLDER,
                            remediationCommand, faultSpec.getFaultName()))
                    .ignoreExitValueCheck(false).expectedCommandOutputList(Collections.emptyList())
                    .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultRemediationRequest())
                    .build();
            commandInfoList.add(commandInfo);
        }
        return commandInfoList;
    }

    public List<CommandInfo> getStatusCommandInfoList(CommandExecutionFaultSpec faultSpec) throws MangleException {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        String statusCommand = systemResourceFaultUtils.buildStatusCommand(faultSpec.getArgs());
        if (!StringUtils.isEmpty(statusCommand)) {
            CommandInfo commandInfo = CommandInfo
                    .builder(String.format(FaultConstants.INFRA_AGENT_SUBMIT_COMMAND,
                            faultSpec.getInjectionHomeDir() + FaultConstants.INFRA_AGENT_NAME_FOLDER, statusCommand,
                            faultSpec.getFaultName()))
                    .ignoreExitValueCheck(false).expectedCommandOutputList(Collections.emptyList())
                    .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultInjectionRequest())
                    .build();
            commandInfoList.add(commandInfo);
        }
        return commandInfoList;
    }

    public List<SupportScriptInfo> getFaultInjectionScripts(CommandExecutionFaultSpec faultSpec) {
        return systemResourceFaultUtils.getAgentFaultScriptsPython(faultSpec.getInjectionHomeDir());
    }

    public void checkTaskSpecificPrerequisites(CommandExecutionFaultSpec spec) throws MangleException {
        //No Specific Requirements
    }
}
