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

package com.vmware.mangle.services.helpers;

import java.util.Stack;

import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreConfigSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskType;

/**
 * @author dbhat
 * @param <T>
 */
@Component
public class ResiliencyScoreTaskHelper<T extends TaskSpec> {

    public Task<T> init(ResiliencyScoreTask resiliencyScoreTask) {
        Task task = new Task<ResiliencyScoreConfigSpec>();
        task.setId(resiliencyScoreTask.getId());
        task.setTaskData(resiliencyScoreTask.getTaskData());
        task.setScheduledTask(resiliencyScoreTask.isScheduledTask());
        task.setTaskType(TaskType.RESILIENCY_SCORE);
        task.setTriggers(new Stack<>());
        return task;
    }
}
