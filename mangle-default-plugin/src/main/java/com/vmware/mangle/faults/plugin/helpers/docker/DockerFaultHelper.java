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

package com.vmware.mangle.faults.plugin.helpers.docker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.faults.plugin.helpers.KnownFailuresHelper;
import com.vmware.mangle.services.enums.DockerFaultName;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.skeletons.ICommandExecutionFaultHelper;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.docker.DockerCommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author rpraveen
 *
 *         Implementation of {@link ICommandExecutionFaultHelper} to support Docker specific faults
 */

@Component
public class DockerFaultHelper implements ICommandExecutionFaultHelper {

    private EndpointClientFactory endpointClientFactory;

    @Autowired
    public DockerFaultHelper(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;
    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec dockerFaultSpec) throws MangleException {
        return new DockerCommandExecutor((CustomDockerClient) endpointClientFactory
                .getEndPointClient(dockerFaultSpec.getCredentials(), dockerFaultSpec.getEndpoint()));
    }

    @Override
    public List<CommandInfo> getInjectionCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec dockerFaultSpec) throws MangleException {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        CommandInfo dockerCmdInfo = new CommandInfo();
        dockerCmdInfo.setCommand(buildInjectionCommand((DockerFaultSpec) dockerFaultSpec));
        dockerCmdInfo.setIgnoreExitValueCheck(false);
        dockerCmdInfo.setExpectedCommandOutputList(Collections.emptyList());
        dockerCmdInfo.setKnownFailureMap(KnownFailuresHelper.getKnownFailureOfDockerFaultInjectionRequest());
        commandInfoList.add(dockerCmdInfo);

        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfo.setExtractedPropertyName("containerId");
        commandOutputProcessingInfo.setRegExpression("^.*$");
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        dockerCmdInfo.setCommandOutputProcessingInfoList(commandOutputProcessingInfoList);
        return commandInfoList;
    }

    @Override
    public List<CommandInfo> getRemediationCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec dockerFaultSpec) throws MangleException {
        CommandInfo remediationCommand = new CommandInfo();
        List<CommandInfo> commandInfoList = new ArrayList<>();
        remediationCommand.setCommand(buildRemediationsCommand((DockerFaultSpec) dockerFaultSpec));
        remediationCommand.setExpectedCommandOutputList(Collections.emptyList());
        remediationCommand.setKnownFailureMap(KnownFailuresHelper.getKnownFailureOfDockerFaultRemediationRequest());
        commandInfoList.add(remediationCommand);
        return commandInfoList;
    }

    private String buildRemediationsCommand(DockerFaultSpec dockerFaultSpec) throws MangleException {
        switch (DockerFaultName.valueOf(dockerFaultSpec.getDockerFaultName().toString().toUpperCase())) {
        case DOCKER_STOP:
            return DockerFaultName.DOCKER_START.name() + ":"
                    + CommonUtils.convertMaptoDelimitedString(dockerFaultSpec.getArgs(), " ")
                    + " --containerId $FI_ADD_INFO_containerId";
        case DOCKER_PAUSE:
            return DockerFaultName.DOCKER_UNPAUSE.name() + ":"
                    + CommonUtils.convertMaptoDelimitedString(dockerFaultSpec.getArgs(), " ");

        default:
            throw new MangleException(ErrorCode.UNSUPPORTED_FAULT, dockerFaultSpec.getDockerFaultName().toString());
        }
    }

    @Override
    public List<SupportScriptInfo> getAgentFaultInjectionScripts() {
        // Right now no Injection scripts required for this task
        return Collections.emptyList();
    }

    @Override
    public void checkTaskSpecificPrerequisites() throws MangleException {
        // Right not identified any task specific prerequisites to implement
    }

    /**
     * Method to construct injection command using ResoureType, Operation and Labels if specified
     *
     * @return {@link String} command
     * @throws MangleException
     */
    private String buildInjectionCommand(DockerFaultSpec dockerFaultSpec) {
        return dockerFaultSpec.getDockerFaultName() + ":"
                + CommonUtils.convertMaptoDelimitedString(dockerFaultSpec.getArgs(), " ");
    }

}
