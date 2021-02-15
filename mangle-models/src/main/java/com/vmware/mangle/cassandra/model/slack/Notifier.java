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

package com.vmware.mangle.cassandra.model.slack;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Notifier model.
 *
 * @author kumargautam
 */
@Table(value = "Notifier")
@Data
public class Notifier implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(position = 0, value = "Name of Notifier which will be used in send notification event for fault")
    @PrimaryKeyColumn(value = "name", ordering = Ordering.ASCENDING, type = PrimaryKeyType.PARTITIONED)
    @NotEmpty
    private String name;

    @ApiModelProperty(position = 1, value = "Type of Notifier")
    @NotNull
    private NotifierType notifierType;

    @Valid
    @NotNull
    @ApiModelProperty(position = 2, value = "Provide slack related info")
    private SlackInfo slackInfo;

    @JsonProperty(required = false)
    @ApiModelProperty(position = 3, value = "true or false , specify this option to disable send notification to this slack", example = "true")
    private Boolean enable = true;
}
