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

package com.vmware.mangle.task.framework.helpers.faults;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import org.springframework.core.annotation.AnnotationUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.PluginFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.task.framework.plugin.context.FIExtensionPoint;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Abstract class for any Mangle Custom Fault
 *
 * @author kumargautam
 */
@Getter
public abstract class AbstractCustomFault implements FIExtensionPoint {
    protected PluginFaultSpec faultSpec;
    private TaskType taskType;

    protected void init(PluginFaultSpec faultSpec, TaskType taskType) throws MangleException {
        this.faultSpec = faultSpec;
        this.taskType = taskType;
        validateForEndpointSupport();
        if (faultSpec.getArgs() == null) {
            faultSpec.setArgs(new HashMap<>());
        }

        faultSpec.getArgs().putAll(getFaultSpecificArgs());
    }

    /**
     * Provide Fault Specific Args, which will used during fault injection.
     *
     * @return
     */
    protected abstract Map<String, String> getFaultSpecificArgs();

    /**
     * Provide Model class as faultSpec and {@link TaskType.INJECTION}, and call
     * {@link super.init(.,.)}
     *
     * @param faultSpec
     * @throws MangleException
     */
    public abstract void init(PluginFaultSpec faultSpec) throws MangleException;

    /**
     * Provide Model class extension.
     *
     * @return
     */
    public abstract Class<? extends CommandExecutionFaultSpec> getModelClass();

    public boolean validateForEndpointSupport() throws MangleException {
        if (getTaskType().equals(TaskType.INJECTION)) {
            List<EndpointType> supportedEndpointTypes = Arrays
                    .asList(AnnotationUtils.findAnnotation(this.getClass(), SupportedEndpoints.class).endPoints());
            if (!supportedEndpointTypes.contains(getFaultSpec().getEndpoint().getEndPointType())) {
                throw new MangleException(ErrorCode.UNSUPPORTED_ENDPOINT, getFaultSpec().getClass().getSimpleName(),
                        supportedEndpointTypes);
            }
        }
        return true;
    }
}