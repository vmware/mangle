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

package com.vmware.mangle.services.application.listener;

import static com.vmware.mangle.utils.constants.URLConstants.HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.Member;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.hazelcast.HazelcastClusterConfig;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.ClusterConfigService;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.hazelcast.HazelcastTaskCache;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.URLConstants;

/**
 * @author ashrimali
 *
 */
@Component
@Log4j2
public class CustomApplicationListener implements ApplicationListener<ContextRefreshedEvent>, HazelcastInstanceAware {


    private static int ONE_MINUTE_IN_MILLIS = 60000;

    private ClusterConfigService configService;
    private HazelcastInstance hazelcastInstance;
    private TaskService taskService;
    private HazelcastTaskCache mapService;
    private SchedulerService schedulerService;

    @Autowired
    public CustomApplicationListener(ClusterConfigService configService, TaskService taskService,
            HazelcastTaskCache mapService, SchedulerService schedulerService) {
        this.configService = configService;
        this.taskService = taskService;
        this.mapService = mapService;
        this.schedulerService = schedulerService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        updateClusterConfigObject();
        if (null != hazelcastInstance && hazelcastInstance.getCluster().getMembers().size() == 1) {
            log.info("Mangle cluster has only one node, will be triggering scheduled tasks and in-progress tasks");
            scheduleMisfiredJobs();
            retriggerInProgressTasks();
        }
        if (hazelcastInstance != null) {
            hazelcastInstance.getCluster().getLocalMember().setStringAttribute(
                    HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE, URLConstants.getMangleNodeCurrentStatus().name());
        }
    }

    private void scheduleMisfiredJobs() {
        log.debug("Triggering misfired scheduled tasks");
        List<SchedulerSpec> scheduledJobsList = schedulerService.getAllScheduledJobByStatus(SchedulerStatus.SCHEDULED);
        if (CollectionUtils.isEmpty(scheduledJobsList)) {
            log.info("No jobs found to be rescheduled");
            return;
        }
        for (SchedulerSpec scheduledJob : scheduledJobsList) {
            mapService.addTaskToCache(scheduledJob.getId(), scheduledJob.getStatus().name());
        }
    }

    private void retriggerInProgressTasks() {
        List<Task<TaskSpec>> inprogressTasks = taskService.getInProgressTasks();
        for (Task<TaskSpec> task : inprogressTasks) {
            try {
                if (!task.isScheduledTask()) {

                    TaskTrigger trigger = task.getTriggers().peek();
                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
                    Date startTime = sdf.parse(trigger.getStartTime());
                    if ((System.currentTimeMillis() - startTime.getTime())
                            / ONE_MINUTE_IN_MILLIS < URLConstants.RETRIGGER_THRESHOLD_TIME_IN_MINS) {
                        mapService.addTaskToCache(task.getId(), task.getTaskStatus().name());
                    }

                } else {
                    task.getTriggers().peek().setTaskStatus(TaskStatus.FAILED);
                    task.getTriggers().peek().setTaskFailureReason("Cluster failure");
                    taskService.addOrUpdateTask(task);
                }
            } catch (Exception e) {
                log.error("Re-Triggering of the in-progress faults failed with an exception: " + e);
            }
        }
    }

    private void updateClusterConfigObject() {
        log.info("Updating mangle cluster configuration");
        HazelcastClusterConfig config = configService.getClusterConfiguration();
        Set<Member> clusterActiveMembers = hazelcastInstance.getCluster().getMembers();

        Set<String> activeMembers =
                clusterActiveMembers.stream().map(member -> member.getAddress().getHost()).collect(Collectors.toSet());

        config.setMembers(activeMembers);
        configService.updateClusterConfiguration(config);

    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }
}
