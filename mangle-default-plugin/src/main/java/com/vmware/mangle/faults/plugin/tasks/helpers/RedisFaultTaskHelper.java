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
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.faults.plugin.helpers.redis.RedisFaultHelper;
import com.vmware.mangle.task.framework.helpers.AbstractCommandExecutionTaskHelper;
import com.vmware.mangle.task.framework.utils.TaskDescriptionUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Implementation of AbstractRemoteCommandExecutionTaskHelper to Support Injection of Redis db
 * specific faults.
 *
 * @author kumargautam
 */
@Extension(ordinal = 1)
public class RedisFaultTaskHelper<T extends RedisFaultSpec> extends AbstractCommandExecutionTaskHelper<T> {

    private RedisFaultHelper redisFaultHelper;

    @Autowired
    public void setRedisFaultHelper(RedisFaultHelper redisFaultHelper) {
        this.redisFaultHelper = redisFaultHelper;
    }

    @Override
    public Task<T> init(T taskSpec) throws MangleException {
        return init(taskSpec, null);
    }

    @Override
    public Task<T> init(T taskSpec, String injectionId) throws MangleException {
        return init(new FaultTask<>(), taskSpec, injectionId);
    }

    @Override
    protected ICommandExecutor getExecutor(Task<T> task) throws MangleException {
        return redisFaultHelper.getExecutor(task.getTaskData());
    }

    @Override
    public void executeTask(Task<T> task) throws MangleException {
        if (task.getTaskType().equals(TaskType.INJECTION)) {
            CommandExecutionFaultSpec faultSpec = task.getTaskData();
            task.getTaskData().setInjectionCommandInfoList(
                    redisFaultHelper.getInjectionCommandInfoList(getExecutor(task), faultSpec));
            task.getTaskData().setRemediationCommandInfoList(
                    redisFaultHelper.getRemediationCommandInfoList(getExecutor(task), faultSpec));
        }
        super.executeTask(task);
    }

    @Override
    public String getDescription(Task<T> task) {
        return TaskDescriptionUtils.getDescription(task);
    }

    @Override
    protected void prepareEndpoint(Task<T> task, List<SupportScriptInfo> listFaultInjectionScripts)
            throws MangleException {
        //When needed we will implement this
    }

    @Override
    protected void checkRemediationPreRequisites(Task<T> task) throws MangleException {
        //No remediation prerequisites identified for this task as of now
    }

    @Override
    protected void checkInjectionPreRequisites(Task<T> task) throws MangleException {
        this.redisFaultHelper.validateExistingRedisFault(getExecutor(task));
    }

    @Override
    protected void checkTaskSpecificPrerequisites(Task<T> task) throws MangleException {
        //When needed we will implement this
    }
}
