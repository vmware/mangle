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
import java.util.Set;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
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

import com.vmware.mangle.cassandra.model.scheduler.SchedulerRequestStatus;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.services.scheduler.Scheduler;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Mangle Scheduling Services
 *
 * @author bkaranam
 * @author ashrimali
 */
@Log4j2
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
    public ResponseEntity<SchedulerRequestStatus> cancelScheduledJobs(@PathVariable("jobIds") List<String> jobIds)
            throws MangleException {
        log.info("Received request to cancel schedules with the schedule id: {}", jobIds.toString());
        Set<String> processedJobs = scheduler.cancelScheduledJobs(jobIds);
        return new ResponseEntity<>(
                new SchedulerRequestStatus(
                        String.format("Received request to cancel the schedules %s", processedJobs.toString())),
                HttpStatus.OK);
    }

    @ApiOperation(value = "API to  pause scheduled task/job(s) ", nickname = "pauseScheduledJobs")
    @RequestMapping(value = "/pause/{jobIds}", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<SchedulerRequestStatus> pauseScheduledJobs(@PathVariable("jobIds") List<String> jobIds)
            throws MangleException {
        log.info("Received request to pause schedules with the schedule id: {}", jobIds.toString());
        Set<String> processedJobs = scheduler.pauseScheduledJobs(jobIds);
        return new ResponseEntity<>(
                new SchedulerRequestStatus(
                        String.format("Received request to pause the schedules %s", processedJobs.toString())),
                HttpStatus.OK);
    }

    @ApiOperation(value = "API to  resume scheduled task/job(s) ", nickname = "resumeScheduledJobs")
    @RequestMapping(value = "/resume/{jobIds}", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<SchedulerRequestStatus> resumeScheduledJobs(@PathVariable("jobIds") List<String> jobIds)
            throws MangleException {
        log.info("Received request to resume schedules with the schedule id: {}", jobIds.toString());
        Set<String> processedJobs = scheduler.resumeJobs(jobIds);
        return new ResponseEntity<>(
                new SchedulerRequestStatus(
                        String.format("Received request to resume the schedules %s", processedJobs.toString())),
                HttpStatus.OK);
    }

    @ApiOperation(value = "API to  cancel all the scheduled jobs ", nickname = "cancelAllScheduledJobs")
    @RequestMapping(value = "/cancel/all-jobs", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<SchedulerRequestStatus> cancelAllScheduledJobs() throws MangleException {
        log.info("Received request to cancel all the schedules");
        scheduler.cancelAllScheduledJobs();
        return new ResponseEntity<>(new SchedulerRequestStatus("Received request to cancel all the schedules"),
                HttpStatus.OK);
    }

    @ApiOperation(value = "API to delete scheduled jobs from Mangle", nickname = "deleteTasks")
    @RequestMapping(value = "", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<SchedulerRequestStatus> deleteScheduledJob(@RequestParam("jobIds") List<String> jobIds)
            throws MangleException {
        log.info("Received request to delete schedules: {}", jobIds.toString());
        Set<String> processedJobs = scheduler.deleteScheduledJobs(jobIds);
        return new ResponseEntity<>(
                new SchedulerRequestStatus(
                        String.format("Received request to delete the schedules %s", processedJobs.toString())),
                HttpStatus.OK);
    }

    @ApiOperation(value = "API to get all Scheduled jobs in Mangle with a Filter on Scheduler Status", nickname = "getAllScheduledJobs")
    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<SchedulerSpec>> getAllScheduledJobs(
            @RequestParam(name = "status", required = false) SchedulerStatus status) {
        log.info("Received request to retrieve all the schedules");
        List<SchedulerSpec> listOfDAOs;
        HttpHeaders headers = new HttpHeaders();
        if (status != null) {
            listOfDAOs = scheduler.getAllScheduledJobs(status);
            headers.add(CommonConstants.MESSAGE_HEADER, "Successfully got all the jobs with status " + status.name());
        } else {
            listOfDAOs = scheduler.getAllScheduledJobs();
        }

        return new ResponseEntity<>(listOfDAOs, headers, HttpStatus.OK);
    }
}
