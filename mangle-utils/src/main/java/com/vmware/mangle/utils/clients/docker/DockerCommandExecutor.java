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

package com.vmware.mangle.utils.clients.docker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.services.dto.OperationInputData;
import com.vmware.mangle.services.dto.OperationMetaData;
import com.vmware.mangle.utils.ICommandClientExecutor;
import com.vmware.mangle.utils.ReadFaultOperationProperties;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author rpraveen
 *
 *
 */
@Log4j2
public class DockerCommandExecutor implements ICommandClientExecutor {
    CustomDockerClient dockerClient;
    Map<String, OperationMetaData> dockerFaultMap;

    public DockerCommandExecutor(CustomDockerClient customDockerClient) {
        this.dockerClient = customDockerClient;
        this.dockerFaultMap = ReadFaultOperationProperties.getDockerFaultOperationMap();
    }

    @Override
    public CommandExecutionResult executeCommand(String command) {
        CommandExecutionResult result = new CommandExecutionResult();
        try {
            OperationInputData inputData = extractOperationAndParamValues(command);
            result = callOperation(dockerFaultMap.get(inputData.getOperationName()), inputData.getParamValues());
        } catch (MangleException e) {
            result.setExitCode(1);
            result.setCommandOutput("[" + command + "]execution failed with exception " + e.getMessage());
        }
        log.trace("Executed command: " + command + "Command execution Result: " + result);
        return result;
    }

    @Override
    public Object[] getOperationParamValues(String[] inputParamValues) {
        List<Object> valueList = new ArrayList<>();
        valueList.add(dockerClient);
        valueList.addAll(Arrays.asList(inputParamValues));
        return valueList.toArray(new Object[0]);
    }
}
