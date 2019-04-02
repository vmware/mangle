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
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.vmware.mangle.cassandra.model.tasks.DockerSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;

/**
 * @author bkaranam
 *
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FaultSpec extends TaskSpec implements Serializable {

    private static final long serialVersionUID = 1L;
    @JsonIgnore
    protected String faultName;
    @JsonIgnore
    protected String faultAction;
    @JsonIgnore
    protected String faultDescription;
    @JsonIgnore
    protected String faultType;
    @NotEmpty
    @ApiModelProperty(value = "Name of the endpoint added to Mangle : use  /endpoints api to get the existing endpoints in mangle")
    private String endpointName;

    @Valid
    private K8SSpecificArguments k8sArguments;

    @Valid
    private DockerSpecificArguments dockerArguments;

    @ApiModelProperty(value = "Tags to be used while sending the events, metrics to monitoring system configured. Example: \"environment\" : \"production\"")
    private Map<String, String> tags;

}
