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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.vmware.mangle.cassandra.model.tasks.DockerSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;
import com.vmware.mangle.services.enums.AgentFaultName;

/**
 * @author rpraveen
 *
 *         Api payload specification for Disk specific faults
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({ "timeoutinMilliseconds", "k8sArguments", "dockerArguments" })
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class DiskFUSEFaultSpec extends CommandExecutionFaultSpec implements Serializable {
    private static final long serialVersionUID = 1L;

    public DiskFUSEFaultSpec() {
        setFaultName(AgentFaultName.INJECT_DISK_FUSE_FAULT.getValue());
        setSpecType(this.getClass().getName());
    }

    @ApiModelProperty(value = "Latency time in Milliseconds, if Diskfaulttype value specified is disklatency ")
    private String diskLatencyinMilliseconds;
    @ApiModelProperty(value = "Select the Diskfaulttype out of these specified ones", example = "disklatency,ioError,NOSPCError,QUOTAExceed,randomError")
    private String diskFaultType;
    @ApiModelProperty(value = "The directory of the charybdefs where the pythonclient for the invocation of fault is to be copied  ", example = "/root/disklatency/charybdefs/")
    private String diskFUSEFaultDirectory;
    @ApiModelProperty(value = "socketPort of the Remote destination VM from which the python client gets connected", example = "9090")
    private String socketPort;

    @JsonIgnore
    @Override
    public void setTimeoutInMilliseconds(String timeoutinMilliseconds) {
        super.setTimeoutInMilliseconds(timeoutinMilliseconds);
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
