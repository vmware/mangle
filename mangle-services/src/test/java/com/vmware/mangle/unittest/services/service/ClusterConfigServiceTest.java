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

package com.vmware.mangle.unittest.services.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.spi.properties.GroupProperty;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.hazelcast.HazelcastClusterConfig;
import com.vmware.mangle.model.enums.MangleDeploymentMode;
import com.vmware.mangle.services.ClusterConfigService;
import com.vmware.mangle.services.commons.ServiceCommonUtils;
import com.vmware.mangle.services.enums.MangleQuorumStatus;
import com.vmware.mangle.services.hazelcast.HazelcastSyncTopicManager;
import com.vmware.mangle.services.mockdata.ClusterConfigMockdata;
import com.vmware.mangle.services.repository.ClusterConfigRepository;
import com.vmware.mangle.utils.constants.HazelcastConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author chetanc
 */
public class ClusterConfigServiceTest {

    @Mock
    private ClusterConfigRepository configRepository;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private HazelcastInstance hazelcastInstance;

    private ClusterConfigService clusterConfigService;

    private ClusterConfigMockdata mockdata = new ClusterConfigMockdata();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        clusterConfigService = new ClusterConfigService(configRepository);
        clusterConfigService.setHazelcastInstance(hazelcastInstance);
        HazelcastConstants.setMangleQourumStatus(MangleQuorumStatus.PRESENT);
        ServiceCommonUtils.setApplicationContext(applicationContext);
    }

    @Test
    public void testGetClusterConfiguration() {
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        List<HazelcastClusterConfig> configs = new ArrayList<>(Arrays.asList(config));
        when(configRepository.findAll()).thenReturn(configs);

        HazelcastClusterConfig persisted = clusterConfigService.getClusterConfiguration();

        Assert.assertEquals(persisted, config);
        verify(configRepository, times(1)).findAll();
    }

    @Test
    public void testGetClusterConfigurationNoPersistence() {
        List<HazelcastClusterConfig> configs = new ArrayList<>();
        when(configRepository.findAll()).thenReturn(configs);

        HazelcastClusterConfig persisted = clusterConfigService.getClusterConfiguration();

        Assert.assertEquals(persisted, null);
        verify(configRepository, times(1)).findAll();
    }

    @Test
    public void testAddClusterConfiguration() {
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        when(configRepository.insert(config)).thenReturn(config);

        HazelcastClusterConfig persisted = clusterConfigService.addClusterConfiguration(config);

        Assert.assertEquals(persisted, config);
        verify(configRepository, times(1)).insert(config);
    }

    @Test
    public void testUpdateClusterConfiguration() {
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        Config hazelcastConfig = new Config();
        GroupConfig hazelcastGroupConfig = new GroupConfig();
        hazelcastConfig.setGroupConfig(hazelcastGroupConfig);

        when(hazelcastInstance.getConfig()).thenReturn(hazelcastConfig);
        when(configRepository.save(config)).thenReturn(config);

        clusterConfigService.updateClusterConfiguration(config);

        Assert.assertEquals(hazelcastGroupConfig.getName(), config.getClusterName());
        Assert.assertEquals(hazelcastConfig.getProperty(GroupProperty.APPLICATION_VALIDATION_TOKEN.getName()),
                config.getValidationToken());
        verify(configRepository, times(1)).save(any());
    }

    @Test
    public void testResync() {
        Member member = mock(Member.class);
        Member member2 = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        members.add(member);
        members.add(member2);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getMembers()).thenReturn(members);
        when(cluster.getLocalMember()).thenReturn(member2);
        doNothing().when(hazelcastInstance).shutdown();

        clusterConfigService.resync("");

        Assert.assertEquals(HazelcastConstants.getMangleQourumStatus(), MangleQuorumStatus.NOT_PRESENT);

        verify(hazelcastInstance, times(2)).getCluster();
        verify(hazelcastInstance, times(1)).shutdown();
        verify(cluster, times(1)).getMembers();
        verify(cluster, times(1)).getLocalMember();
    }

    @Test
    public void testResyncClusterMode() {
        Member member = mock(Member.class);
        Member member2 = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        members.add(member2);
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        config.setDeploymentMode(MangleDeploymentMode.CLUSTER);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getMembers()).thenReturn(members);
        when(cluster.getLocalMember()).thenReturn(member2);
        doNothing().when(hazelcastInstance).shutdown();

        clusterConfigService.resync("");

        Assert.assertEquals(HazelcastConstants.getMangleQourumStatus(), MangleQuorumStatus.PRESENT);

        verify(hazelcastInstance, times(1)).getCluster();
        verify(hazelcastInstance, times(0)).shutdown();
        verify(cluster, times(1)).getMembers();
        verify(cluster, times(0)).getLocalMember();
    }

    @Test
    public void testUpdateMangleQuorum() throws MangleException {
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        config.setDeploymentMode(MangleDeploymentMode.CLUSTER);
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);
        Member member = mock(Member.class);
        Member member2 = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        members.add(member2);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getMembers()).thenReturn(members);


        HazelcastClusterConfig persistentConfig = clusterConfigService.updateMangleQuorum(2);

        Assert.assertEquals(persistentConfig.getQuorum(), Integer.valueOf(2));
        verify(configRepository, times(1)).findAll();
        verify(configRepository, times(1)).save(any());
        verify(applicationContext, times(1)).getBean(HazelcastSyncTopicManager.class);
        verify(topicManager, times(1)).triggerSyncEvent(any(), anyString());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testUpdateMangleQuorumException() throws MangleException {
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);
        Member member = mock(Member.class);
        Member member2 = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        members.add(member2);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getMembers()).thenReturn(members);

        try {
            clusterConfigService.updateMangleQuorum(1);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.CLUSTER_CONFIG_LESSER_QUORUM);

            verify(configRepository, times(0)).findAll();
            verify(configRepository, times(0)).save(any());
            verify(applicationContext, times(0)).getBean(HazelcastSyncTopicManager.class);
            verify(topicManager, times(0)).triggerSyncEvent(any(), anyString());
            throw e;
        }
    }

    @Test
    public void testUpdateMangleDeploymentTypeCluster() throws MangleException {
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);
        Config hazelcastConfig = new Config();
        Member member = mock(Member.class);
        Member member2 = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        members.add(member2);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getConfig()).thenReturn(hazelcastConfig);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getMembers()).thenReturn(members);

        HazelcastClusterConfig persistentConfig =
                clusterConfigService.updateMangleDeploymentType(MangleDeploymentMode.CLUSTER);

        Assert.assertEquals(persistentConfig.getQuorum(), Integer.valueOf(2));
        Assert.assertEquals(HazelcastConstants.getMangleQourum(), 2);
        Assert.assertEquals(hazelcastConfig.getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE),
                MangleDeploymentMode.CLUSTER.name());

        verify(configRepository, times(1)).findAll();
        verify(configRepository, times(1)).save(any());
        verify(applicationContext, times(1)).getBean(HazelcastSyncTopicManager.class);
        verify(topicManager, times(1)).triggerSyncEvent(any(), anyString());
    }

    @Test
    public void testUpdateMangleDeploymentTypeStandalone() throws MangleException {
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        config.setDeploymentMode(MangleDeploymentMode.CLUSTER);
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);
        Config hazelcastConfig = new Config();
        Member member = mock(Member.class);
        Member member2 = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        members.add(member2);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getConfig()).thenReturn(hazelcastConfig);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getMembers()).thenReturn(members);

        HazelcastClusterConfig persistentConfig =
                clusterConfigService.updateMangleDeploymentType(MangleDeploymentMode.STANDALONE);

        Assert.assertEquals(persistentConfig.getQuorum(), Integer.valueOf(1));
        Assert.assertEquals(HazelcastConstants.getMangleQourum(), 1);
        Assert.assertEquals(hazelcastConfig.getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE),
                MangleDeploymentMode.STANDALONE.name());

        verify(configRepository, times(1)).findAll();
        verify(configRepository, times(1)).save(any());
        verify(applicationContext, times(1)).getBean(HazelcastSyncTopicManager.class);
        verify(topicManager, times(1)).triggerSyncEvent(any(), anyString());
    }

    @Test
    public void testHandleQuorumForNewAddition() {
        Member member = mock(Member.class);
        Member member2 = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        members.add(member2);
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        Config hazelcastConfig = new Config();
        hazelcastConfig.setProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE,
                MangleDeploymentMode.CLUSTER.name());
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getConfig()).thenReturn(hazelcastConfig);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getMembers()).thenReturn(members);

        clusterConfigService.handleQuorumForNewNodeAddition();


        Assert.assertEquals(config.getQuorum(), Integer.valueOf(1));
        Assert.assertEquals(HazelcastConstants.getMangleQourum(), 1);
        Assert.assertEquals(hazelcastConfig.getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE),
                MangleDeploymentMode.CLUSTER.name());
        verify(hazelcastInstance, times(2)).getCluster();
        verify(cluster, times(1)).getMembers();
    }


}
