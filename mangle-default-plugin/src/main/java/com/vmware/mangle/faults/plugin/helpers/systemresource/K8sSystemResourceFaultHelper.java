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

import static com.vmware.mangle.utils.constants.FaultConstants.INFRA_AGENT_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.FaultConstants;
import com.vmware.mangle.faults.plugin.helpers.KnownFailuresHelper;
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

    @Autowired
    public K8sSystemResourceFaultHelper(EndpointClientFactory endpointClientFactory,
            SystemResourceFaultUtils systemResourceFaultUtils) {
        super(systemResourceFaultUtils);
        this.endpointClientFactory = endpointClientFactory;
    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec faultSpec) throws MangleException {
        return (KubernetesCommandLineClient) endpointClientFactory.getEndPointClient(faultSpec.getCredentials(),
                faultSpec.getEndpoint());
    }

    @Override
    public List<CommandInfo> getInjectionCommandInfoList(CommandExecutionFaultSpec faultSpec) throws MangleException {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        createK8SCopyCommandInfoList(commandInfoList, faultSpec);
        createExecutableCommandInfoList(commandInfoList, faultSpec);
        CommandInfo agentExtractCommandInfo = CommandInfo
                .builder(String.format(Constants.KUBE_FAULT_EXEC_STRING, faultSpec.getK8sArguments().getPodInAction(),
                        faultSpec.getK8sArguments().getContainerName(), Constants.SH_COMMAND_PREFIX)
                        + String.format("-c 'tar -zxvf %s/%s -C %s'", faultSpec.getInjectionHomeDir(), INFRA_AGENT_NAME,
                                faultSpec.getInjectionHomeDir()))
                .ignoreExitValueCheck(false).expectedCommandOutputList(Arrays.asList("")).build();
        commandInfoList.add(agentExtractCommandInfo);
        CommandInfo agentStartCommandInfo = CommandInfo
                .builder(
                        String.format(Constants.KUBE_FAULT_EXEC_STRING, faultSpec.getK8sArguments().getPodInAction(),
                                faultSpec.getK8sArguments().getContainerName(),
                                "nohup " + Constants.SH_COMMAND_PREFIX + Constants.K8S_SHELL_COMMAND_ARGUMENT
                                        + systemResourceFaultUtils.getPythonAgentInstallCommandInfo(faultSpec)
                                                .getCommand()
                                        + "\""))
                .ignoreExitValueCheck(false)
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentInstallationRequest())
                .expectedCommandOutputList(Arrays.asList("")).build();
        commandInfoList.add(agentStartCommandInfo);
        String injectionCommand =
                systemResourceFaultUtils.buildInjectionCommand(faultSpec.getArgs(), faultSpec.getInjectionHomeDir());
        CommandInfo injectFaultCommandInfo = CommandInfo
                .builder(String.format(Constants.KUBE_FAULT_EXEC_STRING, faultSpec.getK8sArguments().getPodInAction(),
                        faultSpec.getK8sArguments().getContainerName(),
                        Constants.SH_COMMAND_PREFIX + Constants.K8S_SHELL_COMMAND_ARGUMENT
                                + String.format(FaultConstants.INFRA_AGENT_SUBMIT_COMMAND,
                                        faultSpec.getInjectionHomeDir() + FaultConstants.INFRA_AGENT_NAME_FOLDER,
                                        injectionCommand, faultSpec.getFaultName())
                                + "\""))
                .ignoreExitValueCheck(false)
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultInjectionRequest())
                .expectedCommandOutputList(Arrays.asList("")).noOfRetries(3).retryInterval(2).build();
        commandInfoList.add(injectFaultCommandInfo);

        return commandInfoList;
    }

    private void createK8SCopyCommandInfoList(List<CommandInfo> commandInfoList, CommandExecutionFaultSpec faultSpec) {
        CommandInfo k8sCopyCommandInfo = CommandInfo
                .builder("cp " + ConstantsUtils.getMangleSupportScriptDirectory() + FaultConstants.INFRA_AGENT_NAME
                        + " " + faultSpec.getK8sArguments().getPodInAction() + ":" + faultSpec.getInjectionHomeDir()
                        + FaultConstants.INFRA_AGENT_NAME + " -c " + faultSpec.getK8sArguments().getContainerName())
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceK8SCopyRequest()).build();
        commandInfoList.add(k8sCopyCommandInfo);
    }

    private void createExecutableCommandInfoList(List<CommandInfo> commandInfoList,
            CommandExecutionFaultSpec faultSpec) {
        CommandInfo setExecutableCommandInfo = CommandInfo
                .builder(String.format(Constants.KUBE_FAULT_EXEC_STRING, faultSpec.getK8sArguments().getPodInAction(),
                        faultSpec.getK8sArguments().getContainerName(),
                        String.format(Constants.LINUX_COMMAND_FOR_ASSIGN_EXECUTE_PERMISSION_RECURSIVELY,
                                faultSpec.getInjectionHomeDir()) + FaultConstants.INFRA_AGENT_NAME))
                .ignoreExitValueCheck(false).expectedCommandOutputList(null).build();
        commandInfoList.add(setExecutableCommandInfo);
    }

    @Override
    public List<CommandInfo> getRemediationcommandInfoList(CommandExecutionFaultSpec faultSpec) throws MangleException {
        if (!systemResourceFaultUtils.isManualRemediationSupported(faultSpec.getFaultName())) {
            return Collections.emptyList();
        }
        String remediationCommand =
                systemResourceFaultUtils.buildRemediationCommand(faultSpec.getArgs(), faultSpec.getInjectionHomeDir());
        List<CommandInfo> commandInfoList = new ArrayList<>();
        if (!StringUtils.isEmpty(remediationCommand)) {
            CommandInfo commandInfo = CommandInfo
                    .builder(String.format(Constants.KUBE_FAULT_EXEC_STRING,
                            faultSpec.getK8sArguments().getPodInAction(),
                            faultSpec.getK8sArguments().getContainerName(),
                            Constants.SH_COMMAND_PREFIX + Constants.K8S_SHELL_COMMAND_ARGUMENT
                                    + String.format(FaultConstants.INFRA_AGENT_SUBMIT_COMMAND,
                                            faultSpec.getInjectionHomeDir() + FaultConstants.INFRA_AGENT_NAME_FOLDER,
                                            remediationCommand, faultSpec.getFaultName())
                                    + "\""))
                    .ignoreExitValueCheck(false).expectedCommandOutputList(Collections.emptyList())
                    .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultRemediationRequest())
                    .build();
            commandInfoList.add(commandInfo);
        }
        return commandInfoList;
    }

    @Override
    public List<CommandInfo> getStatusCommandInfoList(CommandExecutionFaultSpec faultSpec) throws MangleException {
        String statusCommand = systemResourceFaultUtils.buildStatusCommand(faultSpec.getArgs());
        List<CommandInfo> commandInfoList = new ArrayList<>();
        if (!StringUtils.isEmpty(statusCommand)) {
            CommandInfo commandInfo = CommandInfo
                    .builder(String.format(Constants.KUBE_FAULT_EXEC_STRING,
                            faultSpec.getK8sArguments().getPodInAction(),
                            faultSpec.getK8sArguments().getContainerName(),
                            Constants.SH_COMMAND_PREFIX + Constants.K8S_SHELL_COMMAND_ARGUMENT
                                    + String.format(FaultConstants.INFRA_AGENT_SUBMIT_COMMAND,
                                            faultSpec.getInjectionHomeDir() + FaultConstants.INFRA_AGENT_NAME_FOLDER,
                                            statusCommand, faultSpec.getFaultName())
                                    + "\""))
                    .ignoreExitValueCheck(false).expectedCommandOutputList(Collections.emptyList())
                    .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultInjectionRequest())
                    .build();
            commandInfoList.add(commandInfo);
        }
        return commandInfoList;
    }
}