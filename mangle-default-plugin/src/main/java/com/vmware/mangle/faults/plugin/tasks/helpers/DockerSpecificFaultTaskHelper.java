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

package com.vmware.mangle.faults.plugin.tasks.helpers;

import java.util.List;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.faults.plugin.helpers.docker.DockerFaultHelper;
import com.vmware.mangle.task.framework.helpers.AbstractCommandExecutionTaskHelper;
import com.vmware.mangle.task.framework.utils.TaskDescriptionUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * Implementation of AbstractRemoteCommandExecutionTaskHelper to Support Injection of Docker
 * specific faults
 *
 * @author rpraveen
 */


@Extension(ordinal = 1)
public class DockerSpecificFaultTaskHelper<T extends DockerFaultSpec> extends AbstractCommandExecutionTaskHelper<T> {


    private DockerFaultHelper dockerFaultHelper;


    @Autowired
    public void setDockerFaultHelper(DockerFaultHelper dockerFaultHelper) {
        this.dockerFaultHelper = dockerFaultHelper;
    }

    @Override
    public Task<T> init(T dockerFaultSpec, String injectedTaskId) {
        return init(new FaultTask<>(), dockerFaultSpec, injectedTaskId);
    }

    @Override
    public Task<T> init(T dockerFaultSpec) {
        return init(dockerFaultSpec, null);
    }

    @Override
    public void executeTask(Task<T> task) throws MangleException {
        if (task.getTaskType().equals(TaskType.INJECTION)) {
            CommandExecutionFaultSpec faultSpec = task.getTaskData();
            task.getTaskData().setInjectionCommandInfoList(
                    dockerFaultHelper.getInjectionCommandInfoList(getExecutor(task), faultSpec));
            task.getTaskData().setRemediationCommandInfoList(
                    dockerFaultHelper.getRemediationCommandInfoList(getExecutor(task), faultSpec));
        }
        super.executeTask(task);
    }

    /**
     * Overridden implementation of getExecutor(task) to return the executor based on User Input
     *
     * @throws MangleException
     */
    @Override
    public ICommandExecutor getExecutor(Task<T> task) throws MangleException {
        return dockerFaultHelper.getExecutor(task.getTaskData());
    }

    @Override
    public String getDescription(Task<T> task) {
        return TaskDescriptionUtils.getDescription(task);
    }

    @Override
    public void checkInjectionPreRequisites(Task<T> task) throws MangleException {
        // No injection prerequisites identified for this task as of now
    }

    @Override
    public void checkRemediationPreRequisites(Task<T> task) throws MangleException {
        // No remediation prerequisites identified for this task as of now
    }

    @Override
    protected void prepareEndpoint(Task<T> task, List<SupportScriptInfo> listFaultInjectionScripts)
            throws MangleException {
        //When needed we will implement this
    }

    @Override
    protected void checkTaskSpecificPrerequisites(Task<T> task) throws MangleException {
        //When needed we will implement this
    }
}
