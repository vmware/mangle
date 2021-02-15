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

package com.vmware.mangle.services.mockdata;

import com.vmware.mangle.cassandra.model.tasks.TaskFilter;


/**
 * @author dbhat
 */
public class TaskFilterMockData {

    private TaskFilterMockData() {

    }

    // UI query is made with the following data.
    public static TaskFilter getTaskFilter() {
        TaskFilter taskFilter = new TaskFilter();
        taskFilter.setFromIndex(0);
        taskFilter.setTaskDescription("");
        taskFilter.setTaskStatus("");
        taskFilter.setTaskType("");
        taskFilter.setToIndex(9);
        taskFilter.setEndpointName("");
        return taskFilter;
    }

    public static TaskFilter getTaskFilterWithData() {
        TaskFilter taskFilter = new TaskFilter();
        taskFilter.setFromIndex(0);
        taskFilter.setTaskDescription("dummy");
        taskFilter.setTaskStatus("");
        taskFilter.setTaskType("");
        taskFilter.setToIndex(9);
        taskFilter.setEndpointName("");
        return taskFilter;
    }

    public static TaskFilter getTaskFilterWithPartialFields() {
        TaskFilter taskFilter = new TaskFilter();
        taskFilter.setFromIndex(0);
        taskFilter.setTaskDescription("dummy");
        taskFilter.setToIndex(9);
        return taskFilter;
    }

    public static TaskFilter getTaskFilter1WithPartialFields() {
        TaskFilter taskFilter = new TaskFilter();
        taskFilter.setFromIndex(0);
        taskFilter.setTaskDescription("dummy1");
        taskFilter.setEndpointName("dummy1");
        taskFilter.setToIndex(9);
        return taskFilter;
    }
}
