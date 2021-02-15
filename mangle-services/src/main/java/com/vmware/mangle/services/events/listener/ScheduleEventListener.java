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

package com.vmware.mangle.services.events.listener;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.events.schedule.ScheduleCreatedEvent;
import com.vmware.mangle.services.events.schedule.ScheduleUpdatedEvent;
import com.vmware.mangle.services.hazelcast.HazelcastTaskCache;
import com.vmware.mangle.services.hazelcast.resiliencyscore.HazelcastResiliencyScoreTaskCache;
import com.vmware.mangle.services.helpers.TaskHelper;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 *
 *
 * @author chetanc, dbhat
 */
@Component
@Log4j2
public class ScheduleEventListener {

    @Autowired
    private HazelcastTaskCache taskCache;
    @Autowired
    private TaskHelper taskHelper;
    @Autowired
    private HazelcastResiliencyScoreTaskCache resiliencyScoreTaskCache;

    @EventListener
    public void handleSchedulerUpdatedEvent(ScheduleUpdatedEvent scheduleUpdatedEvent) {
        log.debug("Scheduler update event listener is triggered");
        if (scheduleUpdatedEvent.getScheduleStatus().equals(SchedulerStatus.CANCELLED.name())
                || scheduleUpdatedEvent.getScheduleStatus().equals(SchedulerStatus.PAUSED.name())
                || scheduleUpdatedEvent.getScheduleStatus().equals(SchedulerStatus.FINISHED.name())) {
            deleteFromTaskCache(scheduleUpdatedEvent);
        } else {
            updateTaskCache(scheduleUpdatedEvent);
        }
    }

    @EventListener
    public void handleSchedulerCreatedEvent(ScheduleCreatedEvent scheduleCreatedEvent) {
        log.debug("Scheduler create event listener is triggered");
        addTaskToCache(scheduleCreatedEvent);
    }

    private void deleteFromTaskCache(ScheduleUpdatedEvent scheduleUpdatedEvent) {
        try {
            if (taskHelper.getTaskType(scheduleUpdatedEvent.getScheduleID()) == TaskType.RESILIENCY_SCORE) {
                resiliencyScoreTaskCache.deleteFromTaskCache(scheduleUpdatedEvent.getScheduleID());
            } else {
                taskCache.deleteFromTaskCache(scheduleUpdatedEvent.getScheduleID());
            }
        } catch (MangleException mangleException) {
            log.error(mangleException.getMessage());
        }
    }

    private void updateTaskCache(ScheduleUpdatedEvent scheduleUpdatedEvent) {
        try {
            if (taskHelper.getTaskType(scheduleUpdatedEvent.getScheduleID()) == TaskType.RESILIENCY_SCORE) {
                resiliencyScoreTaskCache.updateHazelcastTaskCache(scheduleUpdatedEvent.getScheduleID(),
                        scheduleUpdatedEvent.getScheduleStatus());
            } else {
                taskCache.updateTaskCache(scheduleUpdatedEvent.getScheduleID(),
                        scheduleUpdatedEvent.getScheduleStatus());
            }

        } catch (MangleException mangleException) {
            log.error(mangleException.getMessage());
        }
    }

    private void addTaskToCache(ScheduleCreatedEvent scheduleCreatedEvent) {
        try {
            if (taskHelper.getTaskType(scheduleCreatedEvent.getScheduleID()) == TaskType.RESILIENCY_SCORE) {
                resiliencyScoreTaskCache.addTaskToCache(scheduleCreatedEvent.getScheduleID(),
                        scheduleCreatedEvent.getScheduleStatus().name());
            } else {
                taskCache.addTaskToCache(scheduleCreatedEvent.getScheduleID(),
                        scheduleCreatedEvent.getScheduleStatus().name());
            }
        } catch (MangleException mangleException) {
            log.error(mangleException.getMessage());
        }
    }

}
