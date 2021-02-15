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
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import com.vmware.mangle.cassandra.model.scheduler.SchedulerInfo;
import com.vmware.mangle.cassandra.model.tasks.DockerSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;

/**
 * Model class used to store Extensions name.
 *
 * @author kumargautam
 */
@UserDefinedType("CustomFaultExecutionSpec")
@Data
public class CustomFaultExecutionRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    @NotEmpty
    private String faultName;
    @NotEmpty
    private String endpointName;
    @NotEmpty
    private Map<String, String> faultParameters;
    @NotEmpty
    private String pluginId;
    @Valid
    private SchedulerInfo schedule;
    @ApiModelProperty(value = "Tags to be used while sending the events, metrics to monitoring system configured. Example: \"environment\" : \"production\"")
    private Map<String, String> tags;
    @Valid
    private K8SSpecificArguments k8sArguments;
    @Valid
    private DockerSpecificArguments dockerArguments;
    @ApiModelProperty(notes = "Notifier names to be used while sending the notification.", required = false, example = "[\"mangle-test\"]")
    @JsonProperty(required = false)
    private Set<String> notifierNames;
}
