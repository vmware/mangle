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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import com.hazelcast.core.Member;
import com.hazelcast.quorum.QuorumEvent;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.services.config.MangleBootInitializer;
import com.vmware.mangle.services.enums.MangleQuorumStatus;
import com.vmware.mangle.services.hazelcast.HazelcastQuorumListener;
import com.vmware.mangle.services.hazelcast.HazelcastTaskCache;
import com.vmware.mangle.services.scheduler.Scheduler;
import com.vmware.mangle.utils.constants.HazelcastConstants;

/**
 * @author chetanc
 *
 *
 */
public class HazelcastQuorumListenerTest {

    @Mock
    private MangleBootInitializer bootInitializer;

    @Mock
    private Scheduler scheduler;

    @Mock
    private HazelcastTaskCache taskCache;

    private HazelcastQuorumListener hazelcastQuorumListener;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        hazelcastQuorumListener = new HazelcastQuorumListener(bootInitializer, scheduler, taskCache);
    }

    @Test
    public void testOnChangeQuorumPresent() {
        Member member = Mockito.mock(Member.class);
        QuorumEvent quorumEvent = new QuorumEvent(this, 1, Collections.singletonList(member), true);

        doNothing().when(bootInitializer).initializeApplicationTasks();

        hazelcastQuorumListener.onChange(quorumEvent);

        Assert.assertEquals(HazelcastConstants.getMangleQourumStatus(), MangleQuorumStatus.PRESENT);
        verify(bootInitializer, times(1)).initializeApplicationTasks();
    }

    @Test
    public void testOnChangeQuorumNotPresent() {
        Member member = Mockito.mock(Member.class);
        QuorumEvent quorumEvent = new QuorumEvent(this, 1, Collections.singletonList(member), false);

        doNothing().when(scheduler).removeAllSchedulesFromCurrentNode();

        hazelcastQuorumListener.onChange(quorumEvent);

        Assert.assertEquals(HazelcastConstants.getMangleQourumStatus(), MangleQuorumStatus.NOT_PRESENT);
        verify(scheduler, times(1)).removeAllSchedulesFromCurrentNode();
    }

}
