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

import javax.validation.constraints.NotNull;

import com.datastax.driver.core.DataType.Name;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.cassandra.core.mapping.CassandraType;

import com.vmware.mangle.cassandra.model.scheduler.SchedulerInfo;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;
import com.vmware.mangle.services.enums.DockerFaultName;

/**
 * @author rpraveen
 *
 *         Api payload specification for Docker specific faults
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DockerFaultSpec extends CommandExecutionFaultSpec implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    @CassandraType(type = Name.VARCHAR)
    private DockerFaultName dockerFaultName;

    public DockerFaultSpec() {
        setSpecType(this.getClass().getName());
    }

    public void setDockerFaultName(DockerFaultName dockerFaultName) {
        this.dockerFaultName = dockerFaultName;
        setFaultName(dockerFaultName.name());
    }

    @JsonIgnore
    @Override
    public void setK8sArguments(K8SSpecificArguments k8sArguments) {
        super.setK8sArguments(k8sArguments);
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
