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

import java.net.UnknownHostException;
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
import com.hazelcast.nio.Address;
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
        HazelcastConstants.setMangleQourum(2);
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

        Assert.assertNull(persisted);
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

    /**
     * Shouldn't process the event when there is only one node in the cluster,
     * and the event is triggered because the deployment mode is modified from
     * cluster to standalone
     */
    @Test
    public void testResyncClusterModeSingleMember() {
        Member member = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getMembers()).thenReturn(members);
        doNothing().when(hazelcastInstance).shutdown();

        clusterConfigService.resync("");

        Assert.assertEquals(HazelcastConstants.getMangleQourumStatus(), MangleQuorumStatus.PRESENT);

        verify(hazelcastInstance, times(1)).getCluster();
        verify(hazelcastInstance, times(0)).shutdown();
        verify(cluster, times(1)).getMembers();
        verify(cluster, times(0)).getLocalMember();
    }

    /**
     * Test resync method when the event is processed by the master
     * 1. Resync method was triggered because of changing the deployment mode
     * to the standalone mode, from cluster
     * 2. Number of the cluster members are more than 1
     *
     *
     * In this case the event should not be processed, when the deployment mode is changed from
     * cluster to standalone, all other nodes except master will trigger hazelcast shutdown
     */
    @Test
    public void testResyncClusterEventOnMasterNode() {
        Member member = mock(Member.class);
        Member member1 = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        members.add(member1);
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getMembers()).thenReturn(members);
        doNothing().when(hazelcastInstance).shutdown();
        when(cluster.getLocalMember()).thenReturn(member);

        clusterConfigService.resync("");

        Assert.assertEquals(HazelcastConstants.getMangleQourumStatus(), MangleQuorumStatus.PRESENT);

        verify(hazelcastInstance, times(2)).getCluster();
        verify(hazelcastInstance, times(0)).shutdown();
        verify(cluster, times(1)).getMembers();
        verify(cluster, times(1)).getLocalMember();
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

    /**
     * When user tries to configure the quorum value which is lesser than the minimum
     * possible quorum value
     *
      * @throws MangleException
     */
    @Test(expectedExceptions = MangleException.class)
    public void testUpdateMangleQuorumLesserQuorumConfiguration() throws MangleException {
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

    /**
     * When user tries to configure value below what is allowed for cluster mode
     * of the deployment, which is 2(Minimum quorum value that should be configured for
     * cluster deployment mode)
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = MangleException.class)
    public void testUpdateMangleQuorumLesserThanForClusterDeploymentMode() throws MangleException {
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);
        Member member = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        config.setDeploymentMode(MangleDeploymentMode.CLUSTER);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getMembers()).thenReturn(members);

        try {
            clusterConfigService.updateMangleQuorum(1);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.CLUSTER_TYPE_CONFIG_LESSER_QUORUM);

            verify(configRepository, times(1)).findAll();
            verify(configRepository, times(0)).save(any());
            verify(applicationContext, times(0)).getBean(HazelcastSyncTopicManager.class);
            verify(topicManager, times(0)).triggerSyncEvent(any(), anyString());
            throw e;
        }
    }

    /**
     * When user tries to set the quorum value greater than 1 for
     * the standalone mode. Quorum value for the standalone mode of
     * deployment can only be 1.
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = MangleException.class)
    public void testUpdateMangleQuorumGreaterThanForStandaloneDeploymentMode() throws MangleException {
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);
        Member member = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getMembers()).thenReturn(members);

        try {
            clusterConfigService.updateMangleQuorum(2);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.CLUSTER_QUORUM_UPDATE_STANDADLONE_FAILURE);

            verify(configRepository, times(1)).findAll();
            verify(configRepository, times(0)).save(any());
            verify(applicationContext, times(0)).getBean(HazelcastSyncTopicManager.class);
            verify(topicManager, times(0)).triggerSyncEvent(any(), anyString());
            throw e;
        }
    }

    /**
     * When the quorum value what user is trying to set is same as what is already
     * configured in the application
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = MangleException.class)
    public void testUpdateMangleQuorumAlreadyInState() throws MangleException {
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);
        Member member = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getMembers()).thenReturn(members);

        try {
            clusterConfigService.updateMangleQuorum(1);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.CLUSTER_ALREADY_IN_STATE);

            verify(configRepository, times(1)).findAll();
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

    /**
     * Updating the deployment mode to cluster, and the current quorum value is
     * less than 2 in the DB. This is a general scenario, when the cluster was in standalone mode,
     * and user modifies that to cluster. The quorum value is updated to 2
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateMangleDeploymentTypeClusterWithQuorumLessThanTwo() throws MangleException {
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);
        Config hazelcastConfig = new Config();
        Member member = mock(Member.class);
        Member member2 = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        members.add(member2);
        config.setQuorum(2);

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

    /**
     * When the user tries to set the deployment mode, but the application is already running
     * in the same deployment mode
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = MangleException.class)
    public void testUpdateMangleDeploymentTypeAlreadyInRequestedState() throws MangleException {
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        config.setDeploymentMode(MangleDeploymentMode.CLUSTER);
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));

        try {
            HazelcastClusterConfig persistentConfig = clusterConfigService.updateMangleDeploymentType(MangleDeploymentMode.CLUSTER);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.CLUSTER_ALREADY_IN_STATE);

            verify(configRepository, times(1)).findAll();
            verify(configRepository, times(0)).save(any());
            verify(applicationContext, times(0)).getBean(HazelcastSyncTopicManager.class);
            verify(topicManager, times(0)).triggerSyncEvent(any(), anyString());

            throw e;
        }
    }

    /**
     * General scenario:
     * When new node is added to the cluster, this block of code will only be executed on the master node, if it has active quorum
     *
     * Following actions will be executed
     * 1. Calculate the possible quorum value
     * 2. If the current quorum value is lesser than the possible,
     * update to the possible quorum value into both db and local cache
     * 3. Trigger for resync
     *
     * @throws UnknownHostException
     */
    @Test
    public void testHandleQuorumForNewAddition() throws UnknownHostException {
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
        Address address = new Address("127.0.0.1", 90000);
        Address address2 = new Address("127.0.0.2", 90000);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getConfig()).thenReturn(hazelcastConfig);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(cluster.getMembers()).thenReturn(members);
        when(member.getAddress()).thenReturn(address);
        when(member2.getAddress()).thenReturn(address2);

        clusterConfigService.handleQuorumForNewNodeAddition();

        Assert.assertEquals(config.getQuorum(), Integer.valueOf(2), "DB cluster quorum value mismatch");
        Assert.assertEquals(HazelcastConstants.getMangleQourum(), 2, "in memory cluster quorum value mismatch");
        Assert.assertEquals(hazelcastConfig.getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE),
                MangleDeploymentMode.CLUSTER.name());

        verify(hazelcastInstance, times(4)).getCluster();
        verify(hazelcastInstance, times(2)).getConfig();
        verify(applicationContext, times(1)).getBean(HazelcastSyncTopicManager.class);
        verify(configRepository, times(1)).findAll();
        verify(configRepository, times(1)).save(config);
        verify(cluster, times(3)).getMembers();
        verify(cluster, times(1)).getLocalMember();
        verify(member, times(1)).getAddress();
        verify(member2, times(1)).getAddress();

    }

    /**
     * when hazelcastInstance is not already instantiated in the current
     * mangle instance, nothing is updated
     * @throws UnknownHostException
     */
    @Test
    public void testHandleQuorumForNewAdditionNoHazelcastInstance() throws UnknownHostException {

        clusterConfigService.setHazelcastInstance(null);
        clusterConfigService.handleQuorumForNewNodeAddition();

        Assert.assertEquals(HazelcastConstants.getMangleQourum(), 2, "in memory cluster quorum value mismatch");

        verify(hazelcastInstance, times(0)).getCluster();
        verify(hazelcastInstance, times(0)).getConfig();
        verify(applicationContext, times(0)).getBean(HazelcastSyncTopicManager.class);
        verify(configRepository, times(0)).findAll();
    }

    /**
     * When the quorum value is null in the DB
     *
     * This issue was noticed during the upgrade of the mangle from 1.0 to 2.0
     * As the columns, quorum and deploymentMode were introduced in the release 2.0
     *
     * @throws UnknownHostException
     */
    @Test
    public void testHandleQuorumForNewAdditionWithNullQuorumInConfig() throws UnknownHostException {
        Member member = mock(Member.class);
        Member member2 = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        members.add(member2);
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        config.setQuorum(null);
        Config hazelcastConfig = new Config();
        hazelcastConfig.setProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE,
                MangleDeploymentMode.CLUSTER.name());
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);
        Address address = new Address("127.0.0.1", 90000);
        Address address2 = new Address("127.0.0.2", 90000);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getConfig()).thenReturn(hazelcastConfig);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(cluster.getMembers()).thenReturn(members);
        when(member.getAddress()).thenReturn(address);
        when(member2.getAddress()).thenReturn(address2);

        clusterConfigService.handleQuorumForNewNodeAddition();

        Assert.assertEquals(config.getQuorum(), Integer.valueOf(2), "DB cluster quorum value mismatch");
        Assert.assertEquals(HazelcastConstants.getMangleQourum(), 2, "in memory cluster quorum value mismatch");
        Assert.assertEquals(hazelcastConfig.getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE),
                MangleDeploymentMode.CLUSTER.name());

        verify(hazelcastInstance, times(4)).getCluster();
        verify(hazelcastInstance, times(2)).getConfig();
        verify(applicationContext, times(1)).getBean(HazelcastSyncTopicManager.class);
        verify(configRepository, times(1)).findAll();
        verify(configRepository, times(1)).save(config);
        verify(cluster, times(3)).getMembers();
        verify(cluster, times(1)).getLocalMember();
        verify(member, times(1)).getAddress();
        verify(member2, times(1)).getAddress();
    }

    /**
     * When the new node is added, but the cluster still doesn't meet the quorum
     * In this scenario, the db won't be updated with the list of the members or
     * master
     *
     * @throws UnknownHostException
     */
    @Test
    public void testHandleQuorumForNewAdditionNoQuorumPresent() throws UnknownHostException {
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
        HazelcastConstants.setMangleQourumStatus(MangleQuorumStatus.NOT_PRESENT);
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getConfig()).thenReturn(hazelcastConfig);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(cluster.getMembers()).thenReturn(members);

        clusterConfigService.handleQuorumForNewNodeAddition();

        Assert.assertEquals(config.getQuorum(), Integer.valueOf(1), "DB cluster quorum value mismatch");
        Assert.assertEquals(HazelcastConstants.getMangleQourum(), 2, "in memory cluster quorum value mismatch");
        Assert.assertEquals(hazelcastConfig.getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE),
                MangleDeploymentMode.CLUSTER.name());

        verify(hazelcastInstance, times(2)).getCluster();
        verify(hazelcastInstance, times(2)).getConfig();
        verify(applicationContext, times(0)).getBean(HazelcastSyncTopicManager.class);
        verify(configRepository, times(0)).findAll();
        verify(configRepository, times(0)).save(config);
        verify(cluster, times(1)).getMembers();
        verify(cluster, times(1)).getLocalMember();
    }

    /**
     * when the current node is not the oldest node(master) of the cluster
     *
     * @throws UnknownHostException
     */
    @Test
    public void testHandleQuorumForNewAdditionNotOldestNode() throws UnknownHostException {
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
        Address address = new Address("127.0.0.1", 90000);
        Address address2 = new Address("127.0.0.2", 90000);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getConfig()).thenReturn(hazelcastConfig);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member2);
        when(cluster.getMembers()).thenReturn(members);
        when(member.getAddress()).thenReturn(address);
        when(member2.getAddress()).thenReturn(address2);

        clusterConfigService.handleQuorumForNewNodeAddition();

        Assert.assertEquals(config.getQuorum(), Integer.valueOf(1), "DB cluster quorum value mismatch");
        Assert.assertEquals(HazelcastConstants.getMangleQourum(), 2, "in memory cluster quorum value mismatch");
        Assert.assertEquals(hazelcastConfig.getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE),
                MangleDeploymentMode.CLUSTER.name());

        verify(hazelcastInstance, times(2)).getCluster();
        verify(hazelcastInstance, times(2)).getConfig();
        verify(applicationContext, times(0)).getBean(HazelcastSyncTopicManager.class);
        verify(configRepository, times(0)).findAll();
        verify(configRepository, times(0)).save(config);
        verify(cluster, times(1)).getMembers();
        verify(cluster, times(1)).getLocalMember();
        verify(member, times(0)).getAddress();
        verify(member2, times(0)).getAddress();
    }

    /**
     * When the deployment mode that is maintained in the hazelcast group property is not
     * yet initialized. This scenario usually arises when a node is still initializing
     * @throws UnknownHostException
     */
    @Test
    public void testHandleQuorumForNewAdditionNoDeploymentPropertySet() throws UnknownHostException {
        Member member = mock(Member.class);
        Member member2 = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        members.add(member2);
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        Config hazelcastConfig = new Config();
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);
        Address address = new Address("127.0.0.1", 90000);
        Address address2 = new Address("127.0.0.2", 90000);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getConfig()).thenReturn(hazelcastConfig);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member2);
        when(cluster.getMembers()).thenReturn(members);
        when(member.getAddress()).thenReturn(address);
        when(member2.getAddress()).thenReturn(address2);

        clusterConfigService.handleQuorumForNewNodeAddition();

        Assert.assertEquals(config.getQuorum(), Integer.valueOf(1), "DB cluster quorum value mismatch");
        Assert.assertEquals(HazelcastConstants.getMangleQourum(), 2, "in memory cluster quorum value mismatch");
        Assert.assertNull(hazelcastConfig.getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE));

        verify(hazelcastInstance, times(0)).getCluster();
        verify(hazelcastInstance, times(1)).getConfig();
        verify(applicationContext, times(0)).getBean(HazelcastSyncTopicManager.class);
        verify(configRepository, times(0)).findAll();
        verify(configRepository, times(0)).save(config);
        verify(cluster, times(0)).getMembers();
        verify(cluster, times(0)).getLocalMember();
        verify(member, times(0)).getAddress();
        verify(member2, times(0)).getAddress();
    }

    /**
     * When the quorum value in the DB is less than the possible quorum value,
     * Quorum value is updated in the DB and in the local mangle cache
     * @throws UnknownHostException
     */
    @Test
    public void testHandleQuorumForNewAdditionCurrentDBQuorumLessThanPossible() throws UnknownHostException {
        Member member = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        Config hazelcastConfig = new Config();
        hazelcastConfig.setProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE,
                MangleDeploymentMode.CLUSTER.name());
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);
        Address address = new Address("127.0.0.1", 90000);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getConfig()).thenReturn(hazelcastConfig);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(cluster.getMembers()).thenReturn(members);
        when(member.getAddress()).thenReturn(address);

        clusterConfigService.handleQuorumForNewNodeAddition();

        Assert.assertEquals(config.getQuorum(), Integer.valueOf(1), "DB cluster quorum value mismatch");
        Assert.assertEquals(HazelcastConstants.getMangleQourum(), 2, "in memory cluster quorum value mismatch");
        Assert.assertEquals(hazelcastConfig.getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE),
                MangleDeploymentMode.CLUSTER.name());

        verify(hazelcastInstance, times(4)).getCluster();
        verify(hazelcastInstance, times(2)).getConfig();
        verify(applicationContext, times(1)).getBean(HazelcastSyncTopicManager.class);
        verify(configRepository, times(1)).findAll();
        verify(configRepository, times(1)).save(config);
        verify(cluster, times(3)).getMembers();
        verify(cluster, times(1)).getLocalMember();
        verify(member, times(1)).getAddress();
    }

    /**
     * When the value in the mangle local cache is less than the possible quorum value,
     * both db and local cache values will be updated to the possible quorum value
     *
     * @throws UnknownHostException
     */
    @Test
    public void testHandleQuorumForNewAdditionCurrentQuorumLessThanPossible() throws UnknownHostException {
        Member member = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        Config hazelcastConfig = new Config();
        hazelcastConfig.setProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE,
                MangleDeploymentMode.CLUSTER.name());
        HazelcastConstants.setMangleQourum(0);
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);
        Address address = new Address("127.0.0.1", 90000);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getConfig()).thenReturn(hazelcastConfig);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(cluster.getMembers()).thenReturn(members);
        when(member.getAddress()).thenReturn(address);

        clusterConfigService.handleQuorumForNewNodeAddition();

        Assert.assertEquals(config.getQuorum(), Integer.valueOf(1), "DB cluster quorum value mismatch");
        Assert.assertEquals(HazelcastConstants.getMangleQourum(), 1, "in memory cluster quorum value mismatch");
        Assert.assertEquals(hazelcastConfig.getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE),
                MangleDeploymentMode.CLUSTER.name());

        verify(hazelcastInstance, times(4)).getCluster();
        verify(hazelcastInstance, times(2)).getConfig();
        verify(applicationContext, times(1)).getBean(HazelcastSyncTopicManager.class);
        verify(configRepository, times(1)).findAll();
        verify(configRepository, times(1)).save(config);
        verify(cluster, times(3)).getMembers();
        verify(cluster, times(1)).getLocalMember();
        verify(member, times(1)).getAddress();

    }

    /**
     * When the new node is added to the cluster but the deployment mode configure is
     * STANDALONE
     *
     * @throws UnknownHostException
     */
    @Test
    public void testHandleQuorumForNewAdditionNotClusterDeployment() throws UnknownHostException {
        Member member = mock(Member.class);
        Member member2 = mock(Member.class);
        Cluster cluster = mock(Cluster.class);
        HashSet<Member> members = new LinkedHashSet<>();
        members.add(member);
        members.add(member2);
        HazelcastClusterConfig config = mockdata.getClusterConfigObject();
        Config hazelcastConfig = new Config();
        hazelcastConfig.setProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE,
                MangleDeploymentMode.STANDALONE.name());
        HazelcastSyncTopicManager topicManager = mock(HazelcastSyncTopicManager.class);
        Address address = new Address("127.0.0.1", 90000);
        Address address2 = new Address("127.0.0.2", 90000);

        when(configRepository.findAll()).thenReturn(Collections.singletonList(config));
        when(configRepository.save(any())).thenReturn(config);
        when(applicationContext.getBean(HazelcastSyncTopicManager.class)).thenReturn(topicManager);
        when(hazelcastInstance.getConfig()).thenReturn(hazelcastConfig);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(cluster.getMembers()).thenReturn(members);
        when(member.getAddress()).thenReturn(address);
        when(member2.getAddress()).thenReturn(address2);

        clusterConfigService.handleQuorumForNewNodeAddition();

        Assert.assertEquals(config.getQuorum(), Integer.valueOf(1), "DB cluster quorum value mismatch");
        Assert.assertEquals(HazelcastConstants.getMangleQourum(), 2, "in memory cluster quorum value mismatch");
        Assert.assertEquals(hazelcastConfig.getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE),
                MangleDeploymentMode.STANDALONE.name());

        verify(hazelcastInstance, times(0)).getCluster();
        verify(hazelcastInstance, times(2)).getConfig();
        verify(applicationContext, times(0)).getBean(HazelcastSyncTopicManager.class);
        verify(configRepository, times(0)).findAll();
        verify(configRepository, times(0)).save(config);
        verify(cluster, times(0)).getMembers();
        verify(cluster, times(0)).getLocalMember();
        verify(member, times(0)).getAddress();
        verify(member2, times(0)).getAddress();
    }

}
