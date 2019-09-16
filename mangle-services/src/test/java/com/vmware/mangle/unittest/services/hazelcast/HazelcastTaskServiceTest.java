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

package com.vmware.mangle.unittest.services.hazelcast;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.nio.Address;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.CredentialService;
import com.vmware.mangle.services.EndpointService;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.hazelcast.HazelcastTaskService;
import com.vmware.mangle.services.helpers.FaultInjectionHelper;
import com.vmware.mangle.services.helpers.FaultTaskFactory;
import com.vmware.mangle.services.mockdata.HazelcastMockData;
import com.vmware.mangle.services.scheduler.Scheduler;
import com.vmware.mangle.services.tasks.executor.TaskExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author chetanc
 */
public class HazelcastTaskServiceTest {

    @Mock
    private TaskService taskService;

    @Mock
    private TaskExecutor<Task<? extends TaskSpec>> concurrentTaskRunner;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private EndpointService endpointService;

    @Mock
    private CredentialService credentialService;

    @Mock
    private FaultTaskFactory faultTaskFactory;

    @Mock
    private Scheduler scheduler;

    @Mock
    private HazelcastInstance instance;

    @Mock
    private FaultInjectionHelper injectionHelper;

    private HazelcastMockData mockData = new HazelcastMockData();

    @InjectMocks
    private HazelcastTaskService hazelcastTaskService;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        hazelcastTaskService.setHazelcastInstance(instance);
    }

    @Test
    public void testTriggerTask() throws UnknownHostException, MangleException {
        String taskId = UUID.randomUUID().toString();

        Task task = mockData.getMockTask();
        CommandExecutionFaultSpec commandExecutionFaultSpec = (CommandExecutionFaultSpec) mockData.getMockFaultSpec();
        EndpointSpec endpointSpec = mockData.getMockEndpointSpec();
        String endpoinName = commandExecutionFaultSpec.getEndpointName();

        Cluster cluster = mock(Cluster.class);
        Member member = mock(Member.class);
        Address address = new Address("127.0.0.1", 90000);
        String memberId = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();
        Set<Object> currentNodeTasks = new HashSet<Object>(Arrays.asList(key));
        IMap<Object, Object> map = mock(IMap.class);

        when(taskService.getTaskById(taskId)).thenReturn(mockData.getMockTask());
        when(concurrentTaskRunner.submitTask(any())).thenReturn(task);
        when(endpointService.getEndpointByName(endpoinName)).thenReturn(endpointSpec);
        when(credentialService.getCredentialByName(anyString())).thenReturn(mockData.getVcenterCredentials());
        when(faultTaskFactory.getTask(any())).thenReturn(task);

        when(instance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(member.getAddress()).thenReturn(address);
        when(instance.getMap(any())).thenReturn(map);
        when(map.get(memberId)).thenReturn(currentNodeTasks);
        doNothing().when(injectionHelper).updateFaultSpec(any());
        hazelcastTaskService.triggerTask(taskId);

        verify(taskService, times(1)).getTaskById(anyString());
        verify(concurrentTaskRunner, times(1)).submitTask(any());

        verify(instance, times(1)).getCluster();
        verify(cluster, times(1)).getLocalMember();
        verify(member, times(1)).getAddress();
        verify(instance, times(1)).getMap(any());
    }

    @Test
    public void testTriggerTaskExistingTasks() throws UnknownHostException, MangleException {
        String taskId = UUID.randomUUID().toString();

        Task task = mockData.getMockTask();
        CommandExecutionFaultSpec commandExecutionFaultSpec = (CommandExecutionFaultSpec) mockData.getMockFaultSpec();
        EndpointSpec endpointSpec = mockData.getMockEndpointSpec();
        String endpoinName = commandExecutionFaultSpec.getEndpointName();

        Cluster cluster = mock(Cluster.class);
        Member member = mock(Member.class);
        Address address = new Address("127.0.0.1", 90000);
        String memberId = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();
        Set<Object> currentNodeTasks = new HashSet<Object>(Arrays.asList(key));
        IMap<Object, Object> map = mock(IMap.class);

        when(taskService.getTaskById(taskId)).thenReturn(mockData.getMockTask());
        when(concurrentTaskRunner.submitTask(any())).thenReturn(task);
        when(endpointService.getEndpointByName(endpoinName)).thenReturn(endpointSpec);
        when(credentialService.getCredentialByName(anyString())).thenReturn(mockData.getVcenterCredentials());
        when(faultTaskFactory.getTask(any())).thenReturn(task);

        when(instance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(member.getAddress()).thenReturn(address);
        when(instance.getMap(any())).thenReturn(map);
        when(map.containsKey(memberId)).thenReturn(true);
        when(map.get(memberId)).thenReturn(currentNodeTasks);
        when(member.getUuid()).thenReturn(memberId);

        hazelcastTaskService.triggerTask(taskId);

        verify(taskService, times(1)).getTaskById(anyString());
        verify(concurrentTaskRunner, times(1)).submitTask(any());

        verify(instance, times(1)).getCluster();
        verify(cluster, times(1)).getLocalMember();
        verify(member, times(1)).getAddress();
        verify(instance, times(1)).getMap(any());

        verify(map, times(1)).containsKey(memberId);
        verify(map, times(1)).get(memberId);
        verify(member, times(1)).getUuid();
        verify(map, times(1)).put(any(), any());
    }

    @Test
    public void testTriggerTaskForCompletedTask() throws UnknownHostException, MangleException {
        String taskId = UUID.randomUUID().toString();

        Task task = mockData.getMockTask();
        CommandExecutionFaultSpec commandExecutionFaultSpec = (CommandExecutionFaultSpec) mockData.getMockFaultSpec();

        String key = UUID.randomUUID().toString();
        Set<Object> currentNodeTasks = new HashSet<Object>(Arrays.asList(key));
        IMap<Object, Object> map = mock(IMap.class);

        task.setTaskStatus(TaskStatus.COMPLETED);

        when(taskService.getTaskById(taskId)).thenReturn(task);
        when(instance.getMap(anyString())).thenReturn(map);

        hazelcastTaskService.triggerTask(taskId);

        verify(taskService, times(1)).getTaskById(anyString());
        verify(concurrentTaskRunner, times(0)).submitTask(any());
        verify(endpointService, times(0)).getEndpointByName(anyString());
        verify(credentialService, times(0)).getCredentialByName(anyString());
        verify(faultTaskFactory, times(0)).getTask(any());
        verify(instance, times(1)).getMap(anyString());
        verify(map, times(1)).remove(anyString());
    }

    @Test
    public void testTriggerTaskForCompletedSchedularTask() throws UnknownHostException, MangleException {
        String taskId = UUID.randomUUID().toString();

        Task task = mockData.getMockSchedulerTask();
        SchedulerSpec schedulerSpec = new SchedulerSpec();

        IMap<Object, Object> map = mock(IMap.class);

        task.setTaskStatus(TaskStatus.COMPLETED);
        schedulerSpec.setStatus(SchedulerStatus.CANCELLED);

        when(taskService.getTaskById(taskId)).thenReturn(task);
        when(instance.getMap(anyString())).thenReturn(map);
        when(schedulerService.getSchedulerDetailsById(anyString())).thenReturn(schedulerSpec);
        when(scheduler.isTaskAlreadyScheduled(taskId)).thenReturn(false);

        hazelcastTaskService.triggerTask(taskId);

        verify(taskService, times(1)).getTaskById(anyString());
        verify(concurrentTaskRunner, times(0)).submitTask(any());
        verify(endpointService, times(0)).getEndpointByName(anyString());
        verify(credentialService, times(0)).getCredentialByName(anyString());
        verify(faultTaskFactory, times(0)).getTask(any());
        verify(instance, times(1)).getMap(anyString());
        verify(map, times(1)).remove(anyString());
    }
}
