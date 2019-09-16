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

package com.vmware.mangle.services.helpers;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.vmware.mangle.cassandra.model.custom.faults.CustomFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.FaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.PluginFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.services.PluginDetailsService;
import com.vmware.mangle.services.PluginService;
import com.vmware.mangle.task.framework.helpers.AbstractTaskHelper;
import com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault;
import com.vmware.mangle.utils.constants.FaultConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Custom FaultInjection Helper class.
 *
 * @author kumargautam
 */
@SuppressWarnings("deprecation")
@Service
@Validated
public class CustomFaultInjectionHelper {
    @Autowired
    private PluginService pluginService;
    @Autowired
    private PluginDetailsService pluginDetailsService;
    @Autowired
    private FaultInjectionHelper faultInjectionHelper;
    @Setter
    private ObjectMapper mapper;

    /**
     * @param faultSpec
     * @throws MangleException
     */
    public AbstractCustomFault getCustomFault(CustomFaultSpec customFaultSpec) throws MangleException {
        if (!pluginDetailsService.isPluginAvailable(customFaultSpec.getPluginId())) {
            throw new MangleException(ErrorCode.CUSTOM_FAULT_EXECUTION_FAILURE_DUE_TO_PLUGIN_STATE,
                    customFaultSpec.getPluginId());
        }
        return pluginService
                .getExtensionForCustomFault((String) customFaultSpec.getExtensionDetails().getFaultExtensionName());
    }

    public PluginFaultSpec getFaultSpec(CustomFaultSpec customFaultSpec, AbstractCustomFault abstractCustomFault) {
        Object jsonObject = customFaultSpec.getFaultParameters();
        PluginFaultSpec faultSpec =
                (PluginFaultSpec) getMapper().convertValue(jsonObject, abstractCustomFault.getModelClass());
        Map<String, Object> map = new HashMap<>();
        map.put(FaultConstants.FAULT_PARAMETERS_KEY, faultSpec);
        pluginDetailsService.updatePluginInformationInFaultSpec(customFaultSpec, faultSpec);
        faultSpec.setEndpointName(customFaultSpec.getEndpointName());
        faultSpec.setSchedule(customFaultSpec.getSchedule());
        faultSpec.setTags(customFaultSpec.getTags());
        faultSpec.setDockerArguments(customFaultSpec.getDockerArguments());
        faultSpec.setK8sArguments(customFaultSpec.getK8sArguments());
        faultSpec.setFaultParameters(customFaultSpec.getFaultParameters());
        return faultSpec;
    }

    public void validateFields(@Valid PluginFaultSpec requestJson, AbstractCustomFault abstractCustomFault)
            throws MangleException {
        faultInjectionHelper.validateSpec(requestJson);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(requestJson);
        abstractCustomFault.init(requestJson);
    }

    /**
     * Method to call triggetTask method in FaultTaskFactory class
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    public Task<TaskSpec> invokeFault(PluginFaultSpec faultSpec, String taskExtensionName) throws MangleException {
        Task<? extends TaskSpec> task = getTask(faultSpec, taskExtensionName);
        return (Task<TaskSpec>) task;
    }

    public Task<? extends TaskSpec> getTask(FaultSpec faultSpec, String taskExtensionName) throws MangleException {
        Task<? extends TaskSpec> task = getTask(faultSpec, null, taskExtensionName);
        faultInjectionHelper.saveTask(task);
        return task;
    }

    @SuppressWarnings("unchecked")
    public Task<TaskSpec> getTask(FaultSpec faultSpec, String taskId, String taskExtensionName) throws MangleException {
        AbstractTaskHelper<TaskSpec> taskHelper =
                (AbstractTaskHelper<TaskSpec>) pluginService.getExtension(taskExtensionName);
        if (taskHelper == null) {
            throw new MangleException(ErrorCode.EXTENSION_NOT_FOUND, taskExtensionName);
        }
        return taskHelper.init(faultSpec, taskId);
    }

    /**
     * @return ObjectMapper
     */
    public ObjectMapper getMapper() {
        if (mapper == null) {
            this.mapper = new ObjectMapper();
        }
        return this.mapper;
    }
}