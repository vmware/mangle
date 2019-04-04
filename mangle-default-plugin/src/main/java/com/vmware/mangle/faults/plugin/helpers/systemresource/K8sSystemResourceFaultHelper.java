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

import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.EXPECTED_REMEDIATION_MESSAGE_FOR_FILE_NOT_FOUND;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.MESSAGE_THROWN_FOR_EXPECTED_REMEDIATION_FAILURE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.services.enums.FaultName;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author hkilari
 *
 */
public class K8sSystemResourceFaultHelper extends SystemResourceFaultHelper {
    private EndpointClientFactory endpointClientFactory;
    private SystemResourceFaultUtils systemResourceFaultUtils;

    @Autowired
    public K8sSystemResourceFaultHelper(EndpointClientFactory endpointClientFactory,
            SystemResourceFaultUtils systemResourceFaultUtils) {
        super();
        this.endpointClientFactory = endpointClientFactory;
        this.systemResourceFaultUtils = systemResourceFaultUtils;
    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec faultSpec) throws MangleException {
        return (KubernetesCommandLineClient) endpointClientFactory.getEndPointClient(faultSpec.getCredentials(),
                faultSpec.getEndpoint());
    }

    @Override
    public List<CommandInfo> getInjectionCommandInfoList(CommandExecutionFaultSpec faultSpec) throws MangleException {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        CommandInfo injectFaultCommandInfo = new CommandInfo();
        FaultName faultName = systemResourceFaultUtils.getFaultName(faultSpec.getArgs());
        createK8SCopyCommandInfoList(commandInfoList, faultName, faultSpec);
        createExecutableCommandInfoList(commandInfoList, faultName, faultSpec);

        injectFaultCommandInfo.setCommand(String.format(Constants.KUBE_FAULT_EXEC_STRING,
                faultSpec.getK8sArguments().getPodInAction(), faultSpec.getK8sArguments().getContainerName(),
                Constants.SH_COMMAND_PREFIX + systemResourceFaultUtils.buildInjectionCommand(faultSpec.getArgs(),
                        faultSpec.getInjectionHomeDir())));
        injectFaultCommandInfo.setIgnoreExitValueCheck(false);
        injectFaultCommandInfo.setExpectedCommandOutputList(null);

        commandInfoList.add(injectFaultCommandInfo);
        return commandInfoList;
    }

    private void createK8SCopyCommandInfoList(List<CommandInfo> commandInfoList, FaultName faultName,
            CommandExecutionFaultSpec faultSpec) {
        String scriptFileName = systemResourceFaultUtils.getScriptNameforFault(faultName);
        CommandInfo k8sCopyCommandInfo = new CommandInfo();
        k8sCopyCommandInfo.setCommand("cp " + ConstantsUtils.getMangleSupportScriptDirectory() + scriptFileName + " "
                + faultSpec.getK8sArguments().getPodInAction() + ":" + faultSpec.getInjectionHomeDir() + scriptFileName
                + " -c " + faultSpec.getK8sArguments().getContainerName());
        commandInfoList.add(k8sCopyCommandInfo);
    }

    private void createExecutableCommandInfoList(List<CommandInfo> commandInfoList, FaultName faultName,
            CommandExecutionFaultSpec faultSpec) {
        String scriptFileName = systemResourceFaultUtils.getScriptNameforFault(faultName);
        CommandInfo setExecutableCommandInfo = new CommandInfo();
        setExecutableCommandInfo.setCommand(String.format(Constants.KUBE_FAULT_EXEC_STRING,
                faultSpec.getK8sArguments().getPodInAction(), faultSpec.getK8sArguments().getContainerName(),
                String.format(Constants.LINUX_COMMAND_FOR_ASSIGN_EXECUTE_PERMISSION_RECURSIVELY,
                        faultSpec.getInjectionHomeDir()) + scriptFileName));
        setExecutableCommandInfo.setIgnoreExitValueCheck(false);
        setExecutableCommandInfo.setExpectedCommandOutputList(null);
        commandInfoList.add(setExecutableCommandInfo);
    }

    @SuppressWarnings("serial")
    @Override
    public List<CommandInfo> getRemediationcommandInfoList(CommandExecutionFaultSpec faultSpec) throws MangleException {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        CommandInfo commandInfo = new CommandInfo();
        commandInfo.setCommand(String.format(Constants.KUBE_FAULT_EXEC_STRING,
                faultSpec.getK8sArguments().getPodInAction(), faultSpec.getK8sArguments().getContainerName(),
                Constants.SH_COMMAND_PREFIX + systemResourceFaultUtils.buildRemediationCommand(faultSpec.getArgs(),
                        faultSpec.getInjectionHomeDir())));
        commandInfo.setIgnoreExitValueCheck(false);
        commandInfo.setExpectedCommandOutputList(Collections.emptyList());
        Map<String, String> knownFailureMap = new HashMap<>();
        knownFailureMap.put(EXPECTED_REMEDIATION_MESSAGE_FOR_FILE_NOT_FOUND,
                MESSAGE_THROWN_FOR_EXPECTED_REMEDIATION_FAILURE);
        commandInfo.setKnownFailureMap(knownFailureMap);
        commandInfoList.add(commandInfo);
        return commandInfoList;
    }

    @Override
    public List<SupportScriptInfo> getFaultInjectionScripts(CommandExecutionFaultSpec faultSpec) {
        return systemResourceFaultUtils.getAgentFaultScripts(faultSpec);
    }

    @Override
    public void checkTaskSpecificPrerequisites(CommandExecutionFaultSpec spec) throws MangleException {
        //No Specific Requirements
    }
}
