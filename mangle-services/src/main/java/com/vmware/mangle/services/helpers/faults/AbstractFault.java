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

package com.vmware.mangle.services.helpers.faults;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import org.springframework.core.annotation.AnnotationUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.helpers.FaultInjectionHelper;
import com.vmware.mangle.task.framework.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Abstract class for any Mangle Fault
 *
 * @author bkaranam
 */
@Data
public abstract class AbstractFault {
    protected CommandExecutionFaultSpec faultSpec;
    TaskType taskType;


    protected AbstractFault(CommandExecutionFaultSpec faultSpec, TaskType taskType) throws MangleException {
        this.faultSpec = faultSpec;
        this.taskType = taskType;
        validateForEndpointSupport();
        if (faultSpec.getArgs() == null) {
            faultSpec.setArgs(new HashMap<>());
        }

        faultSpec.getArgs().putAll(getFaultSpecificArgs());
    }

    protected abstract Map<String, String> getFaultSpecificArgs();

    public boolean validateForEndpointSupport() throws MangleException {
        if (getTaskType().equals(TaskType.INJECTION)) {
            List<EndpointType> supportedEndpointTypes = Arrays
                    .asList(AnnotationUtils.findAnnotation(this.getClass(), SupportedEndpoints.class).endPoints());
            if (!supportedEndpointTypes.contains(getFaultSpec().getEndpoint().getEndPointType())
                    && (null == getFaultSpec().getEndpoint().getEndpointGroupType() || !supportedEndpointTypes.contains(
                            EndpointType.valueOf(getFaultSpec().getEndpoint().getEndpointGroupType().name())))) {
                throw new MangleException(ErrorCode.UNSUPPORTED_ENDPOINT, getFaultSpec().getClass().getSimpleName(),
                        supportedEndpointTypes);
            }
        }
        return true;
    }

    /**
     * Method to call triggetTask method in FaultTaskFactory class
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    public Task<TaskSpec> invokeFault(FaultInjectionHelper faultInjectionHelper) throws MangleException {
        Task<? extends TaskSpec> task = faultInjectionHelper.getTask(getFaultSpec());
        return (Task<TaskSpec>) task;
    }

}
