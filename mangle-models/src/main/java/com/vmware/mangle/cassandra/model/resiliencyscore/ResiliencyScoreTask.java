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
import java.util.Stack;

import javax.validation.constraints.Pattern;

import com.datastax.driver.core.DataType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.MangleDto;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskType;


/**
 * @author dbhat
 *
 */
@Table(value = "resiliencyScore_task")
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(value = { "primaryKey" })
public class ResiliencyScoreTask extends MangleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @PrimaryKeyColumn(name = "id", ordering = Ordering.ASCENDING, type = PrimaryKeyType.PARTITIONED)
    private String id;

    @CassandraType(type = DataType.Name.VARCHAR)
    @Pattern(regexp = "^[A-Za-z0-9-_.]+$", message = "consists only alphanumeric with special characters (_ - .)")
    @Indexed
    private String serviceName;

    @Indexed
    private boolean isScheduledTask;

    @CassandraType(type = DataType.Name.UDT, userTypeName = "ResiliencyScoreTaskTrigger")
    @SuppressWarnings("squid:S1149")
    private Stack<ResiliencyScoreTaskTrigger> triggers;

    @CassandraType(type = DataType.Name.VARCHAR)
    private TaskSpec taskData;

    @CassandraType(type = DataType.Name.VARCHAR)
    private TaskType taskType;

    @CassandraType(type = DataType.Name.VARCHAR)
    private String taskName;

    @CassandraType(type = DataType.Name.VARCHAR)
    private String taskDescription;

    private Long lastUpdated;

    /**
     * Setting this.id via calling generateId() of Super class.
     */
    public ResiliencyScoreTask() {
        this.id = super.generateId();
    }

    public ResiliencyScoreTaskTrigger currentTaskTrigger() {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            return getTriggers().peek();
        }
        return null;
    }

    /**
     * @return the taskStatus
     */
    @JsonIgnore
    public TaskStatus getTaskStatus() {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            return getTriggers().peek().getTaskStatus();
        }
        return TaskStatus.INITIALIZING;
    }

    @JsonIgnore
    public void setTaskStatus(TaskStatus resiliencyScoreTaskStatus) {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            getTriggers().peek().setTaskStatus(resiliencyScoreTaskStatus);
        }
    }

    @JsonIgnore
    public void setResiliencyScore(double resiliencyScore) {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            getTriggers().peek().setResiliencyScore(resiliencyScore);
        }
    }

    @JsonIgnore
    public void setStatusMessage(String statusMessage) {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            getTriggers().peek().setTaskFailureReason(statusMessage);
        }
    }

    @JsonIgnore
    public void setStartTime(String startTime) {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            getTriggers().peek().setStartTime(startTime);
        }
    }

    @JsonIgnore
    public String getStartTime() {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            return getTriggers().peek().getStartTime();
        }
        return null;
    }

    @JsonIgnore
    public void setEndTime(String endTime) {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            getTriggers().peek().setEndTime(endTime);
        }
    }

    @JsonIgnore
    public String getEndTime() {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            return getTriggers().peek().getEndTime();
        }
        return null;
    }

    @JsonIgnore
    public void setResiliencyScoreDetails(ServiceResiliencyScore serviceResiliencyScore) {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            getTriggers().peek().setResiliencyScoreDetails(serviceResiliencyScore);
        }
    }

    @JsonIgnore
    public void setServiceId(String serviceId) {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            getTriggers().peek().setServiceId(serviceId);
        }
    }

    @Override
    @JsonIgnore
    public String getPrimaryKey() {
        return id;
    }

}
