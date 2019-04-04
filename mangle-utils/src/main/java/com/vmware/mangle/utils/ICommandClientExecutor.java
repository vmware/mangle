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

package com.vmware.mangle.utils;


import java.lang.reflect.InvocationTargetException;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.services.dto.OperationInputData;
import com.vmware.mangle.services.dto.OperationMetaData;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam
 */
public interface ICommandClientExecutor extends ICommandExecutor {

    public Object[] getOperationParamValues(String[] inputParamValues);

    default OperationInputData extractOperationAndParamValues(String command) {
        String[] commandArray = command.split(":");
        OperationInputData inputData = new OperationInputData();
        inputData.setOperationName(commandArray[0]);
        inputData.setParamValues(
                this.getOperationParamValues(CommonUtils.getValuesFromCommandArgsString(commandArray[1], null)));
        return inputData;
    }

    default CommandExecutionResult callOperation(OperationMetaData operationMetadata, Object[] paramValues)
            throws MangleException {
        try {
            return (CommandExecutionResult) Class.forName(operationMetadata.getClassName())
                    .getMethod(operationMetadata.getMethodName(),
                            operationMetadata.getMethodArgTypes().toArray(new Class[0]))
                    .invoke(null, paramValues);
        } catch (InvocationTargetException e) {
            if (MangleException.class.isAssignableFrom(e.getCause().getClass())) {
                throw (MangleException) e.getCause();
            } else {
                throw new MangleException(e.getMessage(), ErrorCode.GENERIC_ERROR);
            }
        } catch (IllegalAccessException | ClassNotFoundException | IllegalArgumentException | NoSuchMethodException e) {
            throw new MangleException(e.getMessage(), ErrorCode.GENERIC_ERROR);
        }
    }
}
