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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.vmware.mangle.cassandra.model.tasks.DockerSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;
import com.vmware.mangle.model.enums.ClockSkewOperation;
import com.vmware.mangle.services.enums.AgentFaultName;

/**
 * @author ashrimali
 *
 */

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ClockSkewSpec extends CommandExecutionFaultSpec {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Number of seconds targeted for Clock Skew fault", allowableValues = "0-60")
    @NotNull
    @Min(0)
    @Max(60)
    private short seconds;

    @ApiModelProperty(value = "Number of minutes targeted for Clock Skew fault", allowableValues = "0-60")
    @NotNull
    @Min(0)
    @Max(60)
    private short minutes;

    @ApiModelProperty(value = "Number of hours targeted for Clock Skew fault", allowableValues = "0-24")
    @NotNull
    @Min(0)
    @Max(24)
    private short hours;

    @ApiModelProperty(value = "Number of days targeted for Clock Skew fault", allowableValues = "0-365")
    @NotNull
    @Min(0)
    @Max(365)
    private short days;

    @NotNull
    @ApiModelProperty("Type of Clock Skew Operations")
    private ClockSkewOperation clockSkewOperation;

    public ClockSkewSpec() {
        setFaultName(AgentFaultName.INJECT_CLOCK_SKEW.getValue());
        setSpecType(this.getClass().getName());
    }

    @NotNull
    @Override
    public Integer getTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }

    @JsonIgnore
    @Override
    public void setK8sArguments(K8SSpecificArguments k8sArguments) {
        super.setK8sArguments(k8sArguments);
    }

    @JsonIgnore
    @Override
    public void setDockerArguments(DockerSpecificArguments dockerArguments) {
        super.setDockerArguments(dockerArguments);
    }
}

