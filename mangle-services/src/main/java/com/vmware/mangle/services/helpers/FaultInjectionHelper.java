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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.CredentialService;
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

    public void updateFaultSpec(TaskSpec spec) throws MangleException {
        CommandExecutionFaultSpec commandExecutionFaultSpec = getCommandExecutionFaultSpec(spec);

        commandExecutionFaultSpec
                .setEndpoint(endpointService.getEndpointByName(commandExecutionFaultSpec.getEndpointName()));
        if (commandExecutionFaultSpec.getEndpoint().getEndPointType() != EndpointType.DOCKER) {
            commandExecutionFaultSpec.setCredentials(credentialService
                    .getCredentialByName(commandExecutionFaultSpec.getEndpoint().getCredentialsName()));
        }
    }

    public Task<? extends TaskSpec> getTask(CommandExecutionFaultSpec faultSpec) throws MangleException {
        Task<? extends TaskSpec> task = faultTaskFactory.getTask(faultSpec);
        saveTask(task);
        return task;
    }

    public Task<TaskSpec> triggerRemediation(String taskId) throws MangleException {
        Task<TaskSpec> task = taskService.getTaskById(taskId);
        updateFaultSpec(task.getTaskData());
        Task<TaskSpec> remediationTask = faultTaskFactory.getRemediationTask(task, taskId);
        remediationTask.setTaskTroubleShootingInfo(task.getTaskTroubleShootingInfo());
        saveTask(remediationTask);
        return remediationTask;
    }

    private Task<TaskSpec> saveTask(Task<? extends TaskSpec> task) throws MangleException {
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
        if (!EndpointType.DOCKER.equals(faultSpec.getEndpoint().getEndPointType())) {
            faultSpec.setCredentials(
                    credentialService.getCredentialByName(faultSpec.getEndpoint().getCredentialsName()));
        }
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

}
