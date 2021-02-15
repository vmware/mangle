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

package com.vmware.mangle.services.config;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.hazelcast.HazelcastClusterConfig;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.model.enums.MangleDeploymentMode;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.ClusterConfigService;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.enums.MangleQuorumStatus;
import com.vmware.mangle.services.events.schedule.ScheduleCreatedEvent;
import com.vmware.mangle.services.events.web.CustomEventPublisher;
import com.vmware.mangle.services.hazelcast.HazelcastTaskCache;
import com.vmware.mangle.services.poll.PollingService;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.HazelcastConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 *
 *
 */
@Component
@Log4j2
public class MangleBootInitializer {

    private TaskService taskService;
    private HazelcastTaskCache mapService;
    private SchedulerService schedulerService;
    private HazelcastInstance hazelcastInstance;
    private ClusterConfigService configService;

    private List<String> taskQueue = new ArrayList<>();

    private ThreadPoolTaskScheduler taskScheduler;
    private CustomEventPublisher eventPublisher;

    //@Autowired
    private PollingService<?> pollingService;

    @Autowired
    public MangleBootInitializer(TaskService taskService, HazelcastTaskCache taskCache,
            SchedulerService schedulerService, ClusterConfigService clusterConfigService,
            CustomEventPublisher eventPublisher,PollingService pollingService) {
        this.taskService = taskService;
        this.mapService = taskCache;
        this.schedulerService = schedulerService;
        this.configService = clusterConfigService;
        this.eventPublisher = eventPublisher;
        this.pollingService = pollingService;
    }


    @Autowired
    public void setTaskScheduler(ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        this.taskScheduler = threadPoolTaskScheduler;
    }

    /**
     * The method will re-trigger all the schedules and schedule the INPROGRESS tasks to be
     * triggered after 5minutes in following cases:
     *
     * 1. if the deployment mode standalone, after the tomcat is initialized
     *
     * 2. If deployment is in cluster mode, after the quorum is established, this will be triggered
     * from the master node
     */
    public void initializeApplicationTasks() {
        if (null != hazelcastInstance
                && hazelcastInstance.getCluster().getMembers().iterator().next().getAddress() == hazelcastInstance
                        .getCluster().getLocalMember().getAddress()
                && HazelcastConstants.getMangleQourumStatus() == MangleQuorumStatus.PRESENT) {
            log.info("Mangle cluster has only one node, will be triggering scheduled tasks and in-progress tasks");
            if (!pollingService.isThreadalive()) {
                pollingService.startPollingThread();
            }
            scheduleMisfiredJobs();
            retriggerInProgressTasks();
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
            eventPublisher.publishEvent(new ScheduleCreatedEvent(scheduledJob.getId(), scheduledJob.getStatus()));
        }
    }

    /**
     * Method will schedule the triggering of all the task that are identified as in-progress
     *
     * 1. If a task doesn't have triggers, it will be updated as failed task
     *
     * 2. If a task is older than 30mins, that task is also updated as a failed task
     *
     * 3. All other tasks will be added to a queue, and will be scheduled to be triggered after 5
     * mins
     *
     */
    private void retriggerInProgressTasks() {
        List<Task<TaskSpec>> inprogressTasks = taskService.getInProgressTasks();
        for (Task<TaskSpec> task : inprogressTasks) {
            try {

                if (CollectionUtils.isEmpty(task.getTriggers())) {
                    task.setTriggers(new Stack<>());
                    task.getTriggers().add(new TaskTrigger());
                    updateTaskFailed(task, TaskStatus.FAILED);
                } else if (!task.isScheduledTask()) {
                    TaskTrigger trigger = task.getTriggers().peek();
                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
                    Date startTime = sdf.parse(trigger.getStartTime());
                    if ((System.currentTimeMillis() - startTime.getTime())
                            / Constants.ONE_MINUTE_IN_MILLIS < Constants.RETRIGGER_THRESHOLD_TIME_IN_MINS) {
                        taskQueue.add(task.getId());
                    } else {
                        updateTaskFailed(task, TaskStatus.FAILED);
                    }
                } else {
                    updateTaskFailed(task, TaskStatus.TASK_SKIPPED);
                }
            } catch (Exception e) {
                log.error("Re-Triggering of the in-progress faults failed with an exception: " + e);
            }
        }

        this.taskScheduler.schedule(this::triggerTasks,
                new Date(System.currentTimeMillis() + Constants.ONE_MINUTE_IN_MILLIS * 5));


    }

