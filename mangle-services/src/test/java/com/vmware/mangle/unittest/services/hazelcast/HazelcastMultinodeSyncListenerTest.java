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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.UUID;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.Message;
import com.hazelcast.nio.Address;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.commons.ServiceCommonUtils;
import com.vmware.mangle.services.events.hazelcast.HazelcastSyncEvent;
import com.vmware.mangle.services.hazelcast.HazelcastMultinodeSyncListener;

/**
 * @author chetanc
 *
 *
 */
public class HazelcastMultinodeSyncListenerTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private HazelcastInstance hazelcastInstance;

    @Mock
    private UserService userService;

    @InjectMocks
    private HazelcastMultinodeSyncListener syncListener;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        syncListener.setHazelcastInstance(hazelcastInstance);
        ServiceCommonUtils.setApplicationContext(applicationContext);
    }

    @Test
    public void testOnMessage() throws UnknownHostException {
        String randomId = UUID.randomUUID().toString();
        HazelcastSyncEvent syncEvent = new HazelcastSyncEvent(UserService.class, randomId);
        Message message = mock(Message.class);
        Member newOwner = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        Address address = new Address("127.0.0.1", 90000);
        Member publishingMember = mock(Member.class);
        Address address1 = new Address("127.0.0.2", 90000);

        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(newOwner);
        when(newOwner.getAddress()).thenReturn(address);
        when(publishingMember.getAddress()).thenReturn(address1);
        when(message.getPublishingMember()).thenReturn(publishingMember);
        when(message.getMessageObject()).thenReturn(syncEvent);
        when(applicationContext.getBean(UserService.class)).thenReturn(userService);

        syncListener.onMessage(message);

        verify(cluster, times(1)).getLocalMember();
        verify(message, times(1)).getPublishingMember();
        verify(message, times(1)).getMessageObject();
        verify(applicationContext, times(1)).getBean(UserService.class);
        verify(newOwner, times(1)).getAddress();
        verify(hazelcastInstance, times(1)).getCluster();
    }


}
