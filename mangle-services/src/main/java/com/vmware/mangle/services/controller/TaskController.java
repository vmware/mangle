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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskFilter;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.model.response.ErrorDetails;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.helpers.TaskHelper;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreService;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 * @author hkilari
 *
 */
@Log4j2
@RestController
@Api(value = "/rest/api/v1/tasks")
@RequestMapping("/rest/api/v1/tasks")
public class TaskController {

    private TaskService taskService;
    private TaskHelper taskHelper;
    private ResiliencyScoreService resiliencyScoreService;

    @Autowired
    public TaskController(TaskService taskService, TaskHelper taskHelper,
            ResiliencyScoreService resiliencyScoreService) {
        this.taskService = taskService;
        this.taskHelper = taskHelper;
        this.resiliencyScoreService = resiliencyScoreService;
    }

    @ApiOperation(value = "API to get details of a Tasks Executed by Mangle", nickname = "getAllTasks")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<Task<TaskSpec>>> getAllTasks(
            @RequestParam(name = "isScheduledTask", required = false) Boolean isScheduledTask,
            @RequestParam(name = "taskName", required = false) String taskName) throws MangleException {
        log.info("Received request to retrieve details of all the tasks");
        Resources<Task<TaskSpec>> resources = new Resources<>(taskService.getAllTasks(isScheduledTask, taskName));
        resources.add(getSelfLink(), getTaskHateoasLink(), deleteTasksHateoasLink());
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get all the sorted task based on index", nickname = "getTaskBasedOnIndex")
    @PostMapping(value = "/pagination", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Map<String, Object>>> getTaskBasedOnIndex(@RequestBody TaskFilter taskFilter)
            throws MangleException {
        log.debug("Start execution of getTaskBasedOnIndex() method with filter:" + taskFilter);

        Resource<Map<String, Object>> resource = new Resource<>(taskHelper
                .getTaskBasedOnIndex(taskService.getAllTasks(), resiliencyScoreService.getAllTasks(), taskFilter));
        resource.add(getSelfLink(), getTaskHateoasLink(), deleteTasksHateoasLink());
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get task details from Mangle using its id", nickname = "getTaskInfo")
    @GetMapping(value = "/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Object>> getTask(@PathVariable String taskId) throws MangleException {
        log.info("Received request to retrieve details for the task with the taskId: {}", taskId);
        if (StringUtils.isEmpty(taskId)) {
            throw new MangleException(ErrorCode.NO_TASK_FOUND, taskId);
        }
        Resource<Object> taskResource;
        if (taskHelper.getTaskType(taskId) == TaskType.RESILIENCY_SCORE) {
            ResiliencyScoreTask resiliencyScoreTask = resiliencyScoreService.getTaskById(taskId);
            taskResource = new Resource<>(resiliencyScoreTask);
        } else {
            Task<TaskSpec> task = taskService.getTaskById(taskId);
            taskResource = new Resource<>(task);
        }
        HttpHeaders headers = new HttpHeaders();
        taskResource.add(getSelfLink(), getAllTasksHateoasLink(), getTaskHateoasLink(), deleteTasksHateoasLink());

        return new ResponseEntity<>(taskResource, headers, HttpStatus.OK);
    }


    @ApiOperation(value = "API to delete tasks from the application")
    @DeleteMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ErrorDetails> deleteTasks(@RequestParam List<String> tasksIds) throws MangleException {
        DeleteOperationResponse response = taskHelper.deleteTasks(tasksIds);
        ErrorDetails errorDetails = new ErrorDetails();
        if (response.getAssociations().size() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            Map<String, Map<String, List<String>>> associations = new HashMap<>();
            associations.put("associations", response.getAssociations());
            errorDetails.setTimestamp(new Date());
            errorDetails.setDescription(response.getResponseMessage());
            errorDetails.setCode(ErrorCode.DELETE_OPERATION_FAILED.getCode());
            errorDetails.setDetails(associations);
        }

        return new ResponseEntity<>(errorDetails, HttpStatus.PRECONDITION_FAILED);
    }

    @ApiOperation(value = "API to clean-up of InProgress tasks since threshold time specified", nickname = "cleanupInprogressTasks")
    @PutMapping(value = "/clean-up", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> cleanupInprogressTasks(
            @ApiParam(value = "Task cleanup threshold value in minutes") @RequestParam(value = "taskCleanupThreshold", required = false, defaultValue = "60") String taskCleanupThreshold)
            throws ParseException, MangleException {
        return new ResponseEntity<>(taskService.cleanupInprogressTasks(Long.parseLong(taskCleanupThreshold)),
                HttpStatus.OK);
    }

    public Link getSelfLink() {
        return new Link(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri().toASCIIString())
                .withSelfRel();
    }

    public Link getAllTasksHateoasLink() throws MangleException {
        return linkTo(methodOn(TaskController.class).getAllTasks(false, "")).withRel("GET-ALL");
    }

    public Link getTaskHateoasLink() throws MangleException {
        return linkTo(methodOn(TaskController.class).getTask("")).withRel("GET-TASK");
    }

    public Link deleteTasksHateoasLink() throws MangleException {
        return linkTo(methodOn(TaskController.class).deleteTasks(Collections.emptyList())).withRel("DELETE");
    }
}