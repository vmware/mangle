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

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.partition.PartitionEvent;
import com.hazelcast.partition.PartitionEventListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class HazelcastTaskMapListener implements HazelcastInstanceAware, EntryAddedListener<String, String>,
        EntryRemovedListener<String, String>, EntryUpdatedListener<String, String>,
        EntryEvictedListener<String, String>, PartitionEventListener<PartitionEvent> {


    @Autowired
    HazelcastTaskService hazelcastTaskService;
    private HazelcastInstance hz;

    @Override
    public void onEvent(PartitionEvent event) {
        log.debug("Event triggered for the partition {}", event.getPartitionId());
    }

    /**
     * Triggered when an entry is deleted from the hazelcast task map An entry from the hazelcast is
     * removed if an task is of type
     *
     * 1. schedule, and the status of the schedule is either changed to cancelled or paused or
     * failed
     *
     * 2. simple fault, and the status of the task is changed to either failed/completed
     *
     * In these scenarios hazelcast entry remove event is triggered, which will update nodeTasks
     * map, which holds the mapping of node to task association for each of the task
     *
     * @param event
     */
    @Override
    public void entryRemoved(EntryEvent<String, String> event) {
        log.debug("Task with the id {} is removed from hazelcast cluster cache", event.getKey());
        hazelcastTaskService.removeTaskFromClusterNodeCache(event.getKey());
    }

    @Override
    public void entryEvicted(EntryEvent<String, String> event) {
        log.debug("Task map entry with the taskid {} is evicted", event.getKey());
    }

    @Override
    public void entryUpdated(EntryEvent<String, String> event) {
        try {
            hazelcastTaskService.cleanUpTask(event.getKey(), event.getValue());
        } catch (MangleException e) {
            log.error("Clean up of the task {} failed with the error {}", event.getKey(), e.getMessage());
        }
        log.debug("Entry update event is triggered for the task with the id: {} ", event.getKey());
    }

    @Override
    public void entryAdded(EntryEvent<String, String> event) {
        try {
            log.info("Fault task with the id {} is assigned to member {}", event.getKey(),
                    hz.getCluster().getLocalMember().getAddress().getHost());
            hazelcastTaskService.triggerTask(event.getKey());
        } catch (MangleException e) {
            log.error("Failed to trigger the task {} on the node with the exception: {}", event.getKey(),
                    e.getMessage());
        }
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        hz = hazelcastInstance;
    }
}
