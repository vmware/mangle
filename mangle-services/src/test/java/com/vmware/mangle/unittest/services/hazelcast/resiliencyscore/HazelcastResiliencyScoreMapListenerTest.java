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

package com.vmware.mangle.unittest.services.hazelcast.resiliencyscore;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryEventType;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.nio.Address;
import com.hazelcast.partition.PartitionLostEvent;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.services.hazelcast.resiliencyscore.HazelcastResiliencyScoreMapListener;
import com.vmware.mangle.services.hazelcast.resiliencyscore.HazelcastResiliencyScoreService;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author dbhat
 */
public class HazelcastResiliencyScoreMapListenerTest {

    @Mock
    private HazelcastResiliencyScoreService resiliencyScoreService;

    @Mock
    private IMap<Object, Object> map;

    @Mock
    private IMap<Object, Object> nodeMap;

    @Mock
    private HazelcastInstance hz;

    @InjectMocks
    private HazelcastResiliencyScoreMapListener listener;

    private Cluster cluster;
    private Member member;
    private Address address;
    private String memberId;
    private String key;

    @BeforeMethod
    public void initMocks() throws UnknownHostException {
        MockitoAnnotations.initMocks(this);
        initData();
    }

    private void initData() throws UnknownHostException {
        cluster = mock(Cluster.class);
        member = mock(Member.class);
        address = new Address("127.0.0.1", 90000);
        memberId = UUID.randomUUID().toString();
        key = UUID.randomUUID().toString();
        new HashSet<Object>(Arrays.asList(key));
    }

    @Test
    public void validateEntryAddedEvent() throws MangleException, UnknownHostException {
        String value = TaskStatus.COMPLETED.name();

        when(hz.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(member.getAddress()).thenReturn(address);
        doNothing().when(resiliencyScoreService).triggerTask(key);

        EntryEvent<String, String> event =
                new EntryEvent<String, String>(memberId, member, EntryEventType.ADDED.getType(), key, value);

        listener.entryAdded(event);

        verify(hz, times(1)).getCluster();
        verify(cluster, times(1)).getLocalMember();
        verify(member, times(1)).getAddress();
        verify(resiliencyScoreService, times(1)).triggerTask(key);
    }

    @Test
    public void testOnEvent() {
        PartitionLostEvent partition = mock(PartitionLostEvent.class);

        when(partition.getPartitionId()).thenReturn(100);

        listener.onEvent(partition);
        verify(partition, times(1)).getPartitionId();
    }


}
