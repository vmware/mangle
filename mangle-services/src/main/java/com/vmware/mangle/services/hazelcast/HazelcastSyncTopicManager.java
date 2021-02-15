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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.ITopic;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.mangle.services.events.hazelcast.HazelcastSyncEvent;
import com.vmware.mangle.utils.constants.HazelcastConstants;

/**
 * @author chetanc
 *
 *
 */
@Component
@Log4j2
public class HazelcastSyncTopicManager implements HazelcastInstanceAware {

    private ITopic<HazelcastSyncEvent> topic;

    @Autowired
    private HazelcastMultinodeSyncListener syncListener;

    /**
     *
     * @param syncClass
     *            is the class object of the service which has implemented HazelcastClusterSyncAware
     *            interface
     * @param objectIdentifiers
     *            list of object identifiers which uniquely identifies the object, it is generally a
     *            primary key of the mangle object
     */
    public void triggerSyncEvent(Class syncClass, String... objectIdentifiers) {
        for (String objectIdentifier : objectIdentifiers) {
            log.debug("Triggering sync across nodes for the class: {}", syncClass.getSimpleName());
            triggerSyncEvent(syncClass, objectIdentifier);
        }
    }

    /**
     *
     * @param clazz
     *            is the class object of the service which has implemented HazelcastClusterSyncAware
     *            interface
     * @param objectIdentifier
     *            object identifier which uniquely identifies the object, it is generally a primary
     *            key of the mangle object
     */
    private void triggerSyncEvent(Class clazz, String objectIdentifier) {
        topic.publish(new HazelcastSyncEvent(clazz, objectIdentifier));
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.topic = hazelcastInstance.getTopic(HazelcastConstants.HAZELCAST_MANGLE_SYNC_TOPIC_NAME);
        syncListener.setHazelcastInstance(hazelcastInstance);
        topic.addMessageListener(syncListener);
    }
}
