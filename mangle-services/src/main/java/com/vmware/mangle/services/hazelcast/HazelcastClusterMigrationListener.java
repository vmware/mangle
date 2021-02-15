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

package com.vmware.mangle.services.hazelcast;

import static com.vmware.mangle.utils.constants.HazelcastConstants.HAZELCAST_NODE_TASKS_MAP;
import static com.vmware.mangle.utils.constants.HazelcastConstants.HAZELCAST_TASKS_MAP;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MigrationEvent;
import com.hazelcast.core.MigrationListener;
import com.hazelcast.core.PartitionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.enums.MangleQuorumStatus;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.HazelcastConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 * i) Fault when injected will follow the following steps of execution 1. Construct the task object
 * 2. Store in the DB 3. Trigger TaskCreatedEvent 4. TaskCreatedListener will add the task to the
 * hazelcast cluster cache 5. Hazelcast will put this task into its distributed-map 6. Depending on
 * which partition this task was added into, it will trigger an entryAdded event in its respective
 * node 7. Listener to this event will trigger the execution of the task on that particular node
 *
 * ii) When new member joins the cluster 1. The partition ownership of some of the partitions are
 * moved to new cluster member 2. No re-triggering of the tasks will be carried out 3. Task that
 * were being executed by the old owner of the partition will remain to be executed on the old owner
 * Summary: only partition ownership will be transferred
 *
 * iii) When an existing cluster member leaves the cluster 1. Membership event is generated - Tasks
 * that were still running on the old owner, but whose ownership were assigned to the new owner will
 * be re-triggered 2. Migration event is generated - Partitions owned by the dead node will be
 * distributed across the existing cluster nodes - Tasks that are part of dead cluster member will
 * be re-triggered, on the new owner of the respective partitions
 *
 *
 * @author chetanc
 *
 */
@Component
@Log4j2
public class HazelcastClusterMigrationListener implements MigrationListener, HazelcastInstanceAware {


    private HazelcastInstance hz;

    @Autowired
    private HazelcastTaskService hazelcastTaskService;

    private PartitionService partitionService;

    private List<String> taskQueue = new ArrayList<>();

    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private TaskService taskService;

    @Autowired
    public void setTaskScheduler(ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        this.taskScheduler = threadPoolTaskScheduler;
    }


    @Override
    public void migrationStarted(MigrationEvent migrationEvent) {
        if (isMigratedOutOfTheCurrentNode(migrationEvent)) {
            log.trace("Migration has started for the partition {} from old node {} to new node {}",
                    migrationEvent.getPartitionId(), migrationEvent.getOldOwner().getAddress(),
                    migrationEvent.getNewOwner().getAddress());
            IMap<Object, Object> map = hz.getMap(HAZELCAST_TASKS_MAP);
            Set<Object> keys = map.localKeySet();
            for (Object key : keys) {
                String taskId = String.valueOf(key);
                if (isTaskInCurrentMigratedPartition(migrationEvent, taskId)) {
                    try {
                        hazelcastTaskService.cleanUpTaskForMigration(taskId);
                    } catch (MangleException e) {
                        log.error("Schedule cancellation for the task {} on the node {} failed", taskId,
                                migrationEvent.getOldOwner().getAddress());
                    }
                }
            }
        }
    }

    /**
     * This method is triggered when the node leaves the cluster, and the partition ownership
     * migration from the old node to the new node is completed.
     *
     * As part of this event processing, the tasks that are part of the migrated partitions will be
     * re-triggered on the node that takes over the ownership of that partition.
     *
     * @param migrationEvent
     */
    @Override
    public void migrationCompleted(MigrationEvent migrationEvent) {
        if (isMigratedToTheCurrentNode(migrationEvent)
                && HazelcastConstants.getMangleQourumStatus() == MangleQuorumStatus.PRESENT) {
            IMap<Object, Object> map = hz.getMap(HAZELCAST_TASKS_MAP);

            Set<Object> keys = map.localKeySet();
            synchronized (hazelcastTaskService) {
                migrationTasks(migrationEvent, keys);
            }
        }
        log.trace("Migration of the partition {} to {} is completed", migrationEvent.getPartitionId(),
                migrationEvent.getNewOwner().getAddress());
        this.taskScheduler.schedule(this::triggerTasks,
                new Date(System.currentTimeMillis() + Constants.ONE_MINUTE_IN_MILLIS * 5));
    }

