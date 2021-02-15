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

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.partition.PartitionEvent;
import com.hazelcast.partition.PartitionEventListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author dbhat
 *
 */
@Component
@Log4j2
@DependsOn("hazelcastInstance")
public class HazelcastResiliencyScoreMapListener implements EntryAddedListener<String, String>,
        EntryRemovedListener<String, String>, EntryUpdatedListener<String, String>,
        EntryEvictedListener<String, String>, PartitionEventListener<PartitionEvent> {

    @Autowired
    HazelcastResiliencyScoreService resiliencyScoreService;
    @Autowired
    HazelcastInstance hazelcastInstance;

    @Override
    public void onEvent(PartitionEvent event) {
        log.debug("Event triggered for the partition {}", event.getPartitionId());
    }

    /**
     * Triggered when an entry is deleted from the hazelcast task map An entry from the hazelcast is
     * removed if an task is of type
     * <p>
     * 1. schedule, and the status of the schedule is either changed to cancelled or paused or
     * failed
     * <p>
     * 2. simple fault, and the status of the task is changed to either failed/completed
     * <p>
     * In these scenarios hazelcast entry remove event is triggered, which will update nodeTasks
     * map, which holds the mapping of node to task association for each of the task
     *
     * @param event
     */
    @Override
    public void entryRemoved(EntryEvent<String, String> event) {
        log.debug("Task with the id {} is removed from hazelcast cluster cache", event.getKey());
    }

    @Override
    public void entryEvicted(EntryEvent<String, String> event) {
        log.debug("Task map entry with the taskid {} is evicted", event.getKey());
        log.debug("No action required for Resiliency score task service");
    }

    @Override
    public void entryUpdated(EntryEvent<String, String> event) {
        try {
            log.debug("Entry updated event triggered");
            resiliencyScoreService.cleanUpTask(event.getKey(), event.getValue());
        } catch (MangleException e) {
            log.error("Clean up of the task {} failed with the error {}", event.getKey(), e.getMessage());
        }
        log.debug("Entry update event is triggered for the task with the id: {} ", event.getKey());
    }

    @Override
    public void entryAdded(EntryEvent<String, String> event) {
        try {
            log.info("ResiliencyScore task with the id {} is assigned to member {}", event.getKey(),
                    hazelcastInstance.getCluster().getLocalMember().getAddress().getHost());
            resiliencyScoreService.triggerTask(event.getKey());
        } catch (MangleException e) {
            log.error("Failed to trigger the task {} on the node with the exception: {}", event.getKey(),
                    e.getMessage());
        }
    }


}
