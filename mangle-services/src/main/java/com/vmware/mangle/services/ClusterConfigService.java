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
import java.util.Set;
import java.util.stream.Collectors;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.Member;
import com.hazelcast.spi.properties.GroupProperty;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.hazelcast.HazelcastClusterConfig;
import com.vmware.mangle.model.enums.MangleDeploymentMode;
import com.vmware.mangle.services.enums.MangleQuorumStatus;
import com.vmware.mangle.services.hazelcast.HazelcastClusterSyncAware;
import com.vmware.mangle.services.repository.ClusterConfigRepository;
import com.vmware.mangle.utils.HazelcastUtils;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.HazelcastConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 *
 */
@Service
@Log4j2
public class ClusterConfigService implements HazelcastInstanceAware, HazelcastClusterSyncAware {
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

    /**
     * Updates the Cluster config object in the DB
     *
     * @param config
     * @return
     */
    public HazelcastClusterConfig updateClusterConfiguration(HazelcastClusterConfig config) {
        log.info("Updating cluster config");
        HazelcastClusterConfig persistance = repository.save(config);
        Config clusterConfig = hazelcastInstance.getConfig();
        clusterConfig.setProperty(GroupProperty.APPLICATION_VALIDATION_TOKEN.getName(),
                persistance.getValidationToken());
        clusterConfig.getGroupConfig().setName(persistance.getClusterName());
        return persistance;
    }

