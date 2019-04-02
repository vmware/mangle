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

package com.vmware.mangle.services;

import java.util.List;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.spi.properties.GroupProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.hazelcast.HazelcastClusterConfig;
import com.vmware.mangle.services.repository.ClusterConfigRepository;

/**
 * @author chetanc
 *
 */
@Service
public class ClusterConfigService implements HazelcastInstanceAware {
    private ClusterConfigRepository repository;

    private HazelcastInstance hazelcastInstance;

    @Autowired
    public ClusterConfigService(ClusterConfigRepository repository) {
        this.repository = repository;
    }

    public HazelcastClusterConfig getClusterConfiguration() {
        List<HazelcastClusterConfig> configs = repository.findAll();
        if (!CollectionUtils.isEmpty(configs)) {
            return configs.get(0);
        }
        return null;
    }

    public HazelcastClusterConfig addClusterConfiguration(HazelcastClusterConfig config) {
        return repository.insert(config);
    }

    public HazelcastClusterConfig updateClusterConfiguration(HazelcastClusterConfig config) {
        HazelcastClusterConfig persistance = repository.save(config);
        Config clusterConfig = hazelcastInstance.getConfig();
        clusterConfig.setProperty(GroupProperty.APPLICATION_VALIDATION_TOKEN.getName(),
                persistance.getValidationToken());
        clusterConfig.getGroupConfig().setName(persistance.getClusterName());
        return persistance;
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }
}
