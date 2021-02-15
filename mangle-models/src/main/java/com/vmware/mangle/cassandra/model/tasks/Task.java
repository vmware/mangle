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
import java.util.List;
import java.util.Stack;

import com.datastax.driver.core.DataType.Name;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.MangleDto;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;

/**
 * @author hkilari
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(value = { "primaryKey" })
public class Task<T extends TaskSpec> extends MangleDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @PrimaryKeyColumn(value = "id", ordering = Ordering.ASCENDING, type = PrimaryKeyType.PARTITIONED)
    private String id;
    @Indexed
    private String taskName;
    @CassandraType(type = Name.VARCHAR)
    private TaskType taskType;
    private String taskDescription;
    @Indexed
    private boolean isScheduledTask;
    @SuppressWarnings("squid:S1149")
    private Stack<TaskTrigger> triggers;
    @CassandraType(type = Name.VARCHAR)
    private T taskData;
    private TaskTroubleShootingInfo taskTroubleShootingInfo;
    private boolean initialized;
    private String extensionName;
    private String taskClass;
    private Long lastUpdated;

    public Task() {
        this.id = super.generateId();
    }

    /**
     * @return
     */
    @JsonIgnore
    public String getTaskOutput() {
        return CollectionUtils.isEmpty(getTriggers()) ? null : getTriggers().peek().getTaskOutput();
    }

    /**
     * @param taskOutput
     */
    @JsonIgnore
    public void setTaskOutput(String taskOutput) {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            getTriggers().peek().setTaskOutput(taskOutput);
        }
    }

    /**
     * @return the taskFailureReason
     */
    @JsonIgnore
    public String getTaskFailureReason() {
        return CollectionUtils.isEmpty(getTriggers()) ? null : getTriggers().peek().getTaskFailureReason();
    }

    /**
     * @param taskFailureReason
     *            : the taskFailureReason to set
     */
    @JsonIgnore
    public void setTaskFailureReason(String taskFailureReason) {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            getTriggers().peek().setTaskFailureReason(taskFailureReason);
        }
    }

    @JsonIgnore
    public List<String> getChildTaskIDs() {
        return CollectionUtils.isEmpty(getTriggers()) ? null : getTriggers().peek().getChildTaskIDs();
    }

    @JsonIgnore
    public void setChildTaskIDs(List<String> taskIDs) {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            getTriggers().peek().setChildTaskIDs(taskIDs);
        }
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

    /**
     * @return the taskStatus
     */
    @JsonIgnore
    public boolean isTaskRetriggered() {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            return getTriggers().peek().getTaskRetriggered();
        }
        return false;
    }

    /**
     * @return the taskStatus
     */
    @JsonIgnore
    public void setTaskRetriggered(boolean retriggerTask) {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            getTriggers().peek().setTaskRetriggered(retriggerTask);
        }
    }

    /**
     * @param taskStatus
     *            : the taskStatus to set
     */
    @JsonIgnore
    public void setTaskStatus(TaskStatus taskStatus) {
        if (!CollectionUtils.isEmpty(getTriggers())) {
            getTriggers().peek().setTaskStatus(taskStatus);
        }
    }

    /**
     * @param failureText
     *            : The taskFailureReason to Update
     */
    public void updateTaskFailureReason(String failureText) {
        if (null != getTaskFailureReason()) {
            setTaskFailureReason(getTaskFailureReason() + "\n" + failureText);
        } else {
            setTaskFailureReason(failureText);
        }
    }

    /**
     * @param outputText
     *            : The outputText to Update
     */
    public void updateTaskOutPut(String outputText) {
        if (null != getTaskOutput()) {
            setTaskOutput(getTaskOutput() + "\n" + outputText);
        } else {
            setTaskOutput(outputText);
        }
    }

    /**
     * @param outputText
     *            : The outputText to Update
     */
    public void updateTaskDescription(String outputText) {
        setTaskDescription(outputText);
    }

    public TaskInfo getMangleTaskInfo() {
        return CollectionUtils.isEmpty(getTriggers()) ? null : getTriggers().peek().getMangleTaskInfo();
    }

    /**
     * @param subStage:
     *            task substage
     */
    public void updateTaskSubstage(String subStage) {
        if (null != subStage && !CollectionUtils.isEmpty(getTriggers())) {
            getTriggers().peek().setCurrentStage(subStage);
        }
    }

    /**
     * task substage
     */
    public String getTaskSubstage() {
        return CollectionUtils.isEmpty(getTriggers()) ? null : getTriggers().peek().getCurrentStage();
    }

    @JsonIgnore
    @Override
    public String getPrimaryKey() {
        return this.id;
    }
}
