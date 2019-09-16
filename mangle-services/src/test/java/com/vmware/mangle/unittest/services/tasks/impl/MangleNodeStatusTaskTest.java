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

package com.vmware.mangle.unittest.services.tasks.impl;


import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;

import static com.vmware.mangle.utils.constants.HazelcastConstants.HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.instance.MemberImpl;
import com.hazelcast.nio.Address;
import lombok.extern.log4j.Log4j2;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.task.MangleNodeStatusDto;
import com.vmware.mangle.services.admin.tasks.NodeStatusTask;
import com.vmware.mangle.services.enums.MangleNodeStatus;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.CustomErrorMessage;

/**
 * @author bkaranam (bhanukiran karanam)
 */
@Log4j2
@PrepareForTest(value = { CommonUtils.class })
@PowerMockIgnore({ "org.apache.logging.log4j.*", "com.sun.org.*" })
public class MangleNodeStatusTaskTest extends PowerMockTestCase {

    @InjectMocks
    private NodeStatusTask<MangleNodeStatusDto> nodeStatusUpdateTask;

    @Mock
    HazelcastInstance instance;

    @Mock
    CustomErrorMessage customErrorMessage;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @BeforeMethod
    public void initMocks() {
        PowerMockito.mockStatic(CommonUtils.class);
        MockitoAnnotations.initMocks(this);
    }

    @Mock
    private ApplicationEventPublisher publisher;

    @Test(priority = 0, expectedExceptions = UnsupportedOperationException.class)
    public void testTaskExecutionWithSingleNode() throws UnknownHostException {

        try {
            Cluster cluster = mock(Cluster.class);
            Address address = new Address("127.0.0.1", 90000);
            Member newOwner = new MemberImpl(address, null, true);
            Set<Member> clusterMembers = new HashSet<>();
            clusterMembers.add(newOwner);
            MangleNodeStatusDto updateDto = new MangleNodeStatusDto();
            updateDto.setNodeStatus(MangleNodeStatus.PAUSE);
            Task<MangleNodeStatusDto> task = nodeStatusUpdateTask.init(updateDto);
            Assert.assertTrue(task.isInitialized());
            when(instance.getCluster()).thenReturn(cluster);
            when(cluster.getLocalMember()).thenReturn(newOwner);
            when(cluster.getMembers()).thenReturn(clusterMembers);
            nodeStatusUpdateTask.setEventPublisher(eventPublisher);
            nodeStatusUpdateTask.run(task);
            Assert.assertEquals(eventPublisher, nodeStatusUpdateTask.getEventPublisher());
            verify(instance, times(2)).getCluster();
            verify(cluster, times(1)).getMembers();
            verify(cluster, times(1)).getLocalMember();
            assertNull(nodeStatusUpdateTask.getInfo(task));
            nodeStatusUpdateTask.cancel();
            nodeStatusUpdateTask.init(task, task.getTaskData());

        } catch (MangleException e) {
            log.error("testMockTaskInjectionExecution failed with exception: ", e);
            Assert.assertTrue(false);
        }
    }

    @Test(priority = 1)
    public void testTaskExecutionWithTwoNodes() throws UnknownHostException {
        Cluster cluster = mock(Cluster.class);
        try {
            Address address1 = new Address("127.0.0.1", 90000);
            Member localMember = new MemberImpl(address1, null, true);
            localMember.setStringAttribute(HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE,
                    MangleNodeStatus.ACTIVE.name());
            Address address2 = new Address("127.0.0.1", 90001);
            Member member2 = new MemberImpl(address2, null, true);
            member2.setStringAttribute(HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE, MangleNodeStatus.ACTIVE.name());
            Set<Member> clusterMembers = new HashSet<>();
            clusterMembers.add(localMember);
            clusterMembers.add(member2);
            MangleNodeStatusDto updateDto = new MangleNodeStatusDto();
            updateDto.setNodeStatus(MangleNodeStatus.MAINTENANCE_MODE);
            Task<MangleNodeStatusDto> task = nodeStatusUpdateTask.init(updateDto);
            Assert.assertTrue(task.isInitialized());
            when(instance.getCluster()).thenReturn(cluster);
            when(cluster.getLocalMember()).thenReturn(localMember);
            when(cluster.getMembers()).thenReturn(clusterMembers);
            PowerMockito.doNothing().when(CommonUtils.class, "delayInSeconds", anyInt());
            nodeStatusUpdateTask.run(task);
        } catch (MangleRuntimeException e) {
            Assert.assertTrue(true);
            verify(instance, times(2)).getCluster();
            verify(cluster, times(1)).getMembers();
            verify(cluster, times(1)).getLocalMember();
        } catch (Exception e) {
            log.error("testMockTaskInjectionExecution failed with exception: ", e);
            Assert.assertTrue(false);
        }
    }


}
