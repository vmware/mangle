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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.services.cassandra.model.events.basic.Event;

/**
 * @author hkilari
 *
 * @param <T>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskAssignedEvent<T extends Task<? extends TaskSpec>> extends Event
        implements ResolvableTypeProvider {

    private static final long serialVersionUID = 1L;
    private T task;

    public TaskAssignedEvent(T task) {
        super("TaskAssignedEvent", "Assigned Task: " + task.getClass().getName() + " With Id: " + task.getId());
        this.task = task;
    }

    @Override
    @JsonIgnore
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(new Event()));
    }
}
