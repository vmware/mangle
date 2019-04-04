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
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.cassandra.model.scheduler.ScheduledTaskStatus;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.model.response.DeleteSchedulerResponse;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.services.scheduler.Scheduler;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Mangle Scheduling Services
 *
 * @author bkaranam
 * @author ashrimali
 */
@RestController
@RequestMapping("/rest/api/v1/scheduler")
@Api("/rest/api/v1/scheduler")
public class SchedulerController {

    private Scheduler scheduler;

    @Autowired
    public SchedulerController(Scheduler scheduler) {
        this.scheduler = scheduler;
    }


    @ApiOperation(value = "API to  cancel scheduled task/job(s) ", nickname = "cancelScheduledJobs")
    @RequestMapping(value = "/cancel/{jobIds}", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Map<String, ScheduledTaskStatus>> cancelScheduledJobs(
            @PathVariable("jobIds") List<String> jobIds) {
        Map<String, ScheduledTaskStatus> statusMap = scheduler.cancelScheduledJobs(jobIds);
        if (null != statusMap && !(statusMap.isEmpty())) {
            return verifyJobsStatus(statusMap, SchedulerStatus.CANCELLED);
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.add(CommonConstants.MESSAGE_HEADER, SchedulerStatus.CANCELLATION_FAILED.name());
            return new ResponseEntity<>(statusMap, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "API to  pause scheduled task/job(s) ", nickname = "pauseScheduledJobs")
    @RequestMapping(value = "/pause/{jobIds}", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Map<String, ScheduledTaskStatus>> pauseScheduledJobs(
            @PathVariable("jobIds") List<String> jobIds) {
        Map<String, ScheduledTaskStatus> statusMap = scheduler.pauseScheduledJobs(jobIds);
        if (null != statusMap && !(statusMap.isEmpty())) {
            return verifyJobsStatus(statusMap, SchedulerStatus.PAUSED);
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.add(CommonConstants.MESSAGE_HEADER, SchedulerStatus.PAUSE_FAILED.name());
            return new ResponseEntity<>(statusMap, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "API to  resume scheduled task/job(s) ", nickname = "resumeScheduledJobs")
    @RequestMapping(value = "/resume/{jobIds}", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Map<String, ScheduledTaskStatus>> resumeScheduledJobs(
            @PathVariable("jobIds") List<String> jobIds) {
        Map<String, ScheduledTaskStatus> statusMap = scheduler.resumeJobs(jobIds);
        if (null != statusMap && !(statusMap.isEmpty())) {
            return verifyJobsStatus(statusMap, SchedulerStatus.SCHEDULED);
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.add(CommonConstants.MESSAGE_HEADER, SchedulerStatus.RESUME_FAILED.name());
            return new ResponseEntity<>(statusMap, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "API to  cancel all the scheduled jobs ", nickname = "cancelAllScheduledJobs")
    @RequestMapping(value = "/cancel/all-jobs", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Map<String, ScheduledTaskStatus>> cancelAllScheduledJobs() {
        Map<String, ScheduledTaskStatus> statusMap;
        try {
            statusMap = scheduler.cancelAllScheduledJobs();
        } catch (MangleException e) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(CommonConstants.MESSAGE_HEADER, e.getMessage());
            return new ResponseEntity<>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (null != statusMap && !(statusMap.isEmpty())) {
            return verifyJobsStatus(statusMap, SchedulerStatus.CANCELLED);
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.add(CommonConstants.MESSAGE_HEADER, SchedulerStatus.CANCELLATION_FAILED.name());
            return new ResponseEntity<>(statusMap, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "API to delete scheduled jobs from Mangle", nickname = "deleteTasks")
    @RequestMapping(value = "", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<Map<String, DeleteSchedulerResponse>> deleteScheduledJob(
            @RequestParam("taskIds") List<String> taskIds,
            @RequestParam(name = "delete-associated-tasks", required = false) boolean isDeleteTasks)
            throws MangleException {
        Map<String, DeleteSchedulerResponse> resultMap = scheduler.deleteScheduledJobs(taskIds, isDeleteTasks);
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get all Scheduled jobs in Mangle with a Filter on Scheduler Status", nickname = "getAllScheduledJobs")
    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<SchedulerSpec>> getAllScheduledJobs(
            @RequestParam(name = "status", required = false) SchedulerStatus status) {
        List<SchedulerSpec> listOfDAOs;
        HttpHeaders headers = new HttpHeaders();
        if (status != null) {
            listOfDAOs = scheduler.getAllScheduledJobs(status);
            headers.add(CommonConstants.MESSAGE_HEADER,
                    "Successfully got all the jobs with status " + status.name());
        } else {
            listOfDAOs = scheduler.getAllScheduledJobs();
        }

        return new ResponseEntity<>(listOfDAOs, headers, HttpStatus.OK);
    }

    private ResponseEntity<Map<String, ScheduledTaskStatus>> verifyJobsStatus(
            Map<String, ScheduledTaskStatus> statusMap, SchedulerStatus status) {
        for (ScheduledTaskStatus statusMessages : statusMap.values()) {
            if (!(statusMessages.getStatus().equals(status))) {

                HttpHeaders headers = new HttpHeaders();
                headers.add(CommonConstants.MESSAGE_HEADER,
                        "One or More jobIds not " + status.name() + " .. Please verify status map in the response ");
                return new ResponseEntity<>(statusMap, headers, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, "Jobs " + status.name() + " successfully");
        return new ResponseEntity<>(statusMap, headers, HttpStatus.OK);
    }
}