    /**
     * Updates the deployment mode of the cluster, and triggers resync across the cluster
     *
     * @param deploymentMode
     * @return Updated cluster config object
     * @throws MangleException
     *             If provided deployment mode is same as the one that is already provided
     */
    public HazelcastClusterConfig updateMangleDeploymentType(MangleDeploymentMode deploymentMode)
            throws MangleException {
        log.info("Updating mangle deployment type to {}", deploymentMode.name());
        HazelcastClusterConfig persitentConfig = getClusterConfiguration();

        if (persitentConfig.getDeploymentMode() == deploymentMode) {
            throw new MangleException(ErrorConstants.CLUSTER_ALREADY_IN_STATE, ErrorCode.CLUSTER_ALREADY_IN_STATE,
                    ErrorConstants.DEPLOYMENT_MODE, deploymentMode.name());
        }

        persitentConfig.setDeploymentMode(deploymentMode);
        if (persitentConfig.getDeploymentMode() == MangleDeploymentMode.STANDALONE) {
            persitentConfig.setQuorum(1);
        } else if (persitentConfig.getQuorum() < 2) {
            persitentConfig.setQuorum(2);
        }

        persitentConfig = repository.save(persitentConfig);
        HazelcastUtils.updateHazelcastMangleQuorumValue(persitentConfig.getQuorum());
        hazelcastInstance.getConfig().setProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE,
                deploymentMode.name());
        triggerMultiNodeResync("");
        handleResyncForClusterConfig(persitentConfig);
        return persitentConfig;
    }

    /**
     * Updates the quorum value of the cluster, and triggers re-sync across all other nodes if
     * modified
     *
     * @param quorum
     * @return
     * @throws MangleException
     *
     *             1. if quorum value provided is lesser than the possible value, possible value is
     *             identifed by n/2+1 n, the number of nodes in the current cluster
     *
     *             2. If the quorum value is lesser than 2, and deployment mode is CLUSTER
     *
     *             3. If the user tries to gives the same value which is already configured
     */
    public HazelcastClusterConfig updateMangleQuorum(int quorum) throws MangleException {
        int possibleQuorumValue = hazelcastInstance.getCluster().getMembers().size() / 2 + 1;

        if (possibleQuorumValue > quorum) {
            throw new MangleException(String.format(ErrorConstants.CLUSTER_CONFIG_LESSER_QUORUM, possibleQuorumValue),
                    ErrorCode.CLUSTER_CONFIG_LESSER_QUORUM, possibleQuorumValue);
        }

        HazelcastClusterConfig persistentConfig = getClusterConfiguration();

        if (persistentConfig.getDeploymentMode() == MangleDeploymentMode.CLUSTER && quorum < 2) {
            throw new MangleException(ErrorConstants.CLUSTER_TYPE_CONFIG_LESSER_QUORUM,
                    ErrorCode.CLUSTER_TYPE_CONFIG_LESSER_QUORUM);
        }

        if (persistentConfig.getQuorum() == quorum) {
            throw new MangleException(ErrorConstants.CLUSTER_ALREADY_IN_STATE, ErrorCode.CLUSTER_ALREADY_IN_STATE,
                    ErrorConstants.CLUSTER_QUORUM, quorum);
        }

        persistentConfig.setQuorum(quorum);
        persistentConfig = repository.save(persistentConfig);
        HazelcastUtils.updateHazelcastMangleQuorumValue(quorum);
        triggerMultiNodeResync("");
        return persistentConfig;
    }


    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    /**
     * Called when new node joins the cluster, will be handled on the oldest node of the hazelcast
     * cluster(master node)
     *
     * 1. Updates the members list in the db
     *
     * 2. Updates the quorum value of the cluster
     *
     * 3. Triggers the re-sync across the cluster for cluster config update
     */
    public void handleQuorumForNewNodeAddition() {
        if (hazelcastInstance != null && isClusterDeploymentModeCluster() && isCurrentNodeOldestMember()
                && HazelcastConstants.mangleQourumStatus == MangleQuorumStatus.PRESENT) {
            int possibleQuorumValue = hazelcastInstance.getCluster().getMembers().size() / 2 + 1;
            HazelcastClusterConfig config = getClusterConfiguration();

            Set<Member> clusterActiveMembers = hazelcastInstance.getCluster().getMembers();

            Set<String> activeMembers = clusterActiveMembers.stream().map(member -> member.getAddress().getHost())
                    .collect(Collectors.toSet());
            config.setMembers(activeMembers);
            if (null == config.getQuorum() || HazelcastConstants.mangleQourum < possibleQuorumValue) {
                log.info("updating quorum value to {}", possibleQuorumValue);
                config.setQuorum(possibleQuorumValue);
                HazelcastUtils.updateHazelcastMangleQuorumValue(possibleQuorumValue);
            }
            repository.save(config);
            triggerMultiNodeResync("");
        }
    }

    @Override
    public void resync(String objectIdentifier) {
        HazelcastClusterConfig config = getClusterConfiguration();
        handleResyncForClusterConfig(config);
    }

    /**
     *
     * 1. Updates the quorum value in the current node context
     *
     * 2. Shuts down the hazelcast instance on the current node, if the deployment mode is changed
     * to standalone mode from cluster
     *
     * @param config
     *            Persisted cluster config object
     */
    private void handleResyncForClusterConfig(HazelcastClusterConfig config) {
        Set<Member> members = hazelcastInstance.getCluster().getMembers();
        HazelcastUtils.updateHazelcastMangleQuorumValue(config.getQuorum());
        if (config.getDeploymentMode() == MangleDeploymentMode.STANDALONE && members.size() > 1
                && members.iterator().next() != hazelcastInstance.getCluster().getLocalMember()) {
            log.error("Deployment mode changed to standalone. Current node instance is shutting down");
            HazelcastUtils.updateHazelcastQuorumStatus(MangleQuorumStatus.NOT_PRESENT);
            hazelcastInstance.shutdown();
        }
    }

    /**
     * Verifies if the deployment mode is CLUSTER, which is maintained in the hazelcast property
     *
     * @return
     */
    private boolean isClusterDeploymentModeCluster() {
        return null != hazelcastInstance.getConfig().getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE)
                && hazelcastInstance.getConfig().getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE)
                        .equals(MangleDeploymentMode.CLUSTER.name());
    }

    /**
     * Checks if the current member is the oldest member in the cluster(hazelcast master node)
     *
     * @return
     */
    private boolean isCurrentNodeOldestMember() {
        return hazelcastInstance.getCluster().getLocalMember() == hazelcastInstance.getCluster().getMembers().iterator()
                .next();
    }
}
