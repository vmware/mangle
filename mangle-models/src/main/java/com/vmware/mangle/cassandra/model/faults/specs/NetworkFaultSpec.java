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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.datastax.driver.core.DataType.Name;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.mapping.CassandraType;

import com.vmware.mangle.cassandra.model.tasks.DockerSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;
import com.vmware.mangle.services.enums.AgentFaultName;
import com.vmware.mangle.services.enums.NetworkFaultType;

/**
 * @author kumargautam,jayasankarr
 *
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({ "k8sArguments", "dockerArguments" })
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class NetworkFaultSpec extends CommandExecutionFaultSpec {

    private static final long serialVersionUID = 1L;
    @NotNull
    @CassandraType(type = Name.VARCHAR)
    private NetworkFaultType faultOperation;

    @ApiModelProperty(value = "Integer value which represents the latency in milliseconds.Required only for network Latency Fault", example = "1000")
    private int latency;
    @Max(100)
    @ApiModelProperty(value = "Integer value between 1 to 100 representing % fault on packets.Required incase of duplication,corruption and loss", example = "80")
    private int percentage;
    @ApiModelProperty(value = "Ethernet interface on to which fault will be injected", example = "eth0")
    @NotEmpty
    private String nicName = "eth0";

    public NetworkFaultSpec() {
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
