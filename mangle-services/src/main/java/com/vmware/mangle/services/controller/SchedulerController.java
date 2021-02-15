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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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

import com.vmware.mangle.cassandra.model.scheduler.SchedulerRequestStatus;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.model.enums.SchedulerJobType;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.services.scheduler.Scheduler;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

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
    @PostMapping(value = "/cancel/{jobIds}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<SchedulerRequestStatus>> cancelScheduledJobs(
            @PathVariable("jobIds") List<String> jobIds) throws MangleException {
        log.info("Received request to cancel schedules with the schedule id: {}", jobIds.toString());
        Set<String> processedJobs = scheduler.cancelScheduledJobs(jobIds);

        Resource<SchedulerRequestStatus> requestStatusResource = new Resource<>(new SchedulerRequestStatus(
                String.format("Received request to cancel the schedules %s", processedJobs.toString())));
        requestStatusResource.add(getSelfLink(), cancelAllScheduledJobsHateoasLink(), pauseScheduledJobsHateoasLink(),
                resumeScheduledJobsHateoasLink(), modifyScheduledJobHateoasLink(), cancelScheduledJobsHateoasLink(),
                deleteScheduledJobHateoasLink());
        return new ResponseEntity<>(requestStatusResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to  pause scheduled task/job(s) ", nickname = "pauseScheduledJobs")
    @PostMapping(value = "/pause/{jobIds}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<SchedulerRequestStatus>> pauseScheduledJobs(
            @PathVariable("jobIds") List<String> jobIds) throws MangleException {
        log.info("Received request to pause schedules with the schedule id: {}", jobIds.toString());
        Set<String> processedJobs = scheduler.pauseScheduledJobs(jobIds);

        Resource<SchedulerRequestStatus> requestStatusResource = new Resource<>(new SchedulerRequestStatus(
                String.format("Received request to pause the schedules %s", processedJobs.toString())));
        requestStatusResource.add(getSelfLink(), cancelAllScheduledJobsHateoasLink(), resumeScheduledJobsHateoasLink(),
                modifyScheduledJobHateoasLink(), deleteScheduledJobHateoasLink(), cancelScheduledJobsHateoasLink());
        return new ResponseEntity<>(requestStatusResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to  resume scheduled task/job(s) ", nickname = "resumeScheduledJobs")
    @PostMapping(value = "/resume/{jobIds}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<SchedulerRequestStatus>> resumeScheduledJobs(
            @PathVariable("jobIds") List<String> jobIds) throws MangleException {
        log.info("Received request to resume schedules with the schedule id: {}", jobIds.toString());
        Set<String> processedJobs = scheduler.resumeJobs(jobIds);

        Resource<SchedulerRequestStatus> requestStatusResource = new Resource<>(new SchedulerRequestStatus(
                String.format("Received request to resume the schedules %s", processedJobs.toString())));
        requestStatusResource.add(getSelfLink(), cancelAllScheduledJobsHateoasLink(), pauseScheduledJobsHateoasLink(),
                modifyScheduledJobHateoasLink(), cancelScheduledJobsHateoasLink(), deleteScheduledJobHateoasLink());
        return new ResponseEntity<>(requestStatusResource, HttpStatus.OK);
    }


    @ApiOperation(value = "API to modify scheduled task/job", nickname = "modifyScheduledJob")
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<SchedulerRequestStatus>> modifyScheduledJob(
            @Validated @RequestBody SchedulerSpec schedulerSpec) throws MangleException {
        log.info("Received request to modify schedules with the schedule id: {}", schedulerSpec.getId());
        if (schedulerSpec.getJobType().equals(SchedulerJobType.CRON) && null != schedulerSpec.getCronExpression()
                || schedulerSpec.getJobType().equals(SchedulerJobType.SIMPLE)
                        && null != schedulerSpec.getScheduledTime()) {
            scheduler.modifyJob(schedulerSpec);

            Resource<SchedulerRequestStatus> requestStatusResource = new Resource<>(new SchedulerRequestStatus(
                    String.format("Received request to modify the schedules %s", schedulerSpec.getId())));
            requestStatusResource.add(getSelfLink(), cancelAllScheduledJobsHateoasLink(),
                    pauseScheduledJobsHateoasLink(), resumeScheduledJobsHateoasLink(), cancelScheduledJobsHateoasLink(),
                    cancelAllScheduledJobsHateoasLink(), deleteScheduledJobHateoasLink());
            return new ResponseEntity<>(requestStatusResource, HttpStatus.OK);
        } else {
            throw new MangleException(ErrorConstants.INVALID_SCHEDULE_INPUTS, ErrorCode.INVALID_SCHEDULE_INPUTS,
                    schedulerSpec.getId());
        }
    }

    @ApiOperation(value = "API to  cancel all the scheduled jobs ", nickname = "cancelAllScheduledJobs")
    @PostMapping(value = "/cancel/all-jobs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<SchedulerRequestStatus>> cancelAllScheduledJobs() throws MangleException {
        log.info("Received request to cancel all the schedules");
        scheduler.cancelAllScheduledJobs();

        Resource<SchedulerRequestStatus> requestStatusResource =
                new Resource<>(new SchedulerRequestStatus("Received request to cancel all the schedules"));
        requestStatusResource.add(getSelfLink(), cancelAllScheduledJobsHateoasLink(), pauseScheduledJobsHateoasLink(),
                resumeScheduledJobsHateoasLink(), modifyScheduledJobHateoasLink(), cancelScheduledJobsHateoasLink(),
                deleteScheduledJobHateoasLink());
        return new ResponseEntity<>(requestStatusResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to delete scheduled jobs from Mangle", nickname = "deleteTasks")
    @DeleteMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<SchedulerRequestStatus>> deleteScheduledJob(
            @RequestParam("jobIds") List<String> jobIds,
            @RequestParam(value = "delete-associated-tasks", required = false) boolean deleteAssociatedTasks)
            throws MangleException {
        log.info("Received request to delete schedules: {}", jobIds.toString());
        Set<String> processedJobs = scheduler.deleteScheduledJobs(jobIds, deleteAssociatedTasks);

        Resource<SchedulerRequestStatus> requestStatusResource = new Resource<>(new SchedulerRequestStatus(
                String.format("Received request to delete the schedules %s", processedJobs.toString())));
        requestStatusResource.add(getSelfLink(), cancelAllScheduledJobsHateoasLink(), pauseScheduledJobsHateoasLink(),
                resumeScheduledJobsHateoasLink(), modifyScheduledJobHateoasLink(), cancelScheduledJobsHateoasLink());
        return new ResponseEntity<>(requestStatusResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get all Scheduled jobs in Mangle with a Filter on Scheduler Status", nickname = "getAllScheduledJobs")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<SchedulerSpec>> getAllScheduledJobs(
            @RequestParam(name = "status", required = false) SchedulerStatus status) throws MangleException {
        log.info("Received request to retrieve all the schedules");
        List<SchedulerSpec> listOfDAOs;
        HttpHeaders headers = new HttpHeaders();
        if (status != null) {
            listOfDAOs = scheduler.getAllScheduledJobs(status);
            headers.add(CommonConstants.MESSAGE_HEADER, "Successfully got all the jobs with status " + status.name());
        } else {
            listOfDAOs = scheduler.getAllScheduledJobs();
        }

        Resources<SchedulerSpec> requestStatusResources = new Resources<>(listOfDAOs);
        requestStatusResources.add(getSelfLink(), cancelAllScheduledJobsHateoasLink(), pauseScheduledJobsHateoasLink(),
                resumeScheduledJobsHateoasLink(), modifyScheduledJobHateoasLink(), cancelScheduledJobsHateoasLink(),
                deleteScheduledJobHateoasLink());
        return new ResponseEntity<>(requestStatusResources, HttpStatus.OK);
    }

    public Link getSelfLink() {
        return new Link(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri().toASCIIString())
                .withSelfRel();
    }

    public Link cancelScheduledJobsHateoasLink() throws MangleException {
        return linkTo(methodOn(SchedulerController.class).cancelScheduledJobs(Collections.emptyList()))
                .withRel("CANCEL");
    }

    public Link pauseScheduledJobsHateoasLink() throws MangleException {
        return linkTo(methodOn(SchedulerController.class).pauseScheduledJobs(Collections.emptyList())).withRel("PAUSE");
    }

    public Link resumeScheduledJobsHateoasLink() throws MangleException {
        return linkTo(methodOn(SchedulerController.class).resumeScheduledJobs(Collections.emptyList()))
                .withRel("RESUME");
    }

    public Link modifyScheduledJobHateoasLink() throws MangleException {
        return linkTo(methodOn(SchedulerController.class).modifyScheduledJob(new SchedulerSpec())).withRel("MODIFY");
    }

    public Link cancelAllScheduledJobsHateoasLink() throws MangleException {
        return linkTo(methodOn(SchedulerController.class).cancelAllScheduledJobs()).withRel("CANCEL-ALL");
    }

    public Link deleteScheduledJobHateoasLink() throws MangleException {
        return linkTo(methodOn(SchedulerController.class).deleteScheduledJob(Collections.emptyList(), false))
                .withRel("DELETE");
    }
}
