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

package com.vmware.mangle.services.controller;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.enums.OperationStatus;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.services.deletionutils.TaskDeletionService;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author hkilari
 *
 */

@RestController
@Api(value = "/rest/api/v1/tasks")
@RequestMapping("/rest/api/v1/tasks")
public class TaskController {

    private TaskService taskService;
    private TaskDeletionService taskDeletionService;

    @Autowired
    public TaskController(TaskService taskService, TaskDeletionService taskDeletionService) {
        this.taskService = taskService;
        this.taskDeletionService = taskDeletionService;
    }

    @ApiOperation(value = "API to get details of a Tasks Executed by Mangle", nickname = "getAllTasks")
    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<Task<TaskSpec>>> getAllTasks(
            @RequestParam(name = "isScheduledTask", required = false) Boolean isScheduledTask) throws MangleException {
        List<Task<TaskSpec>> tasks = null;
        if (isScheduledTask != null) {
            tasks = taskService.getTaskByIsScheduledTask(isScheduledTask);
        } else {
            tasks = taskService.getAllTasks();
        }
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get task details from Mangle using its id", nickname = "getTaskInfo")
    @GetMapping(value = "/{taskId}", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> getTask(@PathVariable String taskId) throws MangleException {
        if (StringUtils.isEmpty(taskId)) {
            throw new MangleException(ErrorCode.NO_TASK_FOUND, taskId);
        }
        Task<TaskSpec> task = taskService.getTaskById(taskId);
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(task, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to delete tasks from the application")
    @DeleteMapping(value = "", produces = "application/json")
    public ResponseEntity<DeleteOperationResponse> deleteTasks(@RequestParam List<String> tasksIds)
            throws MangleException {
        DeleteOperationResponse response = taskDeletionService.deleteTasksByIds(tasksIds);
        HttpStatus responseStatus = HttpStatus.OK;
        HttpHeaders headers = new HttpHeaders();
        if (response.getAssociations().size() == 0) {
            response.setResult(OperationStatus.SUCCESS);
            headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.TASKS_DELETED);
        } else {
            responseStatus = HttpStatus.PRECONDITION_FAILED;
            response.setResult(OperationStatus.FAILED);
        }

        return new ResponseEntity<>(response, responseStatus);
    }
}
