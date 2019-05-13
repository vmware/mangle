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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.URLConstants;

/**
 * @author chetanc
 */
@Component
@Log4j2
public class HazelcastTaskCache implements HazelcastInstanceAware {

    @Autowired
    private HazelcastTaskMapListener listener;

    private HazelcastInstance hz;

    private IMap<String, String> taskMap;

    public String addTaskToCache(String key, String value) {
        log.debug("Adding key {} with the value {} to the map", key, value);
        if (!taskMap.containsKey(key)) {
            taskMap.put(key, value);
        } else if (taskMap.containsKey(key) && !taskMap.get(key).equals(value)) {
            updateTaskCache(key, value);
        }
        return key;
    }

    public void updateTaskCache(String taskId, String taskStatus) {
        log.debug("Modifying the task status of the task {} to {} on hazelcast", taskId, taskStatus);
        if (taskMap.containsKey(taskId)) {
            taskMap.replace(taskId, taskStatus);
        } else {
            log.fatal(ErrorConstants.UNEXPECTED_TASK_UPDATE_EVENT);
        }
    }

    public String deleteFromTaskCache(String taskId) {
        log.debug("Removing the task entry from the hazelcast task cache");
        return taskMap.remove(taskId);
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        hz = hazelcastInstance;
        taskMap = hz.getMap(URLConstants.HAZELCAST_TASKS_MAP);
        taskMap.addLocalEntryListener(listener);
    }
}
