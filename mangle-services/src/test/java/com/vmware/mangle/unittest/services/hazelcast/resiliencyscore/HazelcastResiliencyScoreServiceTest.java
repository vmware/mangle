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

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.model.resiliencyscore.ResiliencyScoreProperties;
import com.vmware.mangle.services.hazelcast.resiliencyscore.HazelcastResiliencyScoreService;
import com.vmware.mangle.services.helpers.ResiliencyScoreHelper;
import com.vmware.mangle.services.helpers.ResiliencyScoreTaskHelper;
import com.vmware.mangle.services.mockdata.ResiliencyScoreMockData;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreService;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreTaskExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * @author dbhat
 */
public class HazelcastResiliencyScoreServiceTest {

    @Mock
    private HazelcastInstance hazelcastInstance;
    @Mock
    private ResiliencyScoreHelper resiliencyScoreHelper;
    @Mock
    private ResiliencyScoreService resiliencyScoreService;
    @Mock
    private ResiliencyScoreTaskExecutor resiliencyScoreTaskExecutor;
    @Mock
    private ResiliencyScoreTaskHelper resiliencyScoreTaskHelper;

    @InjectMocks
    private HazelcastResiliencyScoreService hazelcastResiliencyScoreService;

    private Cluster cluster;
    private Member member;
    private Address address;
    private String memberId;
    private String key;
    private Set<Object> currentNodeTasks;
    private IMap<Object, Object> map;

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
        currentNodeTasks = new HashSet<Object>(Arrays.asList(key));
        map = mock(IMap.class);
    }

    @Test
    public void testTriggeringCompletedTask() throws MangleException {
        ResiliencyScoreTask taskSpec = ResiliencyScoreMockData.getResiliencyScoreTask1();
        taskSpec.setTaskStatus(TaskStatus.COMPLETED);
        IMap<Object, Object> map = mock(IMap.class);

        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(member.getAddress()).thenReturn(address);
        when(hazelcastInstance.getMap(any())).thenReturn(map);
        when(map.get(memberId)).thenReturn(currentNodeTasks);
        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(taskSpec);
        when(hazelcastInstance.getMap(any())).thenReturn(map);

        hazelcastResiliencyScoreService.triggerTask(taskSpec.getId());
        verify(resiliencyScoreHelper, times(0)).getResiliencyScoreTaskSpec();
    }

    @Test
    public void testTriggeringFailedTask() throws MangleException {
        ResiliencyScoreTask taskSpec = ResiliencyScoreMockData.getResiliencyScoreTask1();
        taskSpec.setTaskStatus(TaskStatus.FAILED);

        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(member.getAddress()).thenReturn(address);
        when(hazelcastInstance.getMap(any())).thenReturn(map);
        when(map.get(memberId)).thenReturn(currentNodeTasks);
        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(taskSpec);

        hazelcastResiliencyScoreService.triggerTask(taskSpec.getId());
        verify(resiliencyScoreHelper, times(0)).getResiliencyScoreTaskSpec();
    }

    @Test
    public void testTriggeringInProgressTask() throws MangleException {
        ResiliencyScoreTask taskSpec = ResiliencyScoreMockData.getResiliencyScoreTask1();
        taskSpec.setTaskStatus(TaskStatus.IN_PROGRESS);

        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(member.getAddress()).thenReturn(address);
        when(hazelcastInstance.getMap(any())).thenReturn(map);
        when(map.get(memberId)).thenReturn(currentNodeTasks);
        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(taskSpec);

        hazelcastResiliencyScoreService.triggerTask(taskSpec.getId());
        verify(resiliencyScoreHelper, times(0)).getResiliencyScoreTaskSpec();
    }

    @Test
    public void testTriggeringInitialisingTask() throws MangleException {
        ResiliencyScoreTask taskSpec = ResiliencyScoreMockData.getResiliencyScoreTask1();
        taskSpec.setTaskStatus(TaskStatus.INITIALIZING);
        ResiliencyScoreProperties properties = ResiliencyScoreMockData.getResiliencyScoreProperties();
        Task<? extends TaskSpec> task = ResiliencyScoreMockData.getTask();

        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(member.getAddress()).thenReturn(address);
        when(hazelcastInstance.getMap(any())).thenReturn(map);
        when(map.get(memberId)).thenReturn(currentNodeTasks);
        when(resiliencyScoreService.getTaskById(anyString())).thenReturn(taskSpec);
        when(resiliencyScoreHelper.getResiliencyScoreTaskSpec()).thenReturn(properties);
        doNothing().when(resiliencyScoreTaskExecutor).submitTask(any());
        when(resiliencyScoreTaskHelper.init(any())).thenReturn(task);

        hazelcastResiliencyScoreService.triggerTask(taskSpec.getId());
        verify(resiliencyScoreHelper, times(1)).getResiliencyScoreTaskSpec();

    }


}
