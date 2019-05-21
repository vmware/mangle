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
import static org.mockito.Mockito.doThrow;
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
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.Partition;
import com.hazelcast.core.PartitionService;
import com.hazelcast.nio.Address;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.hazelcast.HazelcastClusterConfig;
import com.vmware.mangle.services.ClusterConfigService;
import com.vmware.mangle.services.hazelcast.HazelcastClusterMembershipListener;
import com.vmware.mangle.services.hazelcast.HazelcastTaskService;
import com.vmware.mangle.services.tasks.executor.TaskExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author chetanc
 */
public class HazelcastClusterMembershipListenerTest {
    @Mock
    private HazelcastTaskService taskService;

    private HazelcastInstance hazelcastInstance;

    @Mock
    private PartitionService partitionService;

    @Mock
    private ClusterConfigService configService;

    @Mock
    private TaskExecutor taskExecutor;

    @InjectMocks
    private HazelcastClusterMembershipListener listener;

    private HazelcastClusterConfig clusterConfig = new HazelcastClusterConfig();

    @BeforeMethod
    public void initMocks() throws UnknownHostException {
        MockitoAnnotations.initMocks(this);
        hazelcastInstance = mock(HazelcastInstance.class);
        Cluster cluster = mock(Cluster.class);
        Member newOwner = mock(Member.class);
        Address address = new Address("127.0.0.1", 90000);

        when(hazelcastInstance.getPartitionService()).thenReturn(partitionService);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(newOwner);
        when(newOwner.getAddress()).thenReturn(address);


        doNothing().when(taskService).setHazelcastInstance(any());
        listener.setHazelcastInstance(hazelcastInstance);
        clusterConfig.setMembers(new HashSet<>());
    }

    @Test
    public void testMemberAdded() throws UnknownHostException {
        Member member = mock(Member.class);
        Address address = new Address("127.0.0.1", 90000);
        MembershipEvent event = mock(MembershipEvent.class);
        Set<Member> memberSet = new HashSet<>();
        memberSet.add(member);


        when(member.getAddress()).thenReturn(address);
        when(configService.getClusterConfiguration()).thenReturn(clusterConfig);
        when(configService.updateClusterConfiguration(any())).thenReturn(clusterConfig);
        when(event.getMembers()).thenReturn(memberSet);
        when(event.getMember()).thenReturn(member);

        listener.memberAdded(event);

        verify(configService, times(1)).getClusterConfiguration();
        verify(configService, times(1)).addClusterConfiguration(clusterConfig);
        verify(event, times(1)).getMember();
        verify(member, times(2)).getAddress();
    }


    @Test
    public void testMemberRemoved() throws UnknownHostException, MangleException {
        Member newOwner = mock(Member.class);
        Member oldOwner = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        Partition partition = mock(Partition.class);
        IMap<Object, Object> nodeTask = mock(IMap.class);
        MembershipEvent event = mock(MembershipEvent.class);

        String newMemberId = UUID.randomUUID().toString();
        String oldMemberId = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();
        Set<Object> executingTasks = new HashSet<Object>(Arrays.asList(key));
        Set<Object> currentNodeTasks = new HashSet<Object>(Arrays.asList(key));
        Address address = new Address("127.0.0.1", 90000);

        when(event.getMember()).thenReturn(oldOwner);
        when(cluster.getLocalMember()).thenReturn(newOwner);
        when(event.getCluster()).thenReturn(cluster);
        when(newOwner.getUuid()).thenReturn(newMemberId);
        when(oldOwner.getUuid()).thenReturn(oldMemberId);
        when(oldOwner.getAddress()).thenReturn(address);
        when(newOwner.getAddress()).thenReturn(address);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(hazelcastInstance.getMap("nodeTasks")).thenReturn(nodeTask);
        when(hazelcastInstance.getPartitionService()).thenReturn(partitionService);
        when(nodeTask.get(oldMemberId)).thenReturn(executingTasks);
        when(nodeTask.get(newMemberId)).thenReturn(null);
        when(partitionService.getPartition(anyString())).thenReturn(partition);
        when(partition.getOwner()).thenReturn(newOwner);
        doNothing().when(taskService).triggerTask(anyString());
        when(configService.getClusterConfiguration()).thenReturn(clusterConfig);
        when(configService.updateClusterConfiguration(any())).thenReturn(clusterConfig);


        listener.memberRemoved(event);

        verify(event, times(4)).getMember();
        verify(newOwner, times(2)).getUuid();
        verify(cluster, times(1)).getLocalMember();
        verify(hazelcastInstance, times(3)).getCluster();
        verify(hazelcastInstance, times(1)).getMap("nodeTasks");
        verify(hazelcastInstance, times(1)).getPartitionService();
        verify(partitionService, times(2)).getPartition(anyString());
        verify(taskService, times(1)).triggerTask(anyString());
        verify(partition, times(2)).getOwner();
    }

