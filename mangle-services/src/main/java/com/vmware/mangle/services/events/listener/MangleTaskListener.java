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

import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.services.NotifierService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.events.task.TaskCompletedEvent;
import com.vmware.mangle.services.events.task.TaskCreatedEvent;
import com.vmware.mangle.services.events.task.TaskModifiedEvent;
import com.vmware.mangle.services.hazelcast.HazelcastTaskCache;
import com.vmware.mangle.services.helpers.MetricProviderHelper;
import com.vmware.mangle.task.framework.events.TaskSubstageEvent;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.CustomErrorMessage;

/**
 *
 *
 * @author chetanc, dbhat
 */
@Component
@Log4j2
public class MangleTaskListener {

    @Autowired
    private HazelcastTaskCache mapService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CustomErrorMessage customErrorMessage;

    @Autowired
    private MetricProviderHelper metricProvider;

    @Autowired
    private NotifierService notifierService;

    @SuppressWarnings("rawtypes")
    @EventListener
    public void handleTaskModifiedEvent(TaskModifiedEvent event) {
        Task task = event.getTask();
        String taskId = task.getId();
        log.debug("Listening to the event trigger by the modification of task status of task {} to {}", taskId,
                task.getTaskStatus());
        if (!task.isScheduledTask()) {
            if (task.getTaskStatus() == TaskStatus.FAILED || task.getTaskStatus() == TaskStatus.COMPLETED) {
                mapService.deleteFromTaskCache(taskId);
            } else {
                mapService.updateTaskCache(taskId, task.getTaskStatus().name());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @EventListener
    public void handleTaskCreatedEvent(TaskCreatedEvent event) {
        Task task = event.getTask();
        String taskId = task.getId();
        log.debug("Listening to the event trigger by the Creation of task status of task {} to {}", taskId,
                task.getTaskStatus());
        mapService.addTaskToCache(taskId, task.getTaskStatus().name());
    }

    @SuppressWarnings("rawtypes")
    @EventListener
    public void handleTaskSubstageEvent(TaskSubstageEvent event) {
        Task task = event.getTask();
        String taskId = task.getId();
        log.debug("Listening to the event trigger by the modification of task status of task {} to {}", taskId,
                task.getTaskStatus());
        try {
            taskService.addOrUpdateTask(task);
        } catch (MangleException e) {
            log.error(customErrorMessage.getErrorMessage(e), e);
        }
    }

    @SuppressWarnings("rawtypes")
    @EventListener
    public void handleTaskCompletedEvent(TaskCompletedEvent event) {
        Task task = event.getTask();
        //Created thread because of Send notification to metric provider as well as slack parallelly.
        new Thread(() -> notifierService.sendNotification(task), Thread.currentThread().getName()).start();
        metricProvider.sendFaultEvent(task);
    }
}
