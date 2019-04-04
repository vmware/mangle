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

import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.events.schedule.ScheduleCreatedEvent;
import com.vmware.mangle.services.events.schedule.ScheduleUpdatedEvent;
import com.vmware.mangle.services.hazelcast.HazelcastTaskCache;

/**
 *
 *
 * @author chetanc
 */
@Component
@Log4j2
public class ScheduleEventListener {

    @Autowired
    private HazelcastTaskCache taskCache;

    @EventListener
    public void handleSchedulerUpdatedEvent(ScheduleUpdatedEvent scheduleUpdatedEvent) {
        log.debug("Scheduler update event listener is triggered");
        if (scheduleUpdatedEvent.getScheduleStatus() == SchedulerStatus.CANCELLED
                || scheduleUpdatedEvent.getScheduleStatus() == SchedulerStatus.PAUSED
                || scheduleUpdatedEvent.getScheduleStatus() == SchedulerStatus.FINISHED) {
            taskCache.deleteFromTaskCache(scheduleUpdatedEvent.getScheduleID());
        } else {
            taskCache.updateTaskCache(scheduleUpdatedEvent.getScheduleID(),
                    scheduleUpdatedEvent.getScheduleStatus().name());
        }
    }

    @EventListener
    public void handleSchedulerCreatedEvent(ScheduleCreatedEvent scheduleCreatedEvent) {
        log.debug("Scheduler create event listener is triggered");
        taskCache.updateTaskCache(scheduleCreatedEvent.getScheduleID(),
                scheduleCreatedEvent.getScheduleStatus().name());
    }

}
