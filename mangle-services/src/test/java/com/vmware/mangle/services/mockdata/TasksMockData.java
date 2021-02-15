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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.UUID;

import com.vmware.mangle.cassandra.model.faults.specs.FaultSpec;
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

    private String taskName1;
    private String taskName2;
    private String taskDescription1;
    private String taskDescription2;
    private TaskStatus taskStatus;
    private String endpointName1;
    private String endpointName2;
    private String taskFailureReason1;
    private String taskFailureReason2;
    private TaskInfo taskInfo;
    private Stack<TaskTrigger> taskTriggers;
    private T taskData1;
    private T taskData2;
    private String id1 = UUID.randomUUID().toString();
    private String id2 = UUID.randomUUID().toString();
    private String id3 = UUID.randomUUID().toString();

    public TasksMockData(T taskData) {
        Properties properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        this.taskName1 = properties.getProperty("taskName1");
        this.taskDescription1 = properties.getProperty("taskDescription1");
        this.endpointName1 = properties.getProperty("endpointName1");
        this.taskStatus = TaskStatus.IN_PROGRESS;
        this.taskFailureReason1 = properties.getProperty("taskFailureReason2");
        this.taskInfo = new TaskInfo();
        this.taskInfo.setTaskStatus(taskStatus);
        this.taskInfo.setPercentageCompleted(50);
        this.taskTriggers = new Stack<>();
        this.taskTriggers.add(new TaskTrigger());
        this.taskTriggers.peek().setMangleTaskInfo(taskInfo);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
        this.taskTriggers.peek().setStartTime(sdf.format(new Date()));
        if ( taskData1 instanceof FaultSpec ) {
            ((FaultSpec)taskData1).setEndpointName(this.endpointName1);
        }
        this.taskData1 = taskData;
    }

    public TasksMockData(T taskData1, T taskData2) {
        Properties properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        this.taskName1 = properties.getProperty("taskName1");
        this.taskName2 = properties.getProperty("taskName2");
        this.taskDescription1 = properties.getProperty("taskDescription1");
        this.taskDescription2 = properties.getProperty("taskDescription2");
        this.endpointName1 = properties.getProperty("endpointName1");
        this.endpointName2 = properties.getProperty("endpointName2");
        this.taskStatus = TaskStatus.IN_PROGRESS;
        this.taskFailureReason2 = properties.getProperty("taskFailureReason1");
        this.taskFailureReason1 = properties.getProperty("taskFailureReason2");
        this.taskInfo = new TaskInfo();
        this.taskInfo.setTaskStatus(taskStatus);
        this.taskInfo.setPercentageCompleted(50);
        this.taskTriggers = new Stack<>();
        this.taskTriggers.add(new TaskTrigger());
        this.taskTriggers.peek().setMangleTaskInfo(taskInfo);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
        this.taskTriggers.peek().setStartTime(sdf.format(new Date()));
        if ( taskData1 instanceof FaultSpec ) {
            ((FaultSpec)taskData1).setEndpointName(this.endpointName1);
        }
        if ( taskData2 instanceof FaultSpec ) {
            ((FaultSpec)taskData2).setEndpointName(this.endpointName2);
        }

        this.taskData1 = taskData1;
        this.taskData2 = taskData2;
    }


    public TasksMockData(String taskName2, String taskDescription2, TaskStatus taskStatus, String taskFailureReason2,
            TaskInfo taskInfo, T taskData) {
        this.taskName2 = taskName2;
        this.taskDescription2 = taskDescription2;
        this.taskStatus = taskStatus;
        this.taskFailureReason2 = taskFailureReason2;
        this.taskInfo = taskInfo;
        this.taskTriggers.add(new TaskTrigger());
        this.taskData2 = taskData;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Task<T> getDummy1Task() {
        Task<T> task = new FaultTask();
        task.setId(id2);
        task.setScheduledTask(true);
        task.setTriggers(taskTriggers);
        task.setTaskName(taskName1);
        task.setTaskDescription(taskDescription1);
        task.setTaskStatus(taskStatus);
        task.setTaskType(TaskType.INJECTION);
        task.setTaskFailureReason(taskFailureReason1);
        task.setTaskData(taskData1);
        task.setLastUpdated(System.currentTimeMillis());
        task.setExtensionName("com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper2");
        return task;
    }

    public Task<T> getDummy2Task() {
        Task<T> task = new FaultTask();
        task.setId(id2);
        task.setScheduledTask(true);
        task.setTriggers(taskTriggers);
        task.setTaskName(taskName2);
        task.setTaskDescription(taskDescription2);
        task.setTaskStatus(taskStatus);
        task.setTaskType(TaskType.INJECTION);
        task.setTaskFailureReason(taskFailureReason2);
        task.setTaskData(taskData2);
        task.setLastUpdated(System.currentTimeMillis());
        return task;
    }

    public List<Task<T>> getDummy1Tasks() {
        Task<T> task1 = getDummy1Task();
        Task<T> task2 = getDummy1Task();
        task2.setId(id3);
        return Arrays.asList(task1, task2);
    }

    public List<Task<T>> getDummy2Tasks() {
        Task<T> task1 = getDummy2Task();
        Task<T> task2 = getDummy2Task();
        task2.setId(id3);
        return Arrays.asList(task1, task2);
    }

    public Task<T> getRemediationTask() {
        Task<T> task = getDummy1Task();
        task.setTaskType(TaskType.REMEDIATION);
        task.setExtensionName("dummy");
        return task;
    }
}
