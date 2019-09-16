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

package com.vmware.mangle.task.framework.utils;

import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author bkaranam Utility class to execute commands on Docker host using dockerClient
 */

@Log4j2
public class DockerCommandUtils implements ICommandExecutor {
    private CustomDockerClient dockerClient;
    private CommandExecutionFaultSpec faultSpec;


    public DockerCommandUtils(CommandExecutionFaultSpec jvmAgentFaultSpec,
            EndpointClientFactory endpointClientFactory) {
        this.faultSpec = jvmAgentFaultSpec;
        this.dockerClient = (CustomDockerClient) endpointClientFactory
                .getEndPointClient(jvmAgentFaultSpec.getCredentials(), jvmAgentFaultSpec.getEndpoint());
    }

    public CommandExecutionResult runCommand(String command) throws MangleException {
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        commandExecutionResult = this.dockerClient
                .execCommandInContainerByName(faultSpec.getDockerArguments().getContainerName(), command);
        log.debug("Executing Command: " + commandExecutionResult.getCommandOutput());
        return commandExecutionResult;
    }


    public CommandExecutionResult executeCommand(String command) {
        try {
            return this.runCommand(command);
        } catch (MangleException e) {
            CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
            return commandExecutionResult;
        }
    }

}