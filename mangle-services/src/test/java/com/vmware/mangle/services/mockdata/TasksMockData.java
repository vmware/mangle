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

package com.vmware.mangle.services.mockdata;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.UUID;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskInfo;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.utils.ReadProperty;
import com.vmware.mangle.utils.constants.Constants;

/**
 * Tasks Mock Data.
 *
 * @author hkilari
 */
public class TasksMockData<T extends TaskSpec> {

    private String taskName;
    private String taskDescription;
    private TaskStatus taskStatus;
    private String taskFailureReason;
    private TaskInfo taskInfo;
    private Stack<TaskTrigger> taskTriggers;
    private T taskData;
    private String id1 = UUID.randomUUID().toString();
    private String id2 = UUID.randomUUID().toString();
    private String id3 = UUID.randomUUID().toString();

    public TasksMockData(T taskData) {
        Properties properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        this.taskName = properties.getProperty("taskName");
        this.taskDescription = properties.getProperty("taskDescription");
        this.taskStatus = TaskStatus.IN_PROGRESS;
        this.taskFailureReason = properties.getProperty("taskFailureReason");
        this.taskInfo = new TaskInfo();
        this.taskInfo.setTaskStatus(taskStatus);
        this.taskInfo.setPercentageCompleted(50);
        this.taskTriggers = new Stack<>();
        this.taskTriggers.add(new TaskTrigger());
        this.taskTriggers.peek().setMangleTaskInfo(taskInfo);
        this.taskData = taskData;
    }

    public TasksMockData(String taskName, String taskDescription, TaskStatus taskStatus, String taskFailureReason,
            TaskInfo taskInfo, T taskData) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
        this.taskFailureReason = taskFailureReason;
        this.taskInfo = taskInfo;
        this.taskTriggers.add(new TaskTrigger());
        this.taskData = taskData;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Task<T> getDummyTask() {
        Task<T> task = new FaultTask();
        task.setId(id2);
        task.setScheduledTask(true);
        task.setTriggers(taskTriggers);
        task.setTaskName(taskName);
        task.setTaskDescription(taskDescription);
        task.setTaskStatus(taskStatus);
        task.setTaskFailureReason(taskFailureReason);
        task.setTaskData(taskData);
        return task;
    }

    public List<Task<T>> getDummyTasks() {
        Task<T> task1 = getDummyTask();
        Task<T> task2 = getDummyTask();
        task2.setId(id3);
        return Arrays.asList(task1, task2);
    }

    public Task<T> getRemediationTask() {
        Task<T> task = getDummyTask();
        task.setTaskType(TaskType.REMEDIATION);
        return task;
    }
}
