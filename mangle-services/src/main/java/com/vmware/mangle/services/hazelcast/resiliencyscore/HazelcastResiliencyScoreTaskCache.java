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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.vmware.mangle.utils.constants.ErrorConstants;


/**
 * @author dbhat
 *
 */
@Component
@Log4j2
@DependsOn("hazelcastInstance")
public class HazelcastResiliencyScoreTaskCache {

    private IMap<String, String> taskMap;

    @Autowired
    public HazelcastResiliencyScoreTaskCache(@Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance,
            HazelcastResiliencyScoreMapListener listener) {
        taskMap = hazelcastInstance.getMap(HAZELCAST_RESILIENCY_SCORE_MAP);
        taskMap.addLocalEntryListener(listener);
    }

    public String addTaskToCache(String key, String value) {
        log.debug("Adding key {} with the value {} to the map", key, value);
        if (!taskMap.containsKey(key)) {
            taskMap.put(key, value);
        } else if (taskMap.containsKey(key) && !taskMap.get(key).equals(value)) {
            updateHazelcastTaskCache(key, value);
        }
        return key;
    }

    public void updateHazelcastTaskCache(String taskId, String taskStatus) {
        log.debug("Updating the task {} status to {} on hazelcast", taskId, taskStatus);
        if (taskMap.containsKey(taskId)) {
            taskMap.replace(taskId, taskStatus);
        } else {
            log.error(ErrorConstants.UNEXPECTED_TASK_UPDATE_EVENT);
        }
    }

    public String deleteFromTaskCache(String taskId) {
        log.debug("Removing the task entry from the hazelcast task cache");
        return taskMap.remove(taskId);
    }

    public void cleanTaskMapForQuorumFailure() {
        log.debug("Removing all the task as quorum has failed");
        if (taskMap != null) {
            taskMap.evictAll();
        }
    }
}
