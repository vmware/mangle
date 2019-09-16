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

package com.vmware.mangle.task.framework.events;

import lombok.Data;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;

/**
 * @author bkaranam
 * @param <T>
 */
@Data
public class TaskSubstageEvent<T extends Task<TaskSpec>> {
    private static final long serialVersionUID = 1L;
    private T task;

    public TaskSubstageEvent(T task) {
        this.task = task;
    }
}
