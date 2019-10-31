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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.vmware.mangle.model.constants.Constants;

/**
 *
 *
 * @author chetanc
 */
@Data
@Table(value = "user")
@AllArgsConstructor
public class User {

    private String id;

    @PrimaryKeyColumn(value = "name", ordering = Ordering.ASCENDING, type = PrimaryKeyType.PARTITIONED)
    @NotEmpty
    private String name;

    @Transient
    @JsonIgnore
    private Set<Role> roles;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Pattern(regexp = Constants.AUTH_PD_PATTERN, message = Constants.AUTH_PATTERN_MESSAGE)
    private String password;

    private Boolean accountLocked;

    @Column
    @Indexed
    private Set<String> roleNames;

    public User(String name, String password, Set<Role> roles) {
        this.name = name;
        this.roles = roles;
        this.password = password;
        this.accountLocked = false;
    }

    public User(String name, String password, Role role) {
        this.name = name;
        Set<Role> roleSet = new HashSet<>(Arrays.asList(role));
        this.setRoleNames(roleSet.stream().map(Role::getName).collect(Collectors.toSet()));
        this.roles = roleSet;
        this.password = password;
        this.accountLocked = false;
    }

    public User() {
        this.id = UUID.randomUUID().toString();
        this.accountLocked = false;
    }
}
