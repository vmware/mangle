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

import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.services.events.task.ResiliencyScoreTaskCreatedEvent;
import com.vmware.mangle.services.hazelcast.resiliencyscore.HazelcastResiliencyScoreTaskCache;

/**
 * @author dbhat
 */
@Component
@Log4j2
public class ResiliencyScoreTaskListener {

    @Autowired
    private HazelcastResiliencyScoreTaskCache mapService;

    @EventListener
    public void handleTaskCreatedEvent(ResiliencyScoreTaskCreatedEvent event) {
        ResiliencyScoreTask task = event.getTask();
        String taskId = task.getId();
        log.debug("Listening to the event trigger by the Creation of task status of task {} to {}", taskId,
                task.getTaskStatus());
        mapService.addTaskToCache(taskId, task.getTaskStatus().name());
    }
}
