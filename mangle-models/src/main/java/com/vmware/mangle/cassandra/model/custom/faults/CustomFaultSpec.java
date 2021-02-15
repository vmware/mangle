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

package com.vmware.mangle.cassandra.model.custom.faults;

import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import com.vmware.mangle.cassandra.model.plugin.ExtensionDetails;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerInfo;
import com.vmware.mangle.cassandra.model.tasks.DockerSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;

/**
 * Model class for CustomFault.
 *
 * @author kumargautam
 */
@Data
public class CustomFaultSpec {
    @NotEmpty
    private String pluginId;
    @NotEmpty
    private String faultName;
    @JsonIgnore
    @Transient
    private ExtensionDetails extensionDetails;
    @NotEmpty
    private Map<String, String> faultParameters;
    @NotEmpty
    private String endpointName;
    @Valid
    private SchedulerInfo schedule;
    @ApiModelProperty(value = "Tags to be used while sending the events, metrics to monitoring system configured. Example: \"environment\" : \"production\"")
    private Map<String, String> tags;
    @Valid
    private K8SSpecificArguments k8sArguments;
    @Valid
    private DockerSpecificArguments dockerArguments;
    @JsonProperty(required = false)
    private Set<String> notifierNames;
}