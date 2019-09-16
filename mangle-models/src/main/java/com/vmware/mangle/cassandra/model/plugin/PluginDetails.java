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

package com.vmware.mangle.cassandra.model.plugin;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.datastax.driver.core.DataType.Name;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Model class to store Plugin info.
 *
 * @author kumargautam
 */
@Table(value = "PluginInfo")
@Data
public class PluginDetails implements Serializable {

    private static final long serialVersionUID = 1L;
    @Indexed
    private String id;
    @NotEmpty
    @PrimaryKeyColumn(value = "pluginId", ordering = Ordering.ASCENDING, type = PrimaryKeyType.PARTITIONED)
    private String pluginId;
    @NotEmpty
    private String pluginName;
    @NotEmpty
    private String pluginVersion;
    @NotEmpty
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String pluginPath;
    @Indexed
    @NotNull
    private Boolean isLoaded = false;
    @Indexed
    @NotNull
    private Boolean isActive = false;
    @NotEmpty
    @Valid
    private Map<String, CustomFaultDescriptor> customFaultDescriptorMap;
    @JsonIgnore
    @CassandraType(type = Name.BLOB)
    private byte[] pluginFile;

    public PluginDetails() {
        this.id = UUID.randomUUID().toString();
    }
}