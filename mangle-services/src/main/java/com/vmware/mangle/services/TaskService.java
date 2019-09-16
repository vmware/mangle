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

package com.vmware.mangle.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.datastax.driver.core.PagingState;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.RemediableTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskInfo;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.services.repository.TaskRepository;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Service class Task.
 *
 * @author kumargautam
 */
@Component
@Log4j2
public class TaskService {

    private TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task<TaskSpec>> getAllTasks() {
        log.info("Retrieving all Tasks...");
        return taskRepository.findAll();
    }

    public List<Task<TaskSpec>> getAllTasks(Boolean isScheduledTask, String taskName) {
        if (null != isScheduledTask && StringUtils.hasText(taskName)) {
            return getTaskByIsScheduledTaskAndTaskName(isScheduledTask, taskName);
        } else if (null != isScheduledTask && !StringUtils.hasText(taskName)) {
            return getTaskByIsScheduledTask(isScheduledTask);
        } else if (null == isScheduledTask && StringUtils.hasText(taskName)) {
            return getTaskByTaskName(taskName);
        } else {
            return getAllTasks();
        }
    }

    public Task<TaskSpec> getTaskById(String taskId) throws MangleException {
        log.info("Retrieving task by id : " + taskId);
        if (!StringUtils.isEmpty(taskId)) {
            Optional<Task<TaskSpec>> optional = taskRepository.findById(taskId);
            if (optional.isPresent()) {
                return optional.get();
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.TASK_ID, taskId);
            }

        } else {
            log.error(ErrorConstants.TASK_ID + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.TASK_ID);
        }
    }

    @SuppressWarnings("unchecked")
    public Task<TaskSpec> addOrUpdateTask(Task<?> task) throws MangleException {
        if (task != null) {
            log.debug("Creating Task with Id : " + task.getId());
            return taskRepository.save(task);
        } else {
            log.error(ErrorConstants.TASK + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.TASK);
        }
    }

    public Slice<Task<TaskSpec>> getTaskBasedOnPage(int page, int size) {
        log.info("Retrieving requested page for Tasks...");
        if (page == 1) {
            return taskRepository.findAll(CassandraPageRequest.of(page - 1, size));
        } else {
            CassandraPageRequest cassandraPageRequest = CassandraPageRequest.of(0, size);
            Slice<Task<TaskSpec>> slice = taskRepository.findAll(cassandraPageRequest);
            for (int i = 1; i < page; i++) {
                PagingState pagingState = ((CassandraPageRequest) slice.getPageable()).getPagingState();
                if (pagingState == null) {
                    return slice;
                }
                cassandraPageRequest = CassandraPageRequest.of(slice.getPageable(), pagingState);
                slice = taskRepository.findAll(cassandraPageRequest);
            }
            return slice;
        }
    }

    public Task<TaskSpec> updateRemediationFieldofTaskById(String id, Boolean isRemediated) throws MangleException {
        log.info("Updating Task by id=" + id);
        if (id == null || isRemediated == null || "".equals(id)) {
            log.error("id and isRemediated should not be empty or null");
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.TASK);
        } else {
            RemediableTask<?> dbTask = (RemediableTask<?>) getTaskById(id);
            dbTask.setRemediated(isRemediated);
            return addOrUpdateTask(dbTask);
        }
    }


    public int getTotalPages(Slice<Task<TaskSpec>> slice) {
        long totalCount = taskRepository.count();
        return slice.getSize() == 0 ? 1 : (int) Math.ceil((double) totalCount / (double) slice.getSize());
    }

    public List<Task<TaskSpec>> getTaskByIsScheduledTask(boolean isScheduledTask) {
        log.info("Retrieving all tasks by isScheduledTask : " + isScheduledTask);
        return taskRepository.findByIsScheduledTask(isScheduledTask);
    }

    public List<Task<TaskSpec>> getTaskByIsScheduledTaskAndTaskName(boolean isScheduledTask, String taskName) {
        log.info("Retrieving all tasks with scheduled {} and taskName {}", isScheduledTask, taskName);
        return taskRepository.findByTaskNameAndIsScheduledTask(taskName, isScheduledTask);
    }

    /**
     * Returns the list of task objects, for the given list of taskIds
     *
     * @param taskIds
     *            List of taskIds for which the task objects need to be queried
     * @return List of task objects
     */
    public List<Task<TaskSpec>> getTasksByIds(List<String> taskIds) {
        return taskRepository.findByIds(taskIds);
    }

    public List<Task<TaskSpec>> getInProgressTasks() {
        List<Task<TaskSpec>> tasks = taskRepository.findAll();
        return tasks.stream().filter(task -> (task.getTaskStatus() == TaskStatus.IN_PROGRESS
                || task.getTaskStatus() == TaskStatus.INITIALIZING)).collect(Collectors.toList());
    }

    public List<Task<TaskSpec>> getTaskByTaskName(String taskName) {
        log.debug("Retrieving task by task name: {}", taskName);
        List<Task<TaskSpec>> tasks = taskRepository.findByTaskName(taskName);
        if (tasks == null) {
            throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.TASK_NAME, taskName);
        }
        return tasks;
    }

    /**
     * Method is used to clean-up the in-progress tasks which trigger duration with in specified
     * Threshold time.
     *
     * @param taskCleanupThreshold
     * @throws ParseException
     * @throws MangleException
     */
    @SuppressWarnings({ "deprecation" })
    public String cleanupInprogressTasks(long taskCleanupThreshold) throws ParseException, MangleException {
        log.debug("Received request to clean-up the in-progress tasks...");
        List<String> taskIds = new ArrayList<>();
        for (Task<TaskSpec> task : getInProgressTasks()) {
            TaskTrigger trigger = task.getTriggers().peek();
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()
                    - ((new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT)).parse(trigger.getStartTime()).getTime()));
            if (diffInMinutes < taskCleanupThreshold) {
                trigger.setTaskStatus(TaskStatus.FAILED);
                trigger.setEndTime(new Date(System.currentTimeMillis()).toGMTString());
                TaskInfo taskInfo = new TaskInfo();
                taskInfo.setPercentageCompleted(100);
                taskInfo.setTaskStatus(TaskStatus.FAILED);
                trigger.setMangleTaskInfo(taskInfo);
                trigger.setTaskFailureReason(CommonConstants.TASK_FAILURE_REASON_FOR_CLEANUP);
                addOrUpdateTask(task);
                taskIds.add(task.getId());
            }
        }
        return taskIds.isEmpty() ? CommonConstants.NO_INPROGRESS_TASK_FOR_CLEANUP
                : String.format(CommonConstants.INPROGRESS_TASK_FOR_CLEANUP, taskIds);
    }
}