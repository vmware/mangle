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
import com.hazelcast.core.MigrationEvent;
import com.hazelcast.core.Partition;
import com.hazelcast.core.PartitionService;
import com.hazelcast.nio.Address;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.services.enums.MangleQuorumStatus;
import com.vmware.mangle.services.hazelcast.HazelcastClusterMigrationListener;
import com.vmware.mangle.services.hazelcast.HazelcastTaskService;
import com.vmware.mangle.utils.constants.HazelcastConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author chetanc
 */
public class HazelcastClusterMigrationListenerTest {

    @Mock
    private HazelcastTaskService taskService;

    private HazelcastInstance hazelcastInstance;

    @Mock
    private PartitionService partitionService;

    @Mock
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @InjectMocks
    private HazelcastClusterMigrationListener listener;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        hazelcastInstance = mock(HazelcastInstance.class);
        when(hazelcastInstance.getPartitionService()).thenReturn(partitionService);
        listener.setHazelcastInstance(hazelcastInstance);
        doNothing().when(taskService).setHazelcastInstance(any());
        HazelcastConstants.setMangleQourumStatus(MangleQuorumStatus.PRESENT);

    }

    @AfterClass
    public void tearDown() {
        HazelcastConstants.setMangleQourumStatus(MangleQuorumStatus.NOT_PRESENT);
    }

    @Test
    public void testMigrationCompleted() throws UnknownHostException, MangleException {
        Member newOwner = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        Partition partition = mock(Partition.class);
        IMap<Object, Object> taskMap = mock(IMap.class);
        IMap<Object, Object> nodeTask = mock(IMap.class);
        MigrationEvent event = mock(MigrationEvent.class);

        String memberId = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();

        Set<Object> currentNodeTasks = new HashSet<Object>(Arrays.asList(key));

        Address address = new Address("127.0.0.1", 90000);

        when(event.getNewOwner()).thenReturn(newOwner);
        when(event.getOldOwner()).thenReturn(null);
        when(event.getPartitionId()).thenReturn(100);

        when(newOwner.getUuid()).thenReturn(memberId);
        when(newOwner.getAddress()).thenReturn(address);
        when(cluster.getLocalMember()).thenReturn(newOwner);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(hazelcastInstance.getMap("tasks")).thenReturn(taskMap);
        when(hazelcastInstance.getMap("nodeTasks")).thenReturn(nodeTask);
        when(hazelcastInstance.getPartitionService()).thenReturn(partitionService);

        when(nodeTask.get(memberId)).thenReturn(null);

        when(partitionService.getPartition(anyString())).thenReturn(partition);
        when(partition.getPartitionId()).thenReturn(100);
        when(taskMap.localKeySet()).thenReturn(currentNodeTasks);

        doNothing().when(taskService).triggerTask(anyString());


        listener.migrationCompleted(event);

        verify(event, times(4)).getNewOwner();
        verify(event, times(1)).getOldOwner();
        verify(event, times(2)).getPartitionId();

        verify(newOwner, times(1)).getUuid();
        verify(newOwner, times(4)).getAddress();
        verify(cluster, times(3)).getLocalMember();

        verify(hazelcastInstance, times(3)).getCluster();
        verify(hazelcastInstance, times(1)).getMap("tasks");
        verify(hazelcastInstance, times(1)).getMap("nodeTasks");
        verify(hazelcastInstance, times(1)).getPartitionService();

        verify(nodeTask, times(1)).get(memberId);

        verify(partitionService, times(1)).getPartition(anyString());
        verify(partition, times(1)).getPartitionId();
        verify(taskMap, times(1)).localKeySet();
    }

    @Test
    public void testMigrationCompletedTriggerFailure() throws UnknownHostException, MangleException {
        Member newOwner = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        Partition partition = mock(Partition.class);
        IMap<Object, Object> taskMap = mock(IMap.class);
        IMap<Object, Object> nodeTask = mock(IMap.class);
        MigrationEvent event = mock(MigrationEvent.class);

        String memberId = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();
        String value = TaskStatus.COMPLETED.name();

        Set<Object> currentNodeTasks = new HashSet<Object>(Arrays.asList(key));

        Address address = new Address("127.0.0.1", 90000);

        when(event.getNewOwner()).thenReturn(newOwner);
        when(event.getOldOwner()).thenReturn(null);
        when(event.getPartitionId()).thenReturn(100);

        when(newOwner.getUuid()).thenReturn(memberId);
        when(newOwner.getAddress()).thenReturn(address);
        when(cluster.getLocalMember()).thenReturn(newOwner);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(hazelcastInstance.getMap("tasks")).thenReturn(taskMap);
        when(hazelcastInstance.getMap("nodeTasks")).thenReturn(nodeTask);
        when(hazelcastInstance.getPartitionService()).thenReturn(partitionService);

        when(nodeTask.get(memberId)).thenReturn(currentNodeTasks);

        when(partitionService.getPartition(anyString())).thenReturn(partition);
        when(partition.getPartitionId()).thenReturn(100);
        when(taskMap.localKeySet()).thenReturn(currentNodeTasks);

        doNothing().when(taskService).triggerTask(anyString());


        listener.migrationCompleted(event);

        verify(event, times(4)).getNewOwner();
        verify(event, times(1)).getOldOwner();
        verify(event, times(2)).getPartitionId();

        verify(newOwner, times(1)).getUuid();
        verify(newOwner, times(3)).getAddress();
        verify(cluster, times(3)).getLocalMember();

        verify(hazelcastInstance, times(3)).getCluster();
        verify(hazelcastInstance, times(1)).getMap("tasks");
        verify(hazelcastInstance, times(1)).getMap("nodeTasks");
        verify(hazelcastInstance, times(1)).getPartitionService();

        verify(nodeTask, times(1)).get(memberId);

        verify(partitionService, times(1)).getPartition(anyString());
        verify(partition, times(1)).getPartitionId();
        verify(taskService, times(0)).triggerTask(anyString());
        verify(taskMap, times(1)).localKeySet();
    }

    @Test
    public void testMigrationStarted() throws MangleException, UnknownHostException {
        Member newOwner = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        Partition partition = mock(Partition.class);
        IMap<Object, Object> taskMap = mock(IMap.class);
        IMap<Object, Object> nodeTask = mock(IMap.class);
        MigrationEvent event = mock(MigrationEvent.class);

        String memberId = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();
        String value = TaskStatus.COMPLETED.name();

        Set<Object> currentNodeTasks = new HashSet<Object>(Arrays.asList(key));

        Address address = new Address("127.0.0.1", 90000);

        when(event.getNewOwner()).thenReturn(newOwner);
        when(event.getOldOwner()).thenReturn(null);
        when(event.getPartitionId()).thenReturn(100);

        when(newOwner.getUuid()).thenReturn(memberId);
        when(newOwner.getAddress()).thenReturn(address);
        when(cluster.getLocalMember()).thenReturn(newOwner);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(hazelcastInstance.getMap("tasks")).thenReturn(taskMap);
        when(hazelcastInstance.getMap("nodeTasks")).thenReturn(nodeTask);
        when(hazelcastInstance.getPartitionService()).thenReturn(partitionService);

        when(nodeTask.get(memberId)).thenReturn(currentNodeTasks);

        when(partitionService.getPartition(anyString())).thenReturn(partition);
        when(partition.getPartitionId()).thenReturn(100);
        when(taskMap.localKeySet()).thenReturn(currentNodeTasks);

        doNothing().when(taskService).cleanUpTaskForMigration(anyString());


        listener.migrationStarted(event);

        verify(event, times(1)).getOldOwner();

        verify(cluster, times(1)).getLocalMember();

        verify(hazelcastInstance, times(1)).getCluster();

        verify(taskService, times(0)).triggerTask(anyString());

    }

}
