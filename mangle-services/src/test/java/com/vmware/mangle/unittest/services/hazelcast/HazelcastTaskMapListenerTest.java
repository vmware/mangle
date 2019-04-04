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
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryEventType;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.Partition;
import com.hazelcast.core.PartitionService;
import com.hazelcast.nio.Address;
import com.hazelcast.partition.PartitionLostEvent;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.services.hazelcast.HazelcastTaskMapListener;
import com.vmware.mangle.services.hazelcast.HazelcastTaskService;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author chetanc
 */
public class HazelcastTaskMapListenerTest {

    @Mock
    private HazelcastTaskService hazelcastTaskService;

    @Mock
    private IMap<Object, Object> map;

    @Mock
    private IMap<Object, Object> nodeMap;

    private HazelcastInstance hz;

    @InjectMocks
    private HazelcastTaskMapListener listener;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        hz = mock(HazelcastInstance.class);
        listener.setHazelcastInstance(hz);
    }


    @Test
    public void testEntryUpdatedInprogressStatus() {
        String memberId = UUID.randomUUID().toString();
        Member member = mock(Member.class);
        String key = UUID.randomUUID().toString();
        String value = TaskStatus.IN_PROGRESS.name();

        EntryEvent<String, String> event =
                new EntryEvent<>(memberId, member, EntryEventType.UPDATED.getType(), key, value);
        listener.entryUpdated(event);
    }

    @Test
    public void testEntryRemovedInprogressStatus() {
        String memberId = UUID.randomUUID().toString();
        PartitionService partitionService = mock(PartitionService.class);
        Member member = mock(Member.class);
        Partition partition = mock(Partition.class);
        String key = UUID.randomUUID().toString();
        String value = TaskStatus.IN_PROGRESS.name();

        Set<String> nodeTasks = new HashSet<>(Arrays.asList(key, UUID.randomUUID().toString()));


        when(hz.getPartitionService()).thenReturn(partitionService);
        when(partitionService.getPartition(anyString())).thenReturn(partition);
        when(partition.getOwner()).thenReturn(member);
        when(member.getUuid()).thenReturn(memberId);
        when(hz.getMap("nodeTasks")).thenReturn(nodeMap);
        when(nodeMap.get(memberId)).thenReturn(nodeTasks);


        EntryEvent<String, String> event =
                new EntryEvent<>(memberId, member, EntryEventType.REMOVED.getType(), key, value);
        listener.entryRemoved(event);

        verify(hz, times(1)).getPartitionService();
        verify(partitionService, times(1)).getPartition(key);
        verify(partition, times(1)).getOwner();
        verify(member, times(1)).getUuid();
        verify(hz, times(1)).getMap(any());
        verify(nodeMap, times(1)).get(memberId);
    }

    @Test
    public void testEntryRemovedNoNodeTasks() {
        String memberId = UUID.randomUUID().toString();
        PartitionService partitionService = mock(PartitionService.class);
        Member member = mock(Member.class);
        Partition partition = mock(Partition.class);
        String key = UUID.randomUUID().toString();
        String value = TaskStatus.IN_PROGRESS.name();

        Set<String> nodeTasks = new HashSet<>();


        when(hz.getPartitionService()).thenReturn(partitionService);
        when(partitionService.getPartition(anyString())).thenReturn(partition);
        when(partition.getOwner()).thenReturn(member);
        when(member.getUuid()).thenReturn(memberId);
        when(hz.getMap("nodeTasks")).thenReturn(nodeMap);
        when(nodeMap.get(memberId)).thenReturn(nodeTasks);


        EntryEvent<String, String> event =
                new EntryEvent<>(memberId, member, EntryEventType.REMOVED.getType(), key, value);
        listener.entryRemoved(event);

        verify(hz, times(1)).getPartitionService();
        verify(partitionService, times(1)).getPartition(key);
        verify(partition, times(1)).getOwner();
        verify(member, times(1)).getUuid();
        verify(hz, times(1)).getMap(any());
        verify(nodeMap, times(1)).get(memberId);
        verify(nodeMap, times(0)).put(anyString(), anyString());
    }

    @Test
    public void testEntryAdded() throws MangleException, UnknownHostException {
        String memberId = UUID.randomUUID().toString();
        Cluster cluster = mock(Cluster.class);
        Member member = mock(Member.class);
        Address address = new Address("127.0.0.1", 90000);

        String key = UUID.randomUUID().toString();
        String value = TaskStatus.COMPLETED.name();

        when(hz.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(member.getAddress()).thenReturn(address);
        doNothing().when(hazelcastTaskService).triggerTask(key);

        EntryEvent<String, String> event =
                new EntryEvent<String, String>(memberId, member, EntryEventType.ADDED.getType(), key, value);

        listener.entryAdded(event);

        verify(hz, times(1)).getCluster();
        verify(cluster, times(1)).getLocalMember();
        verify(member, times(1)).getAddress();
        verify(hazelcastTaskService, times(1)).triggerTask(key);
    }

    @Test
    public void testOnEvent() {
        PartitionLostEvent partition = mock(PartitionLostEvent.class);

        when(partition.getPartitionId()).thenReturn(100);

        listener.onEvent(partition);
        verify(partition, times(1)).getPartitionId();
    }
}
