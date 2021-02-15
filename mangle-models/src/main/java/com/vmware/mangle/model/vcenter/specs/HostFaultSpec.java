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

package com.vmware.mangle.model.vcenter.specs;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.datastax.driver.core.DataType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.mapping.CassandraType;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.plugin.PluginMetaInfo;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerInfo;
import com.vmware.mangle.cassandra.model.tasks.DockerSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;
import com.vmware.mangle.services.enums.VCenterHostFaults;

/**
 * @author chetanc
 *
 *         Fault spec all VM State change operations and serves as a parent class for VM NIC and VM
 *         Disk related faults Insert your comment for VMFaultSpec here
 */

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "common-java:DuplicatedBlocks" })
public class HostFaultSpec extends CommandExecutionFaultSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    public HostFaultSpec() {
        setSpecType(this.getClass().getName());
    }

    @CassandraType(type = DataType.Name.VARCHAR)
    private VCenterHostFaults fault;

    private String hostName;

    private boolean enableRandomInjection = true;

    private Map<String, String> filters = new HashMap<>();

    @JsonIgnore
    @Override
    public void setTimeoutInMilliseconds(Integer timeoutInMilliseconds) {
        super.setTimeoutInMilliseconds(timeoutInMilliseconds);
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

    @JsonIgnore
    @Override
    public void setPluginMetaInfo(final PluginMetaInfo pluginMetaInfo) {
        super.setPluginMetaInfo(pluginMetaInfo);
    }

    @JsonIgnore
    @Override
    public void setTags(final Map<String, String> tags) {
        super.setTags(tags);
    }

    @JsonIgnore
    @Override
    public void setRandomEndpoint(final Boolean randomEndpoint) {
        super.setRandomEndpoint(randomEndpoint);
    }

    @JsonIgnore
    @Override
    public void setSpecType(final String specType) {
        super.setSpecType(specType);
    }

    @JsonIgnore
    @Override
    public void setTaskName(final String taskName) {
        super.setTaskName(taskName);
    }
}
