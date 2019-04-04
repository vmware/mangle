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

import com.datastax.driver.core.DataType.Name;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.mapping.CassandraType;

import com.vmware.mangle.cassandra.model.tasks.DockerSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;
import com.vmware.mangle.services.enums.AgentFaultName;
import com.vmware.mangle.services.enums.NetworkFaultType;

/**
 * @author kumargautam
 *
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({ "k8sArguments", "dockerArguments" })
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class NetworkLatencySpec extends CommandExecutionFaultSpec {

    private static final long serialVersionUID = 1L;
    @CassandraType(type = Name.VARCHAR)
    private NetworkFaultType latencyOperation;
    private String latencyAmount;
    private String nicName = "eth0";

    public NetworkLatencySpec() {
        setFaultName(AgentFaultName.INJECT_NETWORK_LATENCY_FAULT.getValue());
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
}
