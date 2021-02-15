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

package com.vmware.mangle.model;

import lombok.Data;

import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskType;


/**
 * @author dbhat
 *
 *         Value object to hold any type of Mangle Task with the information specified. The TaskVO
 *         will help in filtering any Tasks supported by Mangle
 */

@Data
public class TaskWrapper {
    private Object mangleTask;
    private TaskType taskType;
    private String taskDescription;
    private String endpointName = "";
    private TaskStatus taskStatus;
    private Long lastUpdated;
}
