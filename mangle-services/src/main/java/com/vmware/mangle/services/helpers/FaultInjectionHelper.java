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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.endpoint.CertificatesSpec;
import com.vmware.mangle.cassandra.model.endpoint.DockerCertificates;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.KillProcessFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTriggeringTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.CredentialService;
import com.vmware.mangle.services.EndpointCertificatesService;
import com.vmware.mangle.services.EndpointService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.events.task.TaskCreatedEvent;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 * @author bkaranam
 */
@Component
public class FaultInjectionHelper {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private FaultTaskFactory faultTaskFactory;

    @Autowired
    private EndpointService endpointService;

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private EndpointCertificatesService certificatesService;

    public void updateFaultSpec(TaskSpec spec) throws MangleException {
        CommandExecutionFaultSpec commandExecutionFaultSpec = getCommandExecutionFaultSpec(spec);

        commandExecutionFaultSpec
                .setEndpoint(endpointService.getEndpointByName(commandExecutionFaultSpec.getEndpointName()));
        if (commandExecutionFaultSpec.getEndpoint().getEndPointType() != EndpointType.DOCKER) {
            commandExecutionFaultSpec.setCredentials(credentialService
                    .getCredentialByName(commandExecutionFaultSpec.getEndpoint().getCredentialsName()));
        }

        if (commandExecutionFaultSpec.getEndpoint().getEndPointType() == EndpointType.DOCKER && StringUtils.hasText(
                commandExecutionFaultSpec.getEndpoint().getDockerConnectionProperties().getCertificatesName())) {
            CertificatesSpec certificatesSpec = certificatesService.getCertificatesByName(
                    commandExecutionFaultSpec.getEndpoint().getDockerConnectionProperties().getCertificatesName());
            commandExecutionFaultSpec.getEndpoint().getDockerConnectionProperties()
                    .setCertificatesSpec((DockerCertificates) certificatesSpec);
        }
    }

    public Task<? extends TaskSpec> getTask(CommandExecutionFaultSpec faultSpec) throws MangleException {
        Task<? extends TaskSpec> task = faultTaskFactory.getTask(faultSpec);
        saveTask(task);
        return task;
    }

    public Task<TaskSpec> triggerRemediation(String taskIdentifier) throws MangleException {
        Task<TaskSpec> task = null;
        try {
            UUID.fromString(taskIdentifier);
            task = taskService.getTaskById(taskIdentifier);
        } catch (IllegalArgumentException e) {
            task = taskService.getTaskByTaskName(taskIdentifier);
        }


        if (task instanceof FaultTriggeringTask
                && !CollectionUtils.isEmpty(task.getTriggers().peek().getChildTaskIDs())) {
            List<Task<TaskSpec>> k8sChildTasks = taskService.getTasksByIds(task.getTriggers().peek().getChildTaskIDs());
            List<String> k8sChildTaskNames = k8sChildTasks.stream().map(Task::getTaskName).collect(Collectors.toList());
            throw new MangleException(ErrorCode.REMEDIATION_K8S_TASK, k8sChildTaskNames);
        }

        updateFaultSpec(task.getTaskData());
        Task<TaskSpec> remediationTask = faultTaskFactory.getRemediationTask(task, taskIdentifier);
        remediationTask.setTaskTroubleShootingInfo(task.getTaskTroubleShootingInfo());
        saveTask(remediationTask);
        return remediationTask;
    }

    public Task<TaskSpec> saveTask(Task<? extends TaskSpec> task) throws MangleException {
        Task<TaskSpec> retrievedTask = addOrUpdateTask(task);
        publisher.publishEvent(new TaskCreatedEvent<>(retrievedTask));
        return retrievedTask;
    }

    private Task<TaskSpec> addOrUpdateTask(Task<? extends TaskSpec> task) throws MangleException {
        return taskService.addOrUpdateTask(task);
    }

    private CommandExecutionFaultSpec getCommandExecutionFaultSpec(TaskSpec taskSpec) {
        if (taskSpec instanceof K8SFaultTriggerSpec) {
            return ((K8SFaultTriggerSpec) taskSpec).getFaultSpec();
        } else {
            return (CommandExecutionFaultSpec) taskSpec;
        }
    }

    public void validateSpec(CommandExecutionFaultSpec faultSpec) throws MangleException {
        faultSpec.setEndpoint(endpointService.getEndpointByName(faultSpec.getEndpointName()));
        validateScheduleInfo(faultSpec);
        updateFaultSpec(faultSpec);
    }

    public void validateKillProcessFaultSpec(CommandExecutionFaultSpec faultSpec) throws MangleException {
        KillProcessFaultSpec killProcessFaultSpec = (KillProcessFaultSpec) faultSpec;
        if (killProcessFaultSpec.getRemediationCommand() != null
                && killProcessFaultSpec.getRemediationCommand().trim().isEmpty()) {
            throw new MangleException(ErrorCode.BAD_REQUEST, "remediationCommand: must not be empty");
        }
        validateSpec(faultSpec);
    }

    private void validateScheduleInfo(CommandExecutionFaultSpec faultSpec) throws MangleException {
        if (null != faultSpec.getSchedule()) {
            if (StringUtils.isEmpty(faultSpec.getSchedule().getCronExpression())
                    && null == faultSpec.getSchedule().getTimeInMilliseconds()) {
                throw new MangleException(ErrorCode.INVALID_SCHEDULE_INPUTS);
            }
            if (!StringUtils.isEmpty(faultSpec.getSchedule().getCronExpression())) {
                try {
                    new CronSequenceGenerator(faultSpec.getSchedule().getCronExpression());
                } catch (IllegalArgumentException iae) {
                    throw new MangleException(ErrorCode.INVALID_CRON_EXPRESSION,
                            faultSpec.getSchedule().getCronExpression());
                }
            }
        }
    }

    /**
     * @param faultSpec
     * @throws MangleException
     */
    public void validateEndpointTypeSpecificArguments(CommandExecutionFaultSpec faultSpec) throws MangleException {
        if (faultSpec.getEndpoint().getEndPointType() == EndpointType.DOCKER
                && faultSpec.getDockerArguments() == null) {
            throw new MangleException(ErrorCode.DOCKER_SPECIFIC_ARGUMENTS_REQUIRED);
        }

        if (faultSpec.getEndpoint().getEndPointType() == EndpointType.K8S_CLUSTER
                && faultSpec.getK8sArguments() == null) {
            throw new MangleException(ErrorCode.K8S_SPECIFIC_ARGUMENTS_REQUIRED);
        }
    }
}