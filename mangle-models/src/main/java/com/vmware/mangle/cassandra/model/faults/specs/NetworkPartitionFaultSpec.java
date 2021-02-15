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

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.vmware.mangle.cassandra.model.tasks.DockerSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;
import com.vmware.mangle.services.enums.AgentFaultName;

/**
 * Model for NetworkPartitionFault.
 *
 * @author kumargautam
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class NetworkPartitionFaultSpec extends CommandExecutionFaultSpec {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "Provide list of hostname or IP.", example = "[ 10.1.2.3,10.1.2.4 ]")
    @NotEmpty
    private List<String> hosts;

    @SuppressWarnings("squid:S2637")
    public NetworkPartitionFaultSpec() {
        setFaultName(AgentFaultName.INJECT_NETWORK_PARTITION_FAULT.getValue());
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
