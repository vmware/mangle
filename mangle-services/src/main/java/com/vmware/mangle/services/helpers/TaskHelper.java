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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.faults.specs.EndpointGroupFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.FaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskFilter;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.TaskWrapper;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.deletionutils.TaskDeletionService;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreService;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author dbhat
 */
@Component
@Log4j2
public class TaskHelper {

    @Autowired
    private ResiliencyScoreService resiliencyScoreService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskDeletionService taskDeletionService;

    public TaskType getTaskType(String taskId) throws MangleException {
        try {
            Task<? extends TaskSpec> task = taskService.getTaskById(taskId);
            return task.getTaskType();
        } catch (MangleRuntimeException mangleRunTimeException) {
            ResiliencyScoreTask resiliencyScoreTask = resiliencyScoreService.getTaskById(taskId);
            return resiliencyScoreTask.getTaskType();
        }
    }

    public Map<String, Object> getTaskBasedOnIndex(List<Task<TaskSpec>> faultTasks,
            List<ResiliencyScoreTask> resiliencyScoreTasks, TaskFilter taskFilter) {
        log.debug("Processing all the tasks...");
        List<TaskWrapper> allTasks = mergeTasks(faultTasks, resiliencyScoreTasks);
        List<Object> tasksToReturn = new ArrayList<>();
        List<TaskWrapper> filteredTasks = getFilteredTasks(allTasks, taskFilter.getTaskType(),
                taskFilter.getTaskDescription(), taskFilter.getTaskStatus(), taskFilter.getEndpointName());

        if (CollectionUtils.isEmpty(allTasks)) {
            log.debug("No tasks found in the system.");
        }

        for (int i = taskFilter.getFromIndex(); i <= taskFilter.getToIndex(); i++) {
            if (filteredTasks.size() <= i) {
                break;
            }
            tasksToReturn.add(filteredTasks.get(i).getMangleTask());
        }
        Map<String, Object> pagedObject = new HashMap<>();
        pagedObject.put(Constants.TASK_SIZE, filteredTasks.size());
        pagedObject.put(Constants.TASK_LIST, tasksToReturn);
        return pagedObject;
    }

    @SuppressWarnings("squid:S1871")
    private List<TaskWrapper> getFilteredTasks(List<TaskWrapper> allTasks, String taskType, String taskDescription,
            String taskStatus, String endPointName) {
        return allTasks.stream()
                .filter(t -> (StringUtils.isEmpty(taskType) || t.getTaskType().toString().contains(taskType))
                        && (StringUtils.isEmpty(taskDescription) || t.getTaskDescription().contains(taskDescription))
                        && (StringUtils.isEmpty(taskStatus) || t.getTaskStatus().toString().contains(taskStatus))
                        && (StringUtils.isEmpty(endPointName) || t.getEndpointName().contains(endPointName)))
                .sorted((t1, t2) -> t2.getLastUpdated().compareTo(t1.getLastUpdated())).collect(Collectors.toList());
    }

    private List<TaskWrapper> mergeTasks(List<Task<TaskSpec>> faultTasks,
            List<ResiliencyScoreTask> resiliencyScoreTasks) {
        List<TaskWrapper> mergedTasks = new ArrayList<>();
        if (CollectionUtils.isEmpty(faultTasks) && CollectionUtils.isEmpty(resiliencyScoreTasks)) {
            log.debug("All the specified tasks are empty and hence, nothing to merge. Returning empty list.");
            return mergedTasks;
        }
        if (CollectionUtils.isEmpty(faultTasks)) {
            log.debug(
                    "ResiliencyScore tasks are found and NO fault tasks are found in DB. Hence returning only resiliencyScore tasks");
            return getTaskVoForResiliencyScoreTasks(resiliencyScoreTasks);
        }
        if (CollectionUtils.isEmpty(resiliencyScoreTasks)) {
            log.debug(
                    "Fault tasks found in DB but, there are NO resiliencyscore tasks are found in DB. Hence, returning only fault tasks");
            return getTaskVoForFaultTasks(faultTasks);
        }
        log.debug(" Fault tasks and Resiliency score tasks are found in DB. merging both of them.");
        mergedTasks.addAll(getTaskVoForFaultTasks(faultTasks));
        mergedTasks.addAll(getTaskVoForResiliencyScoreTasks(resiliencyScoreTasks));
        log.debug("TaskVO objects are created by combining both type of tasks.");
        return mergedTasks;
    }