    @Test
    public void testMemberRemovedTriggerFailure() throws UnknownHostException, MangleException {
        Member newOwner = mock(Member.class);
        Member oldOwner = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        Partition partition = mock(Partition.class);
        IMap<Object, Object> nodeTask = mock(IMap.class);
        MembershipEvent event = mock(MembershipEvent.class);

        String newMemberId = UUID.randomUUID().toString();
        String oldMemberId = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();
        Set<Object> executingTasks = new HashSet<Object>(Arrays.asList(key));
        Set<Object> currentNodeTasks = new HashSet<Object>(Arrays.asList(key));
        Address address = new Address("127.0.0.1", 90000);

        when(event.getMember()).thenReturn(oldOwner);
        when(cluster.getLocalMember()).thenReturn(newOwner);
        when(event.getCluster()).thenReturn(cluster);
        when(newOwner.getUuid()).thenReturn(newMemberId);
        when(oldOwner.getUuid()).thenReturn(oldMemberId);
        when(oldOwner.getAddress()).thenReturn(address);
        when(newOwner.getAddress()).thenReturn(address);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(hazelcastInstance.getMap("nodeTasks")).thenReturn(nodeTask);
        when(hazelcastInstance.getPartitionService()).thenReturn(partitionService);
        when(nodeTask.get(oldMemberId)).thenReturn(executingTasks);
        when(nodeTask.get(newMemberId)).thenReturn(null);
        when(partitionService.getPartition(anyString())).thenReturn(partition);
        when(partition.getOwner()).thenReturn(newOwner);
        doThrow(new MangleException(ErrorCode.GENERIC_ERROR)).when(taskService).triggerTask(anyString());
        when(configService.getClusterConfiguration()).thenReturn(clusterConfig);
        when(configService.updateClusterConfiguration(any())).thenReturn(clusterConfig);


        listener.memberRemoved(event);

        verify(event, times(4)).getMember();
        verify(newOwner, times(2)).getUuid();
        verify(cluster, times(1)).getLocalMember();
        verify(hazelcastInstance, times(3)).getCluster();
        verify(hazelcastInstance, times(1)).getMap("nodeTasks");
        verify(hazelcastInstance, times(1)).getPartitionService();
        verify(partitionService, times(2)).getPartition(anyString());
        verify(taskService, times(1)).triggerTask(anyString());
        verify(partition, times(2)).getOwner();
    }

    @Test
    public void testMemberRemovedNullExecutingTasks() throws UnknownHostException, MangleException {
        Member newOwner = mock(Member.class);
        Member oldOwner = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        Partition partition = mock(Partition.class);
        IMap<Object, Object> nodeTask = mock(IMap.class);
        MembershipEvent event = mock(MembershipEvent.class);

        String newMemberId = UUID.randomUUID().toString();
        String oldMemberId = UUID.randomUUID().toString();
        Address address = new Address("127.0.0.1", 90000);

        when(event.getMember()).thenReturn(oldOwner);
        when(cluster.getLocalMember()).thenReturn(newOwner);
        when(event.getCluster()).thenReturn(cluster);
        when(newOwner.getUuid()).thenReturn(newMemberId);
        when(oldOwner.getUuid()).thenReturn(oldMemberId);
        when(oldOwner.getAddress()).thenReturn(address);
        when(newOwner.getAddress()).thenReturn(address);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(hazelcastInstance.getMap("nodeTasks")).thenReturn(nodeTask);
        when(hazelcastInstance.getPartitionService()).thenReturn(partitionService);
        when(nodeTask.get(oldMemberId)).thenReturn(null);
        when(nodeTask.get(newMemberId)).thenReturn(null);
        when(partitionService.getPartition(anyString())).thenReturn(partition);
        when(partition.getOwner()).thenReturn(newOwner);
        when(configService.getClusterConfiguration()).thenReturn(clusterConfig);
        when(configService.updateClusterConfiguration(any())).thenReturn(clusterConfig);

        listener.memberRemoved(event);

        verify(event, times(3)).getMember();
        verify(newOwner, times(1)).getUuid();
        verify(cluster, times(1)).getLocalMember();
        verify(hazelcastInstance, times(3)).getCluster();
        verify(hazelcastInstance, times(1)).getMap("nodeTasks");
        verify(hazelcastInstance, times(1)).getPartitionService();
        verify(partitionService, times(0)).getPartition(anyString());
        verify(partition, times(0)).getOwner();
        verify(taskService, times(0)).triggerTask(anyString());
    }

    @Test
    public void testMemberRemovedEmptyExecutingTasks() throws UnknownHostException, MangleException {
        Member newOwner = mock(Member.class);
        Member oldOwner = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        Partition partition = mock(Partition.class);
        IMap<Object, Object> nodeTask = mock(IMap.class);
        MembershipEvent event = mock(MembershipEvent.class);

        String newMemberId = UUID.randomUUID().toString();
        String oldMemberId = UUID.randomUUID().toString();
        Address address = new Address("127.0.0.1", 90000);

        when(event.getMember()).thenReturn(oldOwner);
        when(cluster.getLocalMember()).thenReturn(newOwner);
        when(event.getCluster()).thenReturn(cluster);
        when(newOwner.getUuid()).thenReturn(newMemberId);
        when(oldOwner.getUuid()).thenReturn(oldMemberId);
        when(oldOwner.getAddress()).thenReturn(address);
        when(newOwner.getAddress()).thenReturn(address);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(hazelcastInstance.getMap("nodeTasks")).thenReturn(nodeTask);
        when(hazelcastInstance.getPartitionService()).thenReturn(partitionService);
        when(nodeTask.get(oldMemberId)).thenReturn(null);
        when(nodeTask.get(newMemberId)).thenReturn(new HashSet<Object>());
        when(partitionService.getPartition(anyString())).thenReturn(partition);
        when(partition.getOwner()).thenReturn(newOwner);
        when(configService.getClusterConfiguration()).thenReturn(clusterConfig);
        when(configService.updateClusterConfiguration(any())).thenReturn(clusterConfig);

        listener.memberRemoved(event);

        verify(event, times(3)).getMember();
        verify(newOwner, times(1)).getUuid();
        verify(cluster, times(1)).getLocalMember();
        verify(hazelcastInstance, times(3)).getCluster();
        verify(hazelcastInstance, times(1)).getMap("nodeTasks");
        verify(hazelcastInstance, times(1)).getPartitionService();
        verify(partitionService, times(0)).getPartition(anyString());
        verify(partition, times(0)).getOwner();
        verify(taskService, times(0)).triggerTask(anyString());
    }
}
