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

package com.vmware.mangle.cassandra.model.scheduler;

import java.io.Serializable;
import java.util.Set;

import com.datastax.driver.core.DataType.Name;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.vmware.mangle.cassandra.model.MangleDto;
import com.vmware.mangle.model.enums.SchedulerJobType;
import com.vmware.mangle.model.enums.SchedulerStatus;


/**
 * @author ashrimali
 *
 */
@Data
@Table(value = "SchedulerSpec")
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(value = { "primaryKey" })
public class SchedulerSpec extends MangleDto implements Serializable {
    private static final long serialVersionUID = 1L;
    @PrimaryKeyColumn(value = "id", ordering = Ordering.ASCENDING, type = PrimaryKeyType.PARTITIONED)
    private String id;
    @CassandraType(type = Name.VARCHAR)
    private SchedulerJobType jobType;
    private Long scheduledTime;
    private String cronExpression;
    @Indexed
    @CassandraType(type = Name.VARCHAR)
    private SchedulerStatus status;
    private String description;

    @ApiModelProperty(notes = "Notifier names to be used while sending the notification.", required = false, example = "[\"mangle-test\"]")
    @JsonProperty(required = false)
    private Set<String> notifierNames;

    public SchedulerSpec() {
        if (null == this.id) {
            this.id = super.generateId();
        }
    }

    @JsonIgnore
    @Override
    public String getPrimaryKey() {
        return this.id;
    }
}
