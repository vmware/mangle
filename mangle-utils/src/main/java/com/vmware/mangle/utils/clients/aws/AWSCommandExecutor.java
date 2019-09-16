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

package com.vmware.mangle.utils.clients.aws;

import java.util.ArrayList;
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
 * @author bkaranam
 *
 *         Delegate the command execution and constructs the parameters that are to be used in
 *         calling the function using the reflection
 */
@Log4j2
public class AWSCommandExecutor implements ICommandClientExecutor {
    CustomAwsClient awsClient;
    Map<String, OperationMetaData> awsEC2FaultMap;

    public AWSCommandExecutor(CustomAwsClient awsClient) {
        this.awsClient = awsClient;
        this.awsEC2FaultMap = ReadFaultOperationProperties.getAwsEC2FaultOperationMap();
    }

    @Override
    public CommandExecutionResult executeCommand(String command) {
        CommandExecutionResult returnObject = new CommandExecutionResult();
        OperationInputData inputData = extractOperationAndParamValues(command);
        try {
            returnObject = callOperation(awsEC2FaultMap.get(inputData.getOperationName()), inputData.getParamValues());
        } catch (MangleException e) {
            returnObject.setCommandOutput(e.getMessage());
            returnObject.setExitCode(1);
        }
        log.trace("Executed command: " + command + "Command execution Result: " + returnObject);
        return returnObject;
    }

    @Override
    public Object[] getOperationParamValues(String[] inputParamValues) {
        List<Object> valueList = new ArrayList<>();
        valueList.add(awsClient);
        for (String inputParamValue : inputParamValues) {
            valueList.add(inputParamValue.split(","));
        }
        return valueList.toArray(new Object[0]);
    }
}
