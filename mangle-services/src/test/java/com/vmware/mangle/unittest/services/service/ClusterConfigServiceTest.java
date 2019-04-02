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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.properties.GroupProperty;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.hazelcast.HazelcastClusterConfig;
import com.vmware.mangle.services.ClusterConfigService;
import com.vmware.mangle.services.mockdata.ClusterConfigMockdata;
import com.vmware.mangle.services.repository.ClusterConfigRepository;

/**
 *
 *
 * @author chetanc
 */
public class ClusterConfigServiceTest {

    @Mock
    private ClusterConfigRepository configRepository;

    @Mock
    private HazelcastInstance hazelcastInstance;

    private ClusterConfigService clusterConfigService;

    private ClusterConfigMockdata mockdata = new ClusterConfigMockdata();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        clusterConfigService = new ClusterConfigService(configRepository);
        clusterConfigService.setHazelcastInstance(hazelcastInstance);
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

        HazelcastClusterConfig persisted = clusterConfigService.updateClusterConfiguration(config);

        Assert.assertEquals(hazelcastGroupConfig.getName(), config.getClusterName());
        Assert.assertEquals(hazelcastConfig.getProperty(GroupProperty.APPLICATION_VALIDATION_TOKEN.getName()),
                config.getValidationToken());
        verify(configRepository, times(1)).save(any());
    }


}
