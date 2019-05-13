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

package com.vmware.mangle.task.framework.helpers;

import java.util.HashMap;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.RemediableTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskInfo;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskTroubleShootingInfo;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.task.framework.skeletons.ITaskHelper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author hkilari
 */
@Component
public abstract class AbstractTaskHelper<T extends TaskSpec> implements ITaskHelper<T> {

    private ApplicationEventPublisher publisher;

    public AbstractTaskHelper() {
    }

    public ApplicationEventPublisher getPublisher() {
        return publisher;
    }

    @Override
    public void setEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public Task<T> init(Task<T> task, T taskData, String injectedTaskId) {
        if (task.isInitialized()) {
            return task;
        }
        task.setTaskClass(task.getClass().getName());
        String className = this.getClass().getName();
        initTroubleshootingInfo(task, injectedTaskId);
        task.setExtensionName(this.getClass().getName());
        task.setTaskData(taskData);
        task.setTaskName(className.substring(className.lastIndexOf('.') + 1) + "-" + System.currentTimeMillis());
        if (task.getTriggers() == null) {
            task.setTriggers(new Stack<>());
        }
        task.setTaskDescription(getDescription(task));
        task.setInitialized(true);
        return task;
    }

    @SuppressWarnings("unchecked")
    private void initTroubleshootingInfo(Task<T> task, String injectedTaskId) {
        if (StringUtils.isNotEmpty(injectedTaskId)) {
            ((RemediableTask<TaskSpec>) task).setInjectionTaskId(injectedTaskId);
            task.setTaskType(TaskType.REMEDIATION);
            task.setTaskTroubleShootingInfo(task.getTaskTroubleShootingInfo());
        } else {
            task.setTaskType(TaskType.INJECTION);
            TaskTroubleShootingInfo taskTroubleShootingInfo = new TaskTroubleShootingInfo();
            taskTroubleShootingInfo.setAdditionalInfo(new HashMap<>());
            taskTroubleShootingInfo.setSupportFiles(new HashMap<>());
            task.setTaskTroubleShootingInfo(taskTroubleShootingInfo);
        }
    }

    @Override
    public void run(Task<T> task) throws MangleException {
        executeTask(task);
    }

    @Override
    public TaskInfo getInfo(Task<T> task) throws MangleException {
        TaskInfo taskInfo = new TaskInfo();
        TaskTrigger trigger = task.getTriggers().peek();
        taskInfo.setTaskStatus(trigger.getTaskStatus());
        if (trigger.getTaskStatus() == TaskStatus.COMPLETED) {
            taskInfo.setPercentageCompleted(100);
        } else {
            taskInfo.setPercentageCompleted(0);
        }
        return taskInfo;
    }

    public void setTaskOutput(Task<T> task, String taskOutput) {
        task.getTriggers().peek().setTaskOutput(taskOutput);
    }

    @Override
    public void cancel() {

    }

    public abstract Task<T> init(T taskSpec) throws MangleException;

    public abstract Task<T> init(T taskSpec, String injectionId) throws MangleException;

}
