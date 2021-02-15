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

package com.vmware.mangle.cassandra.model.redis.faults.specs;

import java.io.Serializable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerInfo;
import com.vmware.mangle.cassandra.model.tasks.DockerSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;
import com.vmware.mangle.services.enums.RedisFaultName;

/**
 * RedisFaultSpec model.
 *
 * @author kumargautam
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class RedisFaultSpec extends CommandExecutionFaultSpec implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "Limits how many times it applies the rule, the value unit is in percentage.", example = "50")
    @NotNull
    @Min(1)
    @Max(100)
    private Integer percentage;

    @SuppressWarnings("squid:S2637")
    public RedisFaultSpec() {
        setFaultName(RedisFaultName.REDISDBRETURNEMPTYFAULT.getValue());
        setSpecType(this.getClass().getName());
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

    @JsonIgnore
    @Override
    public void setTimeoutInMilliseconds(Integer timeoutinMilliseconds) {
        super.setTimeoutInMilliseconds(timeoutinMilliseconds);
    }

    @JsonIgnore
    @Override
    public void setInjectionHomeDir(String injectionHomeDir) {
        super.setInjectionHomeDir(injectionHomeDir);
    }

    @JsonIgnore
    @Override
    public void setSchedule(SchedulerInfo schedulerInfo) {
        super.setSchedule(schedule);
    }
}
