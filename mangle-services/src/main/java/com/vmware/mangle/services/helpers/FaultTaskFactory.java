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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.EndpointGroupFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.FaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VCenterFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.RemediableTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2FaultSpec;
import com.vmware.mangle.model.aws.faults.spec.AwsRDSFaultSpec;
import com.vmware.mangle.model.azure.faults.spec.AzureVMFaultSpec;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.model.vcenter.specs.HostFaultSpec;
import com.vmware.mangle.services.EndpointService;
import com.vmware.mangle.services.PluginService;
import com.vmware.mangle.services.enums.FaultName;
import com.vmware.mangle.task.framework.helpers.AbstractTaskHelper;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam
 *
 *         Factory class to trigger fault task which is of AbstractCommandExecutionTaskHelper If
 *         there is specific way to trigger fault task on endpoint, it takes endpoint as an optional
 *         argument. ex: K8S_Cluster as we may need to trigger same type of task on more than
 *         resource (pod)
 */
@Component
@SuppressWarnings({ "unchecked" })
public class FaultTaskFactory {

    @Autowired
    private PluginService pluginService;

    @Autowired
    private EndpointService endpointService;

    public Task<TaskSpec> getTask(FaultSpec faultSpec) throws MangleException {
        return getTask(faultSpec, null);
    }

    /**
     * Method to trigger any fault task example: BytemanTask If there is specific way to trigger fault
     * task on endpoint, it takes endpoint as an argument ex: K8S_Cluster as we may need to trigger same
     * type of task on more than resource (pod)
     */
    public Task<TaskSpec> getTask(FaultSpec faultSpec, String taskId) throws MangleException {
        AbstractTaskHelper<TaskSpec> taskHelper = null;

        if (isEndpointGroupTriggerTaskFault(faultSpec)) {
            EndpointGroupFaultTriggerSpec nodeGroupFaultSpec = new EndpointGroupFaultTriggerSpec();
            nodeGroupFaultSpec.setChildSpecType(faultSpec.getClass().getName());
            nodeGroupFaultSpec.setFaultSpec((CommandExecutionFaultSpec) faultSpec);
            nodeGroupFaultSpec.setSchedule(faultSpec.getSchedule());
            taskHelper =
                    getExtension("com.vmware.mangle.faults.plugin.tasks.helpers.EndpointGroupFaultTriggerTaskHelper");
            return taskHelper.init(nodeGroupFaultSpec, taskId);
        }

        if (faultSpec instanceof K8SFaultSpec) {
            taskHelper = getExtension("com.vmware.mangle.faults.plugin.tasks.helpers.K8sSpecificFaultTaskHelper");
            return taskHelper.init(faultSpec, taskId);
        }

        if (faultSpec instanceof DockerFaultSpec) {
            taskHelper = getExtension("com.vmware.mangle.faults.plugin.tasks.helpers.DockerSpecificFaultTaskHelper");
            return taskHelper.init(faultSpec, taskId);
        }

        if (faultSpec instanceof AwsEC2FaultSpec) {
            taskHelper = getExtension("com.vmware.mangle.faults.plugin.tasks.helpers.AwsEC2SpecificFaultTaskHelper");
            return taskHelper.init(faultSpec, taskId);
        }

        if (faultSpec instanceof AwsRDSFaultSpec) {
            taskHelper = getExtension("com.vmware.mangle.faults.plugin.tasks.helpers.AwsRDSFaultTaskHelper");
            return taskHelper.init(faultSpec, taskId);
        }

        if (faultSpec instanceof AzureVMFaultSpec) {
            taskHelper = getExtension("com.vmware.mangle.faults.plugin.tasks.helpers.AzureVMSpecificFaultTaskHelper");
            return taskHelper.init(faultSpec, taskId);
        }

        if (faultSpec instanceof RedisFaultSpec) {
            taskHelper = getExtension("com.vmware.mangle.faults.plugin.tasks.helpers.RedisFaultTaskHelper");
            return taskHelper.init(faultSpec, taskId);
        }


        if (isVCenterFaultTriggerTaskFault(faultSpec)) {
            VCenterFaultTriggerSpec vCenterFaultTriggerSpec = new VCenterFaultTriggerSpec();
            vCenterFaultTriggerSpec.setChildSpecType(faultSpec.getClass().getName());
            vCenterFaultTriggerSpec.setFaultSpec((CommandExecutionFaultSpec) faultSpec);
            if (faultSpec instanceof VMFaultSpec) {
                vCenterFaultTriggerSpec.setEnableRandomInjection(((VMFaultSpec) faultSpec).isEnableRandomInjection());
            } else if (faultSpec instanceof HostFaultSpec) {
                vCenterFaultTriggerSpec.setEnableRandomInjection(((HostFaultSpec) faultSpec).isEnableRandomInjection());
            }

            taskHelper =
                    getExtension("com.vmware.mangle.faults.plugin.tasks.helpers.VCenterSpecificFaultTriggerTaskHelper");
            return taskHelper.init(vCenterFaultTriggerSpec, taskId);
        }

        if (isK8SFaultTriggerTaskFault(faultSpec)) {
            K8SFaultTriggerSpec k8SFaultSpec = new K8SFaultTriggerSpec();
            k8SFaultSpec.setChildSpecType(faultSpec.getClass().getName());
            k8SFaultSpec.setFaultSpec((CommandExecutionFaultSpec) faultSpec);
            k8SFaultSpec.setSchedule(faultSpec.getSchedule());
            taskHelper = getExtension("com.vmware.mangle.faults.plugin.tasks.helpers.K8SFaultTriggerTaskHelper");
            return taskHelper.init(k8SFaultSpec, taskId);

        }

        if (faultSpec instanceof JVMAgentFaultSpec && null != ((JVMAgentFaultSpec) faultSpec).getJvmProperties()
                && null != ((JVMAgentFaultSpec) faultSpec).getJvmProperties().getJvmprocess()) {
            taskHelper = getExtension("com.vmware.mangle.faults.plugin.tasks.helpers.BytemanFaultTaskHelper");
            return taskHelper.init(faultSpec, taskId);
        } else {
            taskHelper = getExtension("com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper2");
            return taskHelper.init(faultSpec, taskId);
        }
    }

