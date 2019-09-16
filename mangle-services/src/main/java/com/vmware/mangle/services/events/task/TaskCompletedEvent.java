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

package com.vmware.mangle.services.events.task;

import lombok.Data;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;

/*
 * @author dbhat
 *
 * Information in the event is to be consumed by all the event handlers and notifiers (like Wavefront, Slack etc..)
 */

@Data
public class TaskCompletedEvent<T extends Task<TaskSpec>> {

    private T task;

    public TaskCompletedEvent(T task) {
        this.task = task;
    }
}
