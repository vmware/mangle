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

package com.vmware.mangle.cassandra.model.resiliencyscore;

import java.io.Serializable;

import com.datastax.driver.core.DataType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import com.vmware.mangle.cassandra.model.tasks.TaskStatus;


/**
 * @author dbhat
 */

@UserDefinedType(value = "ResiliencyScoreTaskTrigger")
@Data
@EqualsAndHashCode(callSuper = false)
public class ResiliencyScoreTaskTrigger implements Serializable {
    private static final long serialVersionUID = 1L;

    private String startTime;
    private String endTime;
    private String serviceId;
    @CassandraType(type = DataType.Name.VARCHAR)
    private TaskStatus taskStatus;
    private String taskFailureReason;
    private ServiceResiliencyScore resiliencyScoreDetails;
    private double resiliencyScore;
}
