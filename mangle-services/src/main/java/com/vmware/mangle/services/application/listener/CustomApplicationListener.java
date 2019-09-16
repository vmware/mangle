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

package com.vmware.mangle.services.application.listener;

import static com.vmware.mangle.utils.constants.HazelcastConstants.HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.ReplicatedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.vmware.mangle.services.commons.ServiceCommonUtils;
import com.vmware.mangle.services.config.MangleBootInitializer;
import com.vmware.mangle.utils.constants.HazelcastConstants;
import com.vmware.mangle.utils.constants.MetricProviderConstants;
import com.vmware.mangle.utils.constants.URLConstants;

/**
 * @author ashrimali
 *
 */
@Component
public class CustomApplicationListener implements ApplicationListener<ContextRefreshedEvent>, HazelcastInstanceAware {

    private HazelcastInstance hazelcastInstance;

    @Autowired
    private ApplicationContext applicationContext;

    private MangleBootInitializer bootInitializer;

    @Autowired
    public CustomApplicationListener(MangleBootInitializer bootInitializer) {
        this.bootInitializer = bootInitializer;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        addNodeStatusToCluster();
        bootInitializer.updateClusterConfigObject();
        bootInitializer.initializeApplicationTasks();
        if (hazelcastInstance != null) {
            hazelcastInstance.getCluster().getLocalMember().setStringAttribute(
                    HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE, URLConstants.getMangleNodeCurrentStatus().name());
        }
        ServiceCommonUtils.setApplicationContext(applicationContext);
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        bootInitializer.setHazelcastInstance(hazelcastInstance);
    }

    /**
     * Adds the application status of the current node to the cluster level replicated map Which is
     * used in the calculation of the status of the quorum
     */
    private void addNodeStatusToCluster() {
        ReplicatedMap<String, Boolean> mangleApplicationStatusMap =
                hazelcastInstance.getReplicatedMap(HazelcastConstants.MANGLE_APPLICATION_STATUS_MAP);
        mangleApplicationStatusMap.put(System.getProperty(MetricProviderConstants.NODE_ADDRESS), true);
    }

}