    private List<TaskWrapper> getTaskVoForFaultTasks(List<Task<TaskSpec>> faultTasks) {
        List<TaskWrapper> faultTaskWrapper = new ArrayList<>();
        for (Task<TaskSpec> faultTask : faultTasks) {
            TaskWrapper taskWrapper = new TaskWrapper();
            taskWrapper.setMangleTask(faultTask);
            taskWrapper.setLastUpdated(faultTask.getLastUpdated());
            taskWrapper.setTaskDescription(faultTask.getTaskDescription());
            taskWrapper.setTaskStatus(faultTask.getTaskStatus());
            taskWrapper.setTaskType(faultTask.getTaskType());
            TaskSpec taskData = faultTask.getTaskData();

            if (taskData instanceof K8SFaultTriggerSpec && ((K8SFaultTriggerSpec) taskData).getFaultSpec() != null) {
                taskWrapper.setEndpointName(((K8SFaultTriggerSpec) taskData).getFaultSpec().getEndpointName());
            } else if (taskData instanceof EndpointGroupFaultTriggerSpec
                    && ((EndpointGroupFaultTriggerSpec) taskData).getFaultSpec() != null) {
                taskWrapper
                        .setEndpointName(((EndpointGroupFaultTriggerSpec) taskData).getFaultSpec().getEndpointName());
            } else if (taskData instanceof FaultSpec) {
                taskWrapper.setEndpointName(((FaultSpec) taskData).getEndpointName());
            }

            faultTaskWrapper.add(taskWrapper);
        }
        return faultTaskWrapper;
    }

    private List<TaskWrapper> getTaskVoForResiliencyScoreTasks(List<ResiliencyScoreTask> resiliencyScoreTasks) {
        List<TaskWrapper> resiliencyScoreTaskWrapper = new ArrayList<>();
        for (ResiliencyScoreTask resiliencyScoreTask : resiliencyScoreTasks) {
            TaskWrapper taskWrapper = new TaskWrapper();
            taskWrapper.setMangleTask(resiliencyScoreTask);
            taskWrapper.setLastUpdated(resiliencyScoreTask.getLastUpdated());
            taskWrapper.setTaskDescription(resiliencyScoreTask.getTaskDescription());
            taskWrapper.setTaskStatus(resiliencyScoreTask.getTaskStatus());
            taskWrapper.setTaskType(resiliencyScoreTask.getTaskType());
            resiliencyScoreTaskWrapper.add(taskWrapper);
        }
        return resiliencyScoreTaskWrapper;
    }

    /**
     *
     * @param taskIds
     *            : List of Tasks to be deleted. The tasks can be of type : Fault Injection Task OR
     *            Resiliency Score Task.
     *
     * @return : DeleteOperationResponse: having details on the status of tasks deletion.
     * @throws MangleException
     */
    public DeleteOperationResponse deleteTasks(List<String> taskIds) throws MangleException {
        log.info("Deleting the tasks");
        if (CollectionUtils.isEmpty(taskIds)) {
            log.warn(ErrorConstants.TASK_ID + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.TASK_ID);
        }
        List<String> resiliencyScoreTasks = new ArrayList<>();
        List<String> faultTasks = new ArrayList<>();
        for (String task : taskIds) {
            if (getTaskType(task) == TaskType.RESILIENCY_SCORE) {
                resiliencyScoreTasks.add(task);
            } else {
                faultTasks.add(task);
            }
        }
        DeleteOperationResponse deleteResponse = new DeleteOperationResponse();
        DeleteOperationResponse faultTasksResponse = new DeleteOperationResponse();
        if (!CollectionUtils.isEmpty(resiliencyScoreTasks)) {
            deleteResponse = resiliencyScoreService.deleteTasksByIds(resiliencyScoreTasks);
        }
        if (!CollectionUtils.isEmpty(faultTasks)) {
            faultTasksResponse = taskDeletionService.deleteTasksByIds(faultTasks);
        }
        if (!faultTasksResponse.getAssociations().isEmpty()) {
            deleteResponse.getAssociations().putAll(faultTasksResponse.getAssociations());
            deleteResponse.setResponseMessage(ErrorConstants.TASK_DELETION_PRECHECK_FAIL);
        }
        return deleteResponse;
    }

}
