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

package com.vmware.mangle.cassandra.model.hazelcast;

import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.vmware.mangle.model.enums.MangleDeploymentMode;

/**
 * @author chetanc
 *
 */
@Table(value = "cluster")
@Data
@AllArgsConstructor
public class HazelcastClusterConfig {
    @PrimaryKeyColumn(value = "id", ordering = Ordering.ASCENDING, type = PrimaryKeyType.PARTITIONED)
    private String id;
    private String validationToken;

    private String clusterName;

    @Column
    private Set<String> members;

    private String master;

    private Integer quorum;

    private MangleDeploymentMode deploymentMode;

    public HazelcastClusterConfig() {
        this.id = UUID.randomUUID().toString();
    }
}
