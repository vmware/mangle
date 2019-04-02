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

package com.vmware.mangle.task.framework.skeletons;

import org.springframework.context.ApplicationEventPublisher;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskInfo;
import com.vmware.mangle.task.framework.plugin.context.FIExtensionPoint;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * @author hkilari
 *
 */
public interface ITaskHelper<T extends TaskSpec> extends FIExtensionPoint {

    public void executeTask(Task<T> task) throws MangleException;

    public String getDescription(Task<T> task);

    public void run(Task<T> task) throws MangleException;

    public TaskInfo getInfo(Task<T> task) throws MangleException;

    public void cancel();

    void setEventPublisher(ApplicationEventPublisher eventPublisher);
}
