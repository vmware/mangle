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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import com.vmware.mangle.services.commons.ServiceCommonUtils;
import com.vmware.mangle.services.events.hazelcast.HazelcastSyncEvent;

/**
 * @author chetanc
 *
 *
 */
@Component
@Log4j2
public class HazelcastMultinodeSyncListener implements HazelcastInstanceAware, MessageListener<HazelcastSyncEvent> {

    private HazelcastInstance hazelcastInstance;
    private static final Executor messageExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void onMessage(Message<HazelcastSyncEvent> message) {
        log.debug("Received hazelcast sync event with message: {}", message.toString());
        HazelcastSyncEvent syncEvent = message.getMessageObject();
        HazelcastClusterSyncAware beanObject;
        if (syncEvent != null) {
            log.debug("Processing hazelcast sync event on the class {}", syncEvent.getSyncClass());
            if (!hazelcastInstance.getCluster().getLocalMember().getAddress()
                    .equals(message.getPublishingMember().getAddress())) {
                beanObject = (HazelcastClusterSyncAware) ServiceCommonUtils.getBean(syncEvent.getSyncClass());
                messageExecutor.execute(() -> beanObject.resync(syncEvent.getSyncOnObjectIndentifier()));
            }
        } else {
            log.info("Invalid hazelcast sync event");
        }

    }
}
