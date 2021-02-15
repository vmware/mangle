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

package com.vmware.mangle.services.hazelcast.resiliencyscore;

import static com.vmware.mangle.utils.constants.HazelcastConstants.HAZELCAST_RESILIENCY_SCORE_MAP;

import java.util.Set;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.model.resiliencyscore.ResiliencyScoreProperties;
import com.vmware.mangle.services.helpers.ResiliencyScoreHelper;
import com.vmware.mangle.services.helpers.ResiliencyScoreTaskHelper;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreService;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreTaskExecutor;
import com.vmware.mangle.services.scheduler.Scheduler;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * @author dbhat
 */
@Service
@Log4j2
@DependsOn("hazelcastInstance")
public class HazelcastResiliencyScoreService {
    @Autowired
    private ResiliencyScoreHelper resiliencyScoreHelper;
    @Autowired
    private ResiliencyScoreService resiliencyScoreService;
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private ResiliencyScoreTaskExecutor<Task<? extends TaskSpec>> resiliencyScoreTaskExecutor;
    @Autowired
    private ResiliencyScoreTaskHelper resiliencyScoreTaskHelper;
    @Autowired
    private HazelcastInstance hazelcastInstance;

    public void triggerTask(String taskId) throws MangleException {
        log.info("****** Checking the Resiliency Score task with id: ****** " + taskId);
        IMap<String, Set<String>> taskMap = hazelcastInstance.getMap(HAZELCAST_RESILIENCY_SCORE_MAP);
        ResiliencyScoreTask resiliencyScoreTask = resiliencyScoreService.getTaskById(taskId);
        if (!resiliencyScoreTask.isScheduledTask() && isTaskTriggered(resiliencyScoreTask.getTaskStatus())) {
            log.debug(" No action required as the resiliency score calculation task is already running.");
            taskMap.remove(taskId);
            return;
        }
        ResiliencyScoreProperties taskProperties = initResiliencyScoreTaskSpec();
        taskProperties.setTaskId(taskId);
        resiliencyScoreTaskExecutor.submitTask(resiliencyScoreTaskHelper.init(resiliencyScoreTask));
    }

    void cleanUpTask(String taskId, String taskStatus) throws MangleException {
        log.debug("cleanup Task triggered for taskId :" + taskId + " with status : " + taskStatus);
        ResiliencyScoreTask resiliencyScoreTask = resiliencyScoreService.getTaskById(taskId);
        if (resiliencyScoreTask.isScheduledTask()) {
            log.debug("Triggering cleanup for the schedule job {}", taskId);
            scheduler.removeScheduleFromCurrentNode(taskId, taskStatus);
        }
    }

    private ResiliencyScoreProperties initResiliencyScoreTaskSpec() {
        return resiliencyScoreHelper.getResiliencyScoreTaskSpec();
    }

    private boolean isTaskTriggered(TaskStatus taskStatus) {
        return (taskStatus == TaskStatus.FAILED || taskStatus == TaskStatus.IN_PROGRESS
                || taskStatus == TaskStatus.COMPLETED);
    }
}
