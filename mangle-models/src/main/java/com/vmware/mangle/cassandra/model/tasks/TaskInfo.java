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

import java.io.Serializable;

import com.datastax.driver.core.DataType.Name;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

/**
 * @author hkilari
 *
 */
@UserDefinedType("TaskInfo")
@Data
@EqualsAndHashCode(callSuper = false)
public class TaskInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    @CassandraType(type = Name.VARCHAR)
    private TaskStatus taskStatus;
    private double percentageCompleted;
}
