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

import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.faults.plugin.helpers.aws.AwsRDSFaultHelper;
import com.vmware.mangle.model.aws.AwsRDSInstance;
import com.vmware.mangle.model.aws.faults.spec.AwsRDSFaultSpec;
import com.vmware.mangle.task.framework.helpers.AbstractCommandExecutionTaskHelper;
import com.vmware.mangle.task.framework.utils.TaskDescriptionUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author bkaranam
 *
 *         AWS RDS specific fault Task implementation that delegates the creation of the injection
 *         commands, remediation commands and execution of each one of them
 */
@Extension(ordinal = 1)
public class AwsRDSFaultTaskHelper<T extends AwsRDSFaultSpec> extends AbstractCommandExecutionTaskHelper<T> {

    private AwsRDSFaultHelper awsRDSFaultHelper;

    @Autowired
    public void setAwsRDSFaultHelper(AwsRDSFaultHelper awsRDSFaultHelper) {
        this.awsRDSFaultHelper = awsRDSFaultHelper;
    }

    @Override
    public Task<T> init(T faultSpec) {
        return init(faultSpec, null);
    }

    public Task<T> init(T faultSpec, String injectionId) {
        return init(new FaultTask<>(), faultSpec, injectionId);
    }

    @Override
    protected void prepareEndpoint(Task<T> task, List<SupportScriptInfo> listFaultInjectionScripts)
            throws MangleException {
        //When needed we will implement this
    }

    @Override
    protected ICommandExecutor getExecutor(Task<T> task) throws MangleException {
        return awsRDSFaultHelper.getExecutor(task.getTaskData());
    }

    @Override
    public String getDescription(Task<T> task) {
        return TaskDescriptionUtils.getDescription(task);
    }

    @Override
    public void checkInjectionPreRequisites(Task<T> task) throws MangleException {
        //No Injection prerequisites are determined as of now for this endpoint
    }

    @Override
    public void checkRemediationPreRequisites(Task<T> task) throws MangleException {
        //No Injection prerequisites are determined as of now for this endpoint
    }

    @Override
    public void executeTask(Task<T> task) throws MangleException {
        AwsRDSFaultSpec faultSpec = task.getTaskData();
        if (task.getTaskType().equals(TaskType.INJECTION)) {
            List<AwsRDSInstance> rdsInstances = awsRDSFaultHelper.getRdsInstances(faultSpec.getDbIdentifiers(),
                    faultSpec.isRandomInjection(), faultSpec);
            faultSpec.setSelectedRDSInstances(rdsInstances);
            task.getTaskData().setInjectionCommandInfoList(
                    awsRDSFaultHelper.getInjectionCommandInfoList(getExecutor(task), faultSpec));
            task.getTaskData().setRemediationCommandInfoList(
                    awsRDSFaultHelper.getRemediationCommandInfoList(getExecutor(task), faultSpec));
        }
        super.executeTask(task);
        task.updateTaskOutPut("Executed on RDS instances: " + faultSpec.getSelectedRDSInstances());
        task.updateTaskDescription(getDescription(task));
    }

    @Override
    protected void checkTaskSpecificPrerequisites(Task<T> task) throws MangleException {
        //When needed we will implement this
    }
}
