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

package com.vmware.mangle.services.mockdata;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hazelcast.core.IMap;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.VCenterConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.VCenterCredentials;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.model.enums.EndpointType;

/**
 *
 *
 * @author chetanc
 */
public class HazelcastMockData {

    Map<Object, Object> tasks = new HashMap<Object, Object>();

    EndpointMockData endpointMockData = new EndpointMockData();
    CredentialsSpecMockData credentialsSpecMockData = new CredentialsSpecMockData();

    public IMap<Object, Object> getTasks() {
        tasks.put(UUID.randomUUID().toString(), TaskStatus.IN_PROGRESS.name());
        tasks.put(UUID.randomUUID().toString(), TaskStatus.IN_PROGRESS.name());
        tasks.put(UUID.randomUUID().toString(), TaskStatus.COMPLETED.name());
        IMap<Object, Object> map = (IMap<Object, Object>) tasks;
        return map;
    }

    public EndpointSpec getMockEndpointSpec() {
        EndpointSpec vcenterEndpointSpec = new EndpointSpec();
        vcenterEndpointSpec.setName("vCenterMock");
        vcenterEndpointSpec.setEndPointType(EndpointType.VCENTER);
        vcenterEndpointSpec.setCredentialsName("vCenterMockCred");
        vcenterEndpointSpec.setVCenterConnectionProperties(getVCenterConnectionPropertiess());
        return vcenterEndpointSpec;
    }

    public CommandExecutionFaultSpec getMockFaultSpec() {
        CommandExecutionFaultSpec commandExecutionFaultSpec = new CommandExecutionFaultSpec();
        commandExecutionFaultSpec.setEndpointName(getMockEndpointSpec().getName());
        return commandExecutionFaultSpec;
    }

    public K8SFaultTriggerSpec getK8SFaultTriggerSpec() {
        K8SFaultTriggerSpec k8SFaultTriggerSpec = new K8SFaultTriggerSpec();
        k8SFaultTriggerSpec.setFaultSpec(getMockFaultSpec());
        return k8SFaultTriggerSpec;
    }

    public VCenterCredentials getVcenterCredentials() {
        return new VCenterCredentials();
    }

    public Task getMockTask() {
        TasksMockData<CommandExecutionFaultSpec> tasksMockData =
                new TasksMockData<CommandExecutionFaultSpec>(getMockFaultSpec());
        Task task = tasksMockData.getDummyTask();
        task.setScheduledTask(false);
        return task;
    }

    public Task getMockSchedulerTask() {
        Task<CommandExecutionFaultSpec> task = getMockTask();
        task.setScheduledTask(true);
        return task;
    }

    public VCenterConnectionProperties getVCenterConnectionPropertiess() {
        VCenterConnectionProperties vCenterConnectionProperties = new VCenterConnectionProperties();
        vCenterConnectionProperties.setHostname("127.0.0.1");
        return vCenterConnectionProperties;
    }


}
