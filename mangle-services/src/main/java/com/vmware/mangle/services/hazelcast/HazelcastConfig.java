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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
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
import com.vmware.mangle.services.ClusterConfigService;
import com.vmware.mangle.services.application.listener.CustomApplicationListener;
import com.vmware.mangle.utils.HazelcastUtils;
import com.vmware.mangle.utils.constants.ErrorConstants;
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

    @Value("${hazelcast.config.public}")
    private String hazelcastPublicAddress;

    @Value("${hazelcast.config.members}")
    private String hazelcastMembers;

    @Value("${hazelcast.config.validationToken}")
    private String hazelcastValidationToken;

    @Value("${hazelcast.config.cluster.name}")
    private String clusterName;

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

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(getClusterConfig());

        migrationListener.setHazelcastInstance(hz);
        membershipListener.setHazelcastInstance(hz);
        hazelcastTaskService.setHazelcastInstance(hz);
        mapService.setHazelcastInstance(hz);
        applicationListener.setHazelcastInstance(hz);
        clusterConfigService.setHazelcastInstance(hz);

        hz.getPartitionService().addMigrationListener(migrationListener);
        hz.getCluster().addMembershipListener(membershipListener);
        return hz;
    }

    private Config getClusterConfig() throws MangleException {
        Config config = new Config();
        MemberAttributeConfig mangleMaintenanceModeConfig = new MemberAttributeConfig();
        mangleMaintenanceModeConfig.setBooleanAttribute("MANGLE_IN_MAINTENANCE_MODE", false);
        String validationToken = HazelcastUtils.getApplicationValidationToken(hazelcastValidationToken);
        config.setProperty(GroupProperty.APPLICATION_VALIDATION_TOKEN.getName(), validationToken);
        config.setMemberAttributeConfig(mangleMaintenanceModeConfig);
        config.addListenerConfig(new ListenerConfig(membershipListener));
        HazelcastClusterConfig clusterConfig = clusterConfigService.getClusterConfiguration();
        List<String> members = HazelcastUtils.getHazelcastMemberslist(hazelcastMembers);

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
            clusterConfigService.addClusterConfiguration(clusterConfig);
            log.info("Bringing up the cluster with the validation token: {}", validationToken);
        } else {
            if (StringUtils.isEmpty(clusterConfig.getValidationToken())
                    || !(clusterConfig.getValidationToken().equals(validationToken)
                            && clusterConfig.getClusterName().equals(clusterName))) {
                log.error(
                        "Mangle failed to start up, mis-matching validation token with the one configured in db, provided: {}",
                        validationToken);
                throw new MangleException(String.format(ErrorConstants.CLUSTER_CONFIG_MISMATCH, validationToken),
                        ErrorCode.CLUSTER_CONFIG_MISMATCH, validationToken);
            }

            /**
             * if there already exists a cluster configuration in the persistence layer of the
             * application, it maintains the list of active members in the cluster
             * This list of active members will be added to current hazelcast cluster member
             * initialization configuration, so that it can try to reach any of the active member to
             * join that cluster
             */
            if (!CollectionUtils.isEmpty(clusterConfig.getMembers())) {
                members.addAll(clusterConfig.getMembers());
            }
        }

        /**
         * Public address with which the mangle instance can be accessed by another cluster
         * members.
         *
         * https://docs.hazelcast.org/docs/latest/manual/html-single/#public-address
         */
        if (!StringUtils.isEmpty(hazelcastPublicAddress)) {
            config.getNetworkConfig().setPublicAddress(hazelcastPublicAddress);
        }

        /* Setting up tcp/ip discovery for the hazelcast cluster */
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setMembers(members).setEnabled(true);

        /*
         * Setting up hazelcast cluster with a name
         * provided either by the user, or default "mangle"
         */
        config.getGroupConfig().setName(clusterName);
        return config;
    }
}
