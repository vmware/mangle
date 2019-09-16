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

package com.vmware.mangle.services.hazelcast;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.net.InetAddresses;
import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.config.QuorumConfig;
import com.hazelcast.config.QuorumListenerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.quorum.QuorumType;
import com.hazelcast.spi.properties.GroupProperty;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.hazelcast.HazelcastClusterConfig;
import com.vmware.mangle.model.enums.MangleDeploymentMode;
import com.vmware.mangle.services.ClusterConfigService;
import com.vmware.mangle.services.application.listener.CustomApplicationListener;
import com.vmware.mangle.utils.HazelcastUtils;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.HazelcastConstants;
import com.vmware.mangle.utils.constants.MetricProviderConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author chetanc
 */
@Configuration
@EnableConfigurationProperties
@Log4j2
public class HazelcastConfig {

    private HazelcastClusterMigrationListener migrationListener;
    private HazelcastClusterMembershipListener membershipListener;
    private CustomApplicationListener applicationListener;
    private HazelcastTaskService hazelcastTaskService;
    private HazelcastTaskCache mapService;
    private ClusterConfigService clusterConfigService;
    private HazelcastSyncTopicManager syncTopicManager;
    private HazelcastQuorumListener quorumListener;
    private ReplicatedMap<String, Boolean> nodeStatus;
    private HazelcastInstance hazelcastInstance;

    @Value("${hazelcast.config.public}")
    private String publicAddress;

    @Value("${hazelcast.config.members}")
    private String clusterMembers;

    @Value("${hazelcast.config.validationToken}")
    private String clusterValidationToken;

    @Value("${hazelcast.config.cluster.name}")
    private String clusterName;

    @Value("${hazelcast.config.deploymentMode}")
    private String deploymentMode;

    private static final String NAME = HazelcastConstants.HAZELCAST_TASKS_MAP;

    @Autowired
    public HazelcastConfig(ClusterConfigService configService) {
        this.clusterConfigService = configService;
    }

    @Autowired
    public void setMembershipListener(HazelcastClusterMembershipListener membershipListener) {
        this.membershipListener = membershipListener;
    }

    @Autowired
    public void setMigrationListener(HazelcastClusterMigrationListener migrationListener) {
        this.migrationListener = migrationListener;
    }

    @Autowired
    public void setApplicationListener(CustomApplicationListener applicationListener) {
        this.applicationListener = applicationListener;
    }

    @Autowired
    public void setHazelcastTaskService(HazelcastTaskService hazelcastTaskService) {
        this.hazelcastTaskService = hazelcastTaskService;
    }

    @Autowired
    public void setMapService(HazelcastTaskCache mapService) {
        this.mapService = mapService;
    }

    @Autowired
    public void setSyncTopicManager(HazelcastSyncTopicManager syncTopicManager) {
        this.syncTopicManager = syncTopicManager;
    }

    @Autowired
    public void setQuorumListener(HazelcastQuorumListener quorumListener) {
        this.quorumListener = quorumListener;
    }

