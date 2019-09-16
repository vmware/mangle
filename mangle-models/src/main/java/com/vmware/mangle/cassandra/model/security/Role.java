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

package com.vmware.mangle.cassandra.model.security;

import java.util.Set;
import java.util.UUID;

import com.datastax.driver.core.DataType.Name;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.vmware.mangle.model.enums.MangleScopeEnum;

/**
 *
 *
 * @author chetanc
 */
@Data
@Table(value = "role")
public class Role {

    private String id;

    @PrimaryKeyColumn(value = "name", ordering = Ordering.ASCENDING, type = PrimaryKeyType.PARTITIONED)
    private String name;

    @Indexed
    @Column
    @CassandraType(type = Name.VARCHAR)
    @JsonIgnore
    private MangleScopeEnum type;

    @Transient
    @JsonIgnore
    private Set<Privilege> privileges;

    @Column
    private Set<String> privilegeNames;

    public Role() {
        this.id = UUID.randomUUID().toString();
    }

    public Role(String roleName, Set<Privilege> privileges) {
        this.name = roleName;
        this.privileges = privileges;
    }
}
