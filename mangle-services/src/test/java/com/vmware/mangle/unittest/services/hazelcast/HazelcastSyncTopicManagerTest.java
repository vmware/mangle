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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.hazelcast.HazelcastMultinodeSyncListener;
import com.vmware.mangle.services.hazelcast.HazelcastSyncTopicManager;
import com.vmware.mangle.utils.constants.HazelcastConstants;

/**
 * @author chetanc
 *
 *
 */
public class HazelcastSyncTopicManagerTest {

    @Mock
    private HazelcastMultinodeSyncListener syncListener;

    @InjectMocks
    private HazelcastSyncTopicManager topicManager;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testTriggerSyncEvent() {
        String string1 = UUID.randomUUID().toString();
        String[] strings = new String[] { string1 };

        HazelcastInstance hazelcastInstance = mock(HazelcastInstance.class);
        ITopic topic = mock(ITopic.class);

        when(hazelcastInstance.getTopic(HazelcastConstants.HAZELCAST_MANGLE_SYNC_TOPIC_NAME)).thenReturn(topic);
        topicManager.setHazelcastInstance(hazelcastInstance);


        topicManager.triggerSyncEvent(UserService.class, strings);

        verify(topic, times(1)).publish(any());
    }
}
