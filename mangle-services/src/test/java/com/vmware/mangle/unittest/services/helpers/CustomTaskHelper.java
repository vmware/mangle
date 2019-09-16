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

package com.vmware.mangle.unittest.services.helpers;

import org.springframework.context.ApplicationEventPublisher;

import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskInfo;
import com.vmware.mangle.task.framework.skeletons.ITaskHelper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author hkilari
 *
 */
public class CustomTaskHelper implements ITaskHelper {

    @Override
    public void executeTask(Task task) throws MangleException {
        // Dummy Implementation for testing
    }

    @Override
    public String getDescription(Task task) {
        // Dummy Implementation for testing
        return null;
    }

    @Override
    public void run(Task task) throws MangleException {
        // Dummy Implementation for testing

    }

    @Override
    public TaskInfo getInfo(Task task) throws MangleException {
        // Dummy Implementation for testing
        return null;
    }

    @Override
    public void cancel() {
        // Dummy Implementation for testing
    }

    @Override
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        // Dummy Implementation for testing
    }

}
