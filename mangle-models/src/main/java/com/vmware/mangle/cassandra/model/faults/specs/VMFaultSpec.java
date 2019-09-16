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
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.vmware.mangle.cassandra.model.scheduler.SchedulerInfo;
import com.vmware.mangle.cassandra.model.tasks.DockerSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;

/**
 * @author chetanc
 *
 *         Fault spec all VM State change operations and serves as a parent class for VM NIC and VM
 *         Disk related faults Insert your comment for VMFaultSpec here
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class VMFaultSpec extends CommandExecutionFaultSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    private String vmName;

    @JsonIgnore
    @Override
    public void setTimeoutInMilliseconds(Integer timeoutinMilliseconds) {
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
