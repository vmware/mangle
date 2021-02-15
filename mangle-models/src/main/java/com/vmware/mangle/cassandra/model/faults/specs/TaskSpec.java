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

package com.vmware.mangle.cassandra.model.faults.specs;

import java.io.Serializable;
import java.util.Set;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.mapping.Indexed;

import com.vmware.mangle.cassandra.model.MangleDto;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerInfo;

/**
 * @author bkaranam
 * @author ashrimali
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskSpec extends MangleDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    @Valid
    protected SchedulerInfo schedule;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String specType;
    @Indexed
    private String taskName;

    @ApiModelProperty(notes = "Notifier names to be used while sending the notification.", required = false, example = "[\"mangle-test\"]")
    @JsonProperty(required = false)
    private Set<String> notifierNames;

    public TaskSpec() {
        this.id = super.generateId();
    }

    @Override
    public String getPrimaryKey() {
        return id;
    }
}
