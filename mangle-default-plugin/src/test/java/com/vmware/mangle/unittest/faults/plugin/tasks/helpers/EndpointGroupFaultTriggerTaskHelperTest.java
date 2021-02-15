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

package com.vmware.mangle.unittest.faults.plugin.tasks.helpers;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.EndpointGroupFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTriggeringTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelperFactory;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelperFactory;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.BytemanFaultTaskHelper;
import com.vmware.mangle.faults.plugin.tasks.helpers.EndpointGroupFaultTriggerTaskHelper;
import com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper2;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.events.TaskSubstageEvent;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.utils.clients.kubernetes.PODClient;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author bkaranam
 *
 */
@Log4j2
public class EndpointGroupFaultTriggerTaskHelperTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    @Mock
    private BytemanFaultHelperFactory bytemanFaultHelperFactory;
    @Mock
    private BytemanFaultHelper bytemanFaultHelper;
    @Mock
    private SystemResourceFaultHelper systemResourceFaultHelper;
    @Mock
    private SystemResourceFaultHelperFactory systemResourceFaultHelperFactory;
    @InjectMocks
    EndpointGroupFaultTriggerTaskHelper<EndpointGroupFaultTriggerSpec, JVMAgentFaultSpec> injectionTask;
    @InjectMocks
    EndpointGroupFaultTriggerTaskHelper<EndpointGroupFaultTriggerSpec, JVMCodeLevelFaultSpec> jvmCodeInjectionTask;
    @Mock
    ApplicationEventPublisher publisher;
    @Mock
    EndpointClientFactory endpointClientFactory;
    @Spy
    BytemanFaultTaskHelper<JVMAgentFaultSpec> bytemanFaultTask;
    @Spy
    SystemResourceFaultTaskHelper2<JVMAgentFaultSpec> systemResourceFaultTask;
    @Spy
    CommandInfoExecutionHelper commandInfoExecutionHelper;
    @Mock
    PODClient podClient;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitOfInjection() throws MangleException {
        EndpointGroupFaultTriggerTaskHelper<EndpointGroupFaultTriggerSpec, JVMAgentFaultSpec> injectionTask =
                new EndpointGroupFaultTriggerTaskHelper<>();

        Task<EndpointGroupFaultTriggerSpec> task =
                injectionTask.init(faultsMockData.getEndpointGroupBytemanCPUFaultTriggerSpec(), null);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskData().isReadyForChildExecution(), false);
        Assert.assertTrue(task.getTaskDescription().contains("Executing Fault: cpuFault on Endpoint Group"));

        task.getTriggers().add(new TaskTrigger());
    }


    @Test
    public void testInitOfRemediation() throws MangleException {
        EndpointGroupFaultTriggerTaskHelper<EndpointGroupFaultTriggerSpec, JVMAgentFaultSpec> injectionTask =
                new EndpointGroupFaultTriggerTaskHelper<>();
        String injectionTaskId = "12345";
        FaultTriggeringTask<EndpointGroupFaultTriggerSpec, JVMAgentFaultSpec> task =
                (FaultTriggeringTask<EndpointGroupFaultTriggerSpec, JVMAgentFaultSpec>) injectionTask
                        .init(faultsMockData.getEndpointGroupBytemanCPUFaultTriggerSpec(), injectionTaskId);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.REMEDIATION);
        Assert.assertEquals(task.getTaskData().isReadyForChildExecution(), false);
        Assert.assertTrue(task.getTaskDescription().contains("Remediating Fault: cpuFault on Endpoint Group"));
        Assert.assertEquals(task.getInjectionTaskId(), injectionTaskId);
    }

    @Test
    public void testExecutionOfSystemResourceInjection() throws MangleException {
        EndpointGroupFaultTriggerSpec faultTriggerSpec = faultsMockData.getEndpointGroupCPUFaultTriggerSpec();
        faultTriggerSpec.getFaultSpec().setRandomEndpoint(true);
        List<EndpointSpec> endpoints = new ArrayList<>();
        endpoints.add(faultsMockData.getLinuxCpuJvmAgentFaultSpec().getEndpoint());
        faultTriggerSpec.setEndpoints(endpoints);
        Task<EndpointGroupFaultTriggerSpec> task = injectionTask.init(faultTriggerSpec);

        systemResourceFaultTask.setSystemResourceFaultHelperFactory(systemResourceFaultHelperFactory);
        injectionTask.setSystemResourceFaultTaskHelper(systemResourceFaultTask);


        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertTrue(task.getTaskDescription().contains("Executing Fault: cpuFault on Endpoint Group"));
        task.getTriggers().add(new TaskTrigger());
        injectionTask.setEventPublisher(publisher);
        when(systemResourceFaultHelperFactory.getHelper(Mockito.any())).thenReturn(systemResourceFaultHelper);

        injectionTask.executeTask(task);
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(injectionTask.getChildTasks(task).size(), 1);
        Assert.assertEquals(injectionTask.isReadyForChildExecution(task), true);
        Assert.assertEquals(task.getTaskSubstage(), "TRIGGER_CHILD_TASKS");
        Mockito.verify(publisher, Mockito.times(2)).publishEvent(Mockito.any(TaskSubstageEvent.class));
    }

    @Test
    public void testExecutionOfBytemanFaultInjection() throws MangleException {
        EndpointGroupFaultTriggerSpec faultTriggerSpec = faultsMockData.getEndpointGroupBytemanCPUFaultTriggerSpec();
        faultTriggerSpec.getFaultSpec().setRandomEndpoint(true);
        List<EndpointSpec> endpoints = new ArrayList<>();
        endpoints.add(faultsMockData.getLinuxCpuJvmAgentFaultSpec().getEndpoint());
        faultTriggerSpec.setEndpoints(endpoints);
        Task<EndpointGroupFaultTriggerSpec> task = injectionTask.init(faultTriggerSpec);
        bytemanFaultTask.setBytemanFaultHelperFactory(bytemanFaultHelperFactory);
        injectionTask.setBytemanFaultTaskHelper(bytemanFaultTask);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertTrue(task.getTaskDescription().contains("Executing Fault: cpuFault on Endpoint Group"));
        task.getTriggers().add(new TaskTrigger());
        injectionTask.setEventPublisher(publisher);
        when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(bytemanFaultHelper);

        injectionTask.executeTask(task);
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(injectionTask.getChildTasks(task).size(), 1);
        Assert.assertEquals(injectionTask.isReadyForChildExecution(task), true);
        Assert.assertEquals(task.getTaskSubstage(), "TRIGGER_CHILD_TASKS");
        Mockito.verify(publisher, Mockito.times(2)).publishEvent(Mockito.any(TaskSubstageEvent.class));
    }

    @Test
    public void testExecutionOfRandomJVMCodeLevelInjection() throws MangleException {
        EndpointGroupFaultTriggerSpec faultTriggerSpec = faultsMockData.getEndpointGroupCodeLevelFaultTriggerSpec();
        List<EndpointSpec> endpoints = new ArrayList<>();
        endpoints.add(faultsMockData.getLinuxCpuJvmAgentFaultSpec().getEndpoint());
        endpoints.add(faultsMockData.getLinuxJvmCodelevelFaultSpec().getEndpoint());
        faultTriggerSpec.getFaultSpec().setRandomEndpoint(false);
        faultTriggerSpec.setEndpoints(endpoints);
        Task<EndpointGroupFaultTriggerSpec> task = jvmCodeInjectionTask.init(faultTriggerSpec);
        bytemanFaultTask.setBytemanFaultHelperFactory(bytemanFaultHelperFactory);
        injectionTask.setBytemanFaultTaskHelper(bytemanFaultTask);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertTrue(
                task.getTaskDescription().contains("Executing Fault: SPRING_SERVICE_EXCEPTION on Endpoint Group"));
        task.getTriggers().add(new TaskTrigger());
        injectionTask.setEventPublisher(publisher);
        when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(bytemanFaultHelper);

        injectionTask.executeTask(task);
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(injectionTask.getChildTasks(task).size(), 1);
        Assert.assertEquals(injectionTask.isReadyForChildExecution(task), true);
        Assert.assertEquals(task.getTaskSubstage(), "TRIGGER_CHILD_TASKS");
        Mockito.verify(publisher, Mockito.times(2)).publishEvent(Mockito.any(TaskSubstageEvent.class));
    }
}
