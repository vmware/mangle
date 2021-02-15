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

package com.vmware.mangle.cassandra.model.tasks;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import lombok.Data;

/**
 * @author ranjans
 */
@Data
public class TaskFilter {

    private String taskType = "";
    private String taskDescription = "";
    private String taskStatus = "";
    private String endpointName = "";
    @PositiveOrZero
    private int fromIndex = 0;
    @Positive
    private int toIndex = 9;

}