    private void migrationTasks(MigrationEvent migrationEvent, Set<Object> keys) {
        for (Object key : keys) {
            String taskId = String.valueOf(key);
            if (isTaskValidForTriggeringOnCurrentNode(migrationEvent, taskId)) {
                try {
                    log.info("Triggering task {} execution on the node {}", taskId,
                            migrationEvent.getNewOwner().getAddress());
                    if (hazelcastTaskService.isScheduledTask(taskId)) {
                        Task<TaskSpec> task = taskService.getTaskById(taskId);
                        hazelcastTaskService.triggerTask(task);
                    } else {
                        taskQueue.add(taskId);
                    }
                } catch (MangleException | HazelcastInstanceNotActiveException e) {
                    log.error("Triggering of the task {} failed with the exception: {}", taskId,
                            e.getStackTrace());
                }
            } else {
                log.debug("Task {} to be migrated is already re-triggered on the node {}", taskId,
                        migrationEvent.getNewOwner());
            }
        }
    }

    private boolean isTaskInCurrentMigratedPartition(MigrationEvent migrationEvent, String taskId) {
        return partitionService.getPartition(taskId).getPartitionId() == migrationEvent.getPartitionId();
    }

    private boolean isMigratedToTheCurrentNode(MigrationEvent event) {
        return hz.getCluster().getLocalMember().equals(event.getNewOwner());
    }

    private boolean isMigratedOutOfTheCurrentNode(MigrationEvent event) {
        return hz.getCluster().getLocalMember().equals(event.getOldOwner());
    }

    private boolean isTaskValidForTriggeringOnCurrentNode(MigrationEvent event, String taskId) {
        IMap<String, Set<String>> nodeToTaskMapping = hz.getMap(HAZELCAST_NODE_TASKS_MAP);
        Set<String> currentNodeTasks = nodeToTaskMapping.get(hz.getCluster().getLocalMember().getUuid());
        return isTaskInCurrentMigratedPartition(event, taskId)
                && (isNodeRemovedMigration(event) || hazelcastTaskService.isScheduledTask(taskId))
                && !isTaskAlreadyTriggered(currentNodeTasks, taskId);
    }

    /**
     * The task has to be triggered if the node hasn't already triggered it
     *
     * @param tasks
     *            Tasks executing in the local node
     * @param taskId
     *            Task that needs to be verified if it is already triggered in the current node
     * @return true if task has to be triggered on the node
     */
    private boolean isTaskAlreadyTriggered(Set<String> tasks, String taskId) {
        return !(CollectionUtils.isEmpty(tasks) || !tasks.contains(taskId));
    }

    /**
     * Migration event triggered because node left the cluster
     *
     * @param event
     *            Migration event
     * @return true if the migration is triggered because a node left the cluster, false if it is
     *         because a new node joined the cluster
     */
    private boolean isNodeRemovedMigration(MigrationEvent event) {
        return event.getNewOwner().getAddress().equals(hz.getCluster().getLocalMember().getAddress())
                && event.getOldOwner() == null;
    }

    @Override
    public void migrationFailed(MigrationEvent migrationEvent) {
        log.error("Migration of the partition {} failed from node {} to  node {}", migrationEvent.getPartitionId(),
                migrationEvent.getOldOwner(), migrationEvent.getNewOwner());
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        hz = hazelcastInstance;
        hazelcastTaskService.setHazelcastInstance(hz);
        partitionService = hz.getPartitionService();
    }

    private void triggerTasks() {
        for (String taskId : taskQueue) {
            try {
                Task<TaskSpec> task = taskService.getTaskById(taskId);
                hazelcastTaskService.triggerTask(task);
            } catch (MangleException e) {
                log.error("Failed to re-trigger the task {} because  of the exception {}", taskId, e.getMessage());
            }
        }
    }
}

