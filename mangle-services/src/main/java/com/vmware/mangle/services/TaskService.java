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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.datastax.driver.core.PagingState;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.services.repository.TaskRepository;
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
        log.info("Geting all Tasks...");
        return taskRepository.findAll();
    }

    public Task<TaskSpec> getTaskById(String taskId) throws MangleException {
        log.info("Geting task by id : " + taskId);
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
            log.debug("Adding/Updating Task with Id : " + task.getId());
            return taskRepository.save(task);
        } else {
            log.error(ErrorConstants.TASK + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.TASK);
        }
    }

    public Slice<Task<TaskSpec>> getTaskBasedOnPage(int page, int size) {
        log.info("Geting requested page for Tasks...");
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
            FaultTask<?> dbTask = (FaultTask<?>) getTaskById(id);
            dbTask.setRemediated(isRemediated);
            return addOrUpdateTask(dbTask);
        }
    }

    public int getTotalPages(Slice<Task<TaskSpec>> slice) {
        long totalCount = taskRepository.count();
        return slice.getSize() == 0 ? 1 : (int) Math.ceil((double) totalCount / (double) slice.getSize());
    }

    public List<Task<TaskSpec>> getTaskByIsScheduledTask(boolean isScheduledTask) throws MangleException {
        log.info("Geting task by isScheduledTask : " + isScheduledTask);
        List<Task<TaskSpec>> tasks = taskRepository.findByIsScheduledTask(isScheduledTask);
        if (tasks != null && !tasks.isEmpty()) {
            return tasks;
        } else {
            throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.IS_SCHEDULED_TASK,
                    isScheduledTask);
        }
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
        return tasks.stream().filter(task -> task.getTaskStatus() == TaskStatus.IN_PROGRESS)
                .collect(Collectors.toList());
    }
}