    private AbstractTaskHelper<TaskSpec> getExtension(String extensionName) {
        return (AbstractTaskHelper<TaskSpec>) pluginService.getExtension(extensionName);
    }

    private boolean isK8SFaultTriggerTaskFault(FaultSpec faultSpecParam) {
        CommandExecutionFaultSpec faultSpec = (CommandExecutionFaultSpec) faultSpecParam;
        return faultSpec.getEndpoint() != null
                && faultSpec.getEndpoint().getEndPointType().equals(EndpointType.K8S_CLUSTER)
                && null != faultSpec.getK8sArguments() && null == faultSpec.getK8sArguments().getPodInAction();
    }

    private boolean isVCenterFaultTriggerTaskFault(FaultSpec faultSpecParam) {
        CommandExecutionFaultSpec faultSpec = (CommandExecutionFaultSpec) faultSpecParam;
        return (faultSpec instanceof VMFaultSpec || faultSpec instanceof HostFaultSpec)
                && faultSpec.getEndpoint() != null
                && faultSpec.getEndpoint().getEndPointType().equals(EndpointType.VCENTER);
    }

    private boolean isEndpointGroupTriggerTaskFault(FaultSpec faultSpecParam) {
        CommandExecutionFaultSpec faultSpec = (CommandExecutionFaultSpec) faultSpecParam;
        return faultSpec.getEndpoint() != null
                && faultSpec.getEndpoint().getEndPointType().equals(EndpointType.ENDPOINT_GROUP);
    }

    public Task<TaskSpec> getRemediationTask(Task<TaskSpec> injectedTask, String taskId) throws MangleException {
        checkPreConditionOnRemediationRequest(injectedTask);
        FaultSpec faultSpec;

        if (injectedTask.getTaskData() instanceof K8SFaultTriggerSpec) {
            faultSpec = ((K8SFaultTriggerSpec) injectedTask.getTaskData()).getFaultSpec();
        } else if (injectedTask.getTaskData() instanceof EndpointGroupFaultTriggerSpec) {
            faultSpec = ((EndpointGroupFaultTriggerSpec) injectedTask.getTaskData()).getFaultSpec();
        } else {
            faultSpec = (FaultSpec) injectedTask.getTaskData();
        }

        return getTask(injectedTask, faultSpec, taskId);
    }

    /**
     * Verify if the task can be remediated
     *
     * @param injectedTask
     * @throws MangleException
     *             if the task is not remediable
     */
    private void checkPreConditionOnRemediationRequest(Task<?> injectedTask) throws MangleException {
        if (injectedTask == null) {
            throw new MangleException(ErrorCode.NO_TASK_FOUND);
        }
        if (!(TaskType.INJECTION.equals(injectedTask.getTaskType()))) {
            throw new MangleException(ErrorCode.NOT_A_INJECTION_TASK);
        }
        if (injectedTask.getTaskStatus() != TaskStatus.COMPLETED
                && injectedTask.getTaskStatus() != TaskStatus.INJECTED) {
            throw new MangleException(ErrorCode.INVALID_STATE_FOR_REMEDIATION);
        }
        if (((RemediableTask<TaskSpec>) injectedTask).isRemediated()) {
            throw new MangleException(ErrorCode.FAULT_ALREADY_REMEDIATED);
        }
        if (injectedTask.getTaskData() instanceof CommandExecutionFaultSpec
                && ((CommandExecutionFaultSpec) injectedTask.getTaskData()).getRemediationCommandInfoList().isEmpty()) {
            if (FaultName.KERNELPANICFAULT.getValue()
                    .equals(((CommandExecutionFaultSpec) injectedTask.getTaskData()).getFaultName())) {
                throw new MangleException(
                        String.format(ErrorConstants.FAULT_REMEDIATION_NOT_SUPPORTED_FOR_KERNELPANICFAULT,
                                ((CommandExecutionFaultSpec) injectedTask.getTaskData()).getFaultName()),
                        ErrorCode.FAULT_REMEDIATION_NOT_SUPPORTED);
            } else {
                throw new MangleException(
                        String.format(ErrorConstants.FAULT_REMEDIATION_NOT_SUPPORTED,
                                ((CommandExecutionFaultSpec) injectedTask.getTaskData()).getFaultName()),
                        ErrorCode.FAULT_REMEDIATION_NOT_SUPPORTED);
            }
        }
    }

    /**
     * @param injectedTask
     * @param faultSpec
     * @param taskId
     * @return
     * @throws MangleException
     */
    private Task<TaskSpec> getTask(Task<TaskSpec> injectedTask, FaultSpec faultSpec, String taskId)
            throws MangleException {
        AbstractTaskHelper<TaskSpec> taskHelper = getExtension(injectedTask.getExtensionName());

        if (isK8SFaultTriggerTaskFault(faultSpec)) {
            K8SFaultTriggerSpec k8SFaultSpec = new K8SFaultTriggerSpec();
            k8SFaultSpec.setChildSpecType(faultSpec.getClass().getName());
            k8SFaultSpec.setFaultSpec((CommandExecutionFaultSpec) faultSpec);
            k8SFaultSpec.setSchedule(faultSpec.getSchedule());
            return taskHelper.init(k8SFaultSpec, taskId);
        } else {
            return taskHelper.init(faultSpec, taskId);
        }
    }
}