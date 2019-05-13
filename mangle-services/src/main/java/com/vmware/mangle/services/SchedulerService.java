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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.repository.SchedulerRepository;

/**
 * @author ashrimali
 *
 */
@Service
@Log4j2
public class SchedulerService {
    @Autowired
    private SchedulerRepository schedulerRepository;

    public SchedulerSpec updateSchedulerStatus(String id, SchedulerStatus status) {
        SchedulerSpec schedulerSpec = getSchedulerDetailsById(id);
        schedulerSpec.setStatus(status);
        return addOrUpdateSchedulerDetails(schedulerSpec);
    }

    public SchedulerSpec addOrUpdateSchedulerDetails(SchedulerSpec schedulerSpec) {
        log.info("Adding/Updating SchedulerSpec");
        SchedulerSpec persistedSchedulerSpec = schedulerRepository.save(schedulerSpec);
        if (null != getSchedulerDetailsById(persistedSchedulerSpec.getId())) {
            return persistedSchedulerSpec;
        }
        return null;
    }

    public List<SchedulerSpec> getAllSchedulerDetails() {
        log.info("Retrieving all Mangle Scheduled job details...");
        return schedulerRepository.findAll();
    }

    public SchedulerSpec getSchedulerDetailsById(String jobId) {
        log.info("Retrieving Scheduler details for Job = " + jobId);
        if (StringUtils.isEmpty(jobId)) {
            log.error("jobId should not be empty or null");
            return null;
        } else {
            Optional<SchedulerSpec> schedulerSpec = schedulerRepository.findById(jobId);
            return schedulerSpec.isPresent() ? schedulerSpec.get() : null;
        }
    }

    public SchedulerSpec getScheduledJobByIdandStatus(String jobId, SchedulerStatus status) {
        log.info("Retrieving Scheduler details for Job = " + jobId);
        if (StringUtils.isEmpty(jobId)) {
            log.error("jobId should not be empty or null");
            return null;
        } else {
            Optional<SchedulerSpec> schedulerSpec = schedulerRepository.findByIdAndStatus(jobId, status);
            return schedulerSpec.isPresent() ? schedulerSpec.get() : null;
        }
    }

    public List<SchedulerSpec> getAllScheduledJobByStatus(SchedulerStatus status) {
        log.info("Retrieving MangleScheduled jobs with status: " + status);
        if (null == status) {
            log.error("status should not be empty or null");
            return new ArrayList<>();
        } else {
            Optional<List<SchedulerSpec>> schedulerSpec = schedulerRepository.findByStatus(status);
            return schedulerSpec.isPresent() ? schedulerSpec.get() : null;
        }
    }

    /**
     * Gives the list of active schedules(schedules that are Scheduled/Paused/Initializing).
     *
     * @return list of schedule ids of the active schedules
     */
    public List<String> getActiveScheduleJobs() {
        List<SchedulerSpec> specs = schedulerRepository.findAll();
        return specs.stream().filter(schedulerSpec -> isActiveSchedule(schedulerSpec.getStatus()))
                .map(schedulerSpec -> schedulerSpec.getId()).collect(Collectors.toList());
    }

    /**
     * @param status
     * @return returns true if the status is of active schedule
     */
    private boolean isActiveSchedule(SchedulerStatus status) {
        return status.equals(SchedulerStatus.SCHEDULED) || status.equals(SchedulerStatus.PAUSED)
                || status.equals(SchedulerStatus.INITIALIZING);
    }

    public List<SchedulerSpec> getActiveSchedulesForIds(List<String> scheduleIds) {
        Set<SchedulerSpec> specs = schedulerRepository.findByIds(scheduleIds);
        return specs.stream().filter(schedulerSpec -> isActiveSchedule(schedulerSpec.getStatus()))
                .collect(Collectors.toList());
    }

    public Set<SchedulerSpec> getSchedulesForIds(List<String> scheduleIds) {
        return schedulerRepository.findByIds(scheduleIds);
    }

}