    /**
     *
     * Create the hazelcast instance in the embedded mode. The cluster created has the following
     * properties
     *
     * 1. User can configure the public address for the hazelcast instance >
     * https://docs.hazelcast.org/docs/latest/manual/html-single/#public-address public-address
     * overrides the public address of a member. By default, a member selects its socket address as
     * its public address. But behind a network address translation (NAT), two endpoints (members)
     * may not be able to see/access each other. If both members set their public addresses to their
     * defined addresses on NAT, then that way they can communicate with each other
     *
     * 2. Hazelcast instances in the cluster tries to discover each other through tcp/ip >
     * https://docs.hazelcast.org/docs/latest/manual/html-single/#discovering-members-by-tcp At
     * least one of the listed members has to be active in the cluster when a new member joins for
     * it to become the part of the cluster
     *
     * 3. Hazelcast instances form the cluster with the name identified by the constanct >
     * URLConstants.HAZELCAST_CLUSTER_NAME
     *
     * 4. Hazelcast listeners are configured to handle i. ClusterMembership Events > member leaving
     * the event will re-trigger the tasks on the new active member ii. ClusterMigration Events >
     * member leaving the cluster triggers the partition ownership migration for the map partitions
     * that were owner by the member leaving the cluster
     *
     * @return
     */
    @Bean(name = "hazelcastInstance")
    public HazelcastInstance createHazelcastCluster() throws MangleException {

        if (StringUtils.isEmpty(clusterValidationToken)) {
            throw new MangleException(ErrorConstants.CLUSTER_VALIDATION_TOKEN_MISSING,
                    ErrorCode.CLUSTER_MANDATORY_PARAMETER_NOT_PROVIDED);
        }

        if (StringUtils.isEmpty(publicAddress)) {
            throw new MangleException(ErrorConstants.PUBLIC_ADDRESS_MISSING,
                    ErrorCode.CLUSTER_MANDATORY_PARAMETER_NOT_PROVIDED);
        }

        if (!InetAddresses.isInetAddress(publicAddress)) {
            throw new MangleException(ErrorConstants.PUBLIC_ADDRESS_WRONG_FORMAT,
                    ErrorCode.CLUSTER_MANDATORY_PARAMETER_ERROR);
        }

        Config config = getClusterConfig();
        hazelcastInstance = Hazelcast.newHazelcastInstance(config);

        if (null != config.getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE)
                && config.getProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE)
                        .equals(MangleDeploymentMode.STANDALONE.name())
                && hazelcastInstance.getCluster().getMembers().size() > 1) {
            throw new MangleException(
                    String.format(ErrorConstants.HZ_STANDALONE_ALREADY_EXISTS,
                            hazelcastInstance.getCluster().getMembers().iterator().next().getAddress().getHost()),
                    ErrorCode.HZ_STANDALONE_ALREADY_EXISTS);
        }
        System.setProperty(MetricProviderConstants.NODE_ADDRESS,
                hazelcastInstance.getCluster().getLocalMember().getAddress().getHost());
        migrationListener.setHazelcastInstance(hazelcastInstance);
        membershipListener.setHazelcastInstance(hazelcastInstance);
        hazelcastTaskService.setHazelcastInstance(hazelcastInstance);
        mapService.setHazelcastInstance(hazelcastInstance);
        applicationListener.setHazelcastInstance(hazelcastInstance);
        clusterConfigService.setHazelcastInstance(hazelcastInstance);
        syncTopicManager.setHazelcastInstance(hazelcastInstance);
        return hazelcastInstance;
    }

    private Config getClusterConfig() throws MangleException {
        Config config = new Config();
        HazelcastClusterConfig clusterConfig = clusterConfigService.getClusterConfiguration();

        MemberAttributeConfig mangleMaintenanceModeConfig = new MemberAttributeConfig();
        mangleMaintenanceModeConfig.setBooleanAttribute("MANGLE_IN_MAINTENANCE_MODE", false);
        String validationToken = HazelcastUtils.getApplicationValidationToken(clusterValidationToken);
        config.setProperty(GroupProperty.APPLICATION_VALIDATION_TOKEN.getName(), validationToken);
        config.setMemberAttributeConfig(mangleMaintenanceModeConfig);
        config.addListenerConfig(new ListenerConfig(membershipListener));
        config.addListenerConfig(new ListenerConfig(migrationListener));
        config.addListenerConfig(new ListenerConfig(new HazelcastLifeCycleListener()));
        List<String> members = HazelcastUtils.getMembersList(clusterMembers);

        /**
         * if the cluster configuration is not setup already, it will initialize and save it in DB,
         * further when new nodes are trying to join the existing cluster they should pass the same
         * validation token, and cluster name of that cluster
         *
         * This cluster configuration is stored in the db under the table "cluster"
         *
         *
         * 1. If the persistence(DB) is already configured to be part of any cluster, to use that
         * persistence, one should give the same validation token and cluster name, that is stored
         * in the "cluster" table of the persistence.
         *
         * 2. If the token and cluster name doesn't match, application will fail to start
         *
         */
        if (clusterConfig == null) {
            clusterConfig = new HazelcastClusterConfig();
            Set<String> membersSet = new HashSet<>(members);
            clusterConfig.setValidationToken(validationToken);
            clusterConfig.setMembers(membersSet);
            clusterConfig.setClusterName(clusterName);
            clusterConfig.setDeploymentMode(extractMangleDeploymentMode(deploymentMode));
            clusterConfig.setQuorum(extractQuorum(null));
            clusterConfigService.addClusterConfiguration(clusterConfig);
            log.info("Bringing up the cluster with the validation token: {}", validationToken);
        } else {
            validateHazelcastInitialization(clusterConfig, validationToken);

            /**
             * if there already exists a cluster configuration in the persistence layer of the
             * application, it maintains the list of active members in the cluster This list of
             * active members will be added to current hazelcast cluster member
             * initialization configuration, so that it can try to reach any of the active member to
             * join that cluster
             */
            if (!CollectionUtils.isEmpty(clusterConfig.getMembers())) {
                members.addAll(clusterConfig.getMembers());
            }
        }

        String deploymentModeVal;
        if (clusterConfig.getDeploymentMode() != null) {
            deploymentModeVal = clusterConfig.getDeploymentMode().name();
        } else {
            deploymentModeVal = extractMangleDeploymentMode(deploymentMode).name();
        }

        config.setProperty(HazelcastConstants.HAZELCAST_PROPERTY_DEPLOYMENT_MODE, deploymentModeVal);
        /**
         * Public address with which the mangle instance can be accessed by another cluster members.
         *
         * https://docs.hazelcast.org/docs/latest/manual/html-single/#public-address
         */
        if (!StringUtils.isEmpty(publicAddress)) {
            config.getNetworkConfig().setPublicAddress(publicAddress);
        }

        /* Setting up tcp/ip discovery for the hazelcast cluster */
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setMembers(members).setEnabled(true);

        HazelcastUtils.updateHazelcastMangleQuorumValue(extractQuorum(clusterConfig));
        QuorumConfig quorumConfig = getQuorumConfig();
        config.addQuorumConfig(quorumConfig);

        /*
         * Setting up hazelcast cluster with a name
         * provided either by the user, or default "mangle"
         */
        config.getGroupConfig().setName(clusterName);
        return config;
    }

    private QuorumConfig getQuorumConfig() {
        QuorumConfig quorumConfig = new QuorumConfig();
        quorumConfig.setName(NAME);
        quorumConfig.setEnabled(true);
        quorumConfig.setType(QuorumType.READ_WRITE);

        quorumConfig.setQuorumFunctionImplementation(this::isQuorumPresent);
        QuorumListenerConfig listenerConfig = new QuorumListenerConfig();
        // You can either directly set quorum listener implementation of your own
        listenerConfig.setImplementation(quorumListener);
        quorumConfig.addListenerConfig(listenerConfig);
        return quorumConfig;
    }

    /**
     * Method to check the presence of quorum
     *
     * 1. Quorum is said to be present if the number of the members in the clusters is more than
     * number of quorum, and
     *
     * 2. If number of active members is equal to quorum value(status maintained in nodeStatus
     * replicated map)
     *
     * @param members
     * @return
     */
    private boolean isQuorumPresent(Collection<Member> members) {
        int numOfActiveMembers = 0;
        if (hazelcastInstance == null) {
            return false;
        }
        if (nodeStatus == null) {
            nodeStatus = hazelcastInstance.getReplicatedMap(HazelcastConstants.MANGLE_APPLICATION_STATUS_MAP);
        }

        for (Member member : members) {
            if (nodeStatus.containsKey(member.getAddress().getHost())
                    && nodeStatus.get(member.getAddress().getHost())) {
                numOfActiveMembers++;
            }
        }
        log.trace("Nodes status maintained are: {}", nodeStatus.keySet().toString());
        return numOfActiveMembers >= HazelcastConstants.mangleQourum
                && members.size() >= HazelcastConstants.mangleQourum;
    }

    private void validateHazelcastInitialization(HazelcastClusterConfig clusterConfig, String validationToken)
            throws MangleException {
        /*Validating if the validation token given matches the one configured in the db*/
        if (StringUtils.isEmpty(clusterConfig.getValidationToken())
                || !(clusterConfig.getValidationToken().equals(validationToken)
                        && clusterConfig.getClusterName().equals(clusterName))) {
            log.error(
                    "Mangle failed to start up, mis-matching validation token with the one configured in db, provided: {}",
                    validationToken);
            throw new MangleException(ErrorConstants.CLUSTER_CONFIG_MISMATCH_VALIDATION_TOKEN,
                    ErrorCode.CLUSTER_CONFIG_MISMATCH_VALIDATION_TOKEN);
        }

        /**
         * If the node joined another node which is running on a different mode of deployment, this
         * will throw an exception fail to boot the second node
         */
        if (null != clusterConfig.getDeploymentMode()
                && !clusterConfig.getDeploymentMode().name().equals(deploymentMode)) {
            throw new MangleException(ErrorConstants.CLUSTER_CONFIG_MISMATCH_DEPLOYMENT_MODE,
                    ErrorCode.CLUSTER_CONFIG_MISMATCH_DEPLOYMENT_TYPE);
        }
    }


    private int extractQuorum(HazelcastClusterConfig clusterConfig) {
        int clusterQuorum = 1;
        if (clusterConfig != null && null != clusterConfig.getQuorum()) {
            clusterQuorum = clusterConfig.getQuorum();
        }

        if (MangleDeploymentMode.CLUSTER.name().equals(deploymentMode) && clusterQuorum < 2) {
            clusterQuorum = 2;
        }

        return clusterQuorum;
    }

    private MangleDeploymentMode extractMangleDeploymentMode(String deploymentMode) {
        return deploymentMode.equals(MangleDeploymentMode.CLUSTER.name()) ? MangleDeploymentMode.CLUSTER
                : MangleDeploymentMode.STANDALONE;
    }
}
