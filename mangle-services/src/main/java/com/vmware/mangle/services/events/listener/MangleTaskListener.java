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

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.services.events.task.TaskCompletedEvent;
import com.vmware.mangle.services.events.task.TaskCreatedEvent;
import com.vmware.mangle.services.events.task.TaskModifiedEvent;
import com.vmware.mangle.services.hazelcast.HazelcastTaskCache;
import com.vmware.mangle.services.helpers.MetricProviderHelper;
import com.vmware.mangle.task.framework.events.TaskSubstageEvent;
import com.vmware.mangle.utils.PopulateFaultEventData;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.CustomErrorMessage;
import com.vmware.mangle.utils.helpers.notifiers.Notifier;

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

    @EventListener
    public void handleTaskModifiedEvent(TaskModifiedEvent event) {
        Task task = event.getTask();
        String taskId = task.getId();
        log.debug("Listening to the event trigger by the modification of task status of task {} to {}" + taskId,
                task.getTaskStatus());
        if (!task.isScheduledTask()) {
            if (task.getTaskStatus() == TaskStatus.FAILED || task.getTaskStatus() == TaskStatus.COMPLETED) {
                mapService.deleteFromTaskCache(taskId);
            } else {
                mapService.updateTaskCache(taskId, task.getTaskStatus().name());
            }
        }
    }

    @EventListener
    public void handleTaskCreatedEvent(TaskCreatedEvent event) {
        Task task = event.getTask();
        String taskId = task.getId();
        log.debug("Listening to the event trigger by the Creation of task status of task {} to {}" + taskId,
                task.getTaskStatus());
        mapService.addTaskToCache(taskId, task.getTaskStatus().name());
    }

    @EventListener
    public void handleTaskSubstageEvent(TaskSubstageEvent event) {
        Task task = event.getTask();
        String taskId = task.getId();
        log.debug("Listening to the event trigger by the modification of task status of task {} to {}" + taskId,
                task.getTaskStatus());
        try {
            taskService.addOrUpdateTask(task);
        } catch (MangleException e) {
            log.error(customErrorMessage.getErrorMessage(e), e);
        }
    }

    @EventListener
    public void handleTaskCompletedEvent(TaskCompletedEvent event) {
        Task task = event.getTask();
        log.debug("TaskCreatedEvent", "Created Task: " + task.getClass().getName() + " With Id: " + task.getId());
        if (task.getTaskData() instanceof CommandExecutionFaultSpec
                || task.getTaskData() instanceof K8SFaultTriggerSpec) {
            PopulateFaultEventData populateFaultEventData = new PopulateFaultEventData(task);
            FaultEventSpec faultEventInfo = populateFaultEventData.getFaultEventSpec();
            if (faultEventInfo == null) {
                log.error(" We don't have the valid data to send the event. Can't send the event");
                return;
            }
            log.debug("TaskCompleted Event is generated and here are the details: " + faultEventInfo.toString());
            Notifier activeNotifier = metricProvider.getActiveNotificationProvider();
            if (activeNotifier == null) {
                log.error(
                        "We can't find an active metric provider. Please check if the metric providers are created and marked as Active");
                log.error("Cannot send the events to Metric Provider");
                return;
            }
            activeNotifier.sendEvent(faultEventInfo);
        }
    }
}