    @SuppressWarnings("deprecation")
    private void updateTaskFailed(Task<TaskSpec> task, TaskStatus taskStatus) throws MangleException {
        TaskTrigger taskTrigger = task.getTriggers().peek();
        taskTrigger.setEndTime(new Date(System.currentTimeMillis()).toGMTString());
        taskTrigger.setTaskStatus(taskStatus);
        taskTrigger.setTaskFailureReason(
                String.format("Node %s removed from cluster", task.getTriggers().peek().getNode()));
        taskService.addOrUpdateTask(task);
    }


    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    /**
     * Method takes all the task in task queue and will add it into hazelcast distributed map, which
     * will then assign the ownership of the task automatically to different members present in the
     * cluster
     */
    private void triggerTasks() {
        for (String taskId : taskQueue) {
            try {
                Task<TaskSpec> task = taskService.getTaskById(taskId);
                if (task.getTaskStatus() != TaskStatus.COMPLETED || task.getTaskStatus() != TaskStatus.FAILED) {
                    mapService.addTaskToCache(taskId, task.getTaskStatus().name());
                }
            } catch (MangleException e) {
                log.error("Re-Triggering of the in-progress faults failed with an exception: " + e);
            }
        }
    }

    /**
     * Method will update the cluster configuration of the cluster with the list of the members if
     * the cluster has a quorum, only node that has joined the cluster with active quorum can update
     * the members list
     */
    public void updateClusterConfigObject() {
        log.info("Updating mangle cluster configuration");
        if (hazelcastInstance != null && HazelcastConstants.getMangleQourumStatus() == MangleQuorumStatus.PRESENT) {
            synchronized (configService) {
                HazelcastClusterConfig config = configService.getClusterConfiguration();
                Set<Member> clusterActiveMembers = hazelcastInstance.getCluster().getMembers();

                Set<String> activeMembers = clusterActiveMembers.stream().map(member -> member.getAddress().getHost())
                        .collect(Collectors.toSet());

                config.setMembers(activeMembers);
                config.setMaster(clusterActiveMembers.iterator().next().getAddress().getHost());
                if (null == config.getQuorum() || HazelcastConstants.getMangleQourum() > config.getQuorum()) {
                    config.setQuorum(HazelcastConstants.getMangleQourum());
                }
                config.setDeploymentMode(extractMangleDeploymentMode(hazelcastInstance.getConfig()
                        .getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE)));
                configService.updateClusterConfiguration(config);
            }
        }
    }

    /**
     * This method will be called when a node leaves the cluster, which may cause the current node
     * to lose the quorum
     *
     * When a node loses quorum it will remove it's entry from the list of members in db
     */
    public void removeCurrentClusterNodesFromClusterConfig() {
        if (hazelcastInstance != null && isCurrentNodeOldestMember()) {
            synchronized (configService) {
                String currentMemberAddress = hazelcastInstance.getCluster().getLocalMember().getAddress().getHost();
                HazelcastClusterConfig config = configService.getClusterConfiguration();
                Set<Member> clusterActiveMembers = hazelcastInstance.getCluster().getMembers();
                Set<String> activeMembers = clusterActiveMembers.stream().map(member -> member.getAddress().getHost())
                        .collect(Collectors.toSet());
                log.debug("Removing current cluster member entry {} from cluster configuration",
                        activeMembers.toString());
                if (!CollectionUtils.isEmpty(config.getMembers())) {
                    config.getMembers().removeAll(activeMembers);
                }

                if (currentMemberAddress.equals(config.getMaster())) {
                    log.debug("Updating master entry as the master has lost the quorum");
                    config.setMaster(null);
                }
                configService.updateClusterConfiguration(config);
            }
        }
    }

    /** convert string value to deployment mode enum **/
    private MangleDeploymentMode extractMangleDeploymentMode(String deploymentMode) {
        return deploymentMode.equals(MangleDeploymentMode.CLUSTER.name()) ? MangleDeploymentMode.CLUSTER
                : MangleDeploymentMode.STANDALONE;
    }

    /**
     * Checks if the current member is the oldest member in the cluster(hazelcast master node)
     *
     * @return
     */
    private boolean isCurrentNodeOldestMember() {
        return hazelcastInstance.getCluster().getLocalMember() == hazelcastInstance.getCluster().getMembers().iterator()
                .next();
    }

}
