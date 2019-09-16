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

import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.services.commons.ServiceCommonUtils;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.utils.constants.MetricProviderConstants;

/**
 * @author chetanc
 *
 *
 */
@Log4j2
public class HazelcastLifeCycleListener implements LifecycleListener {

    /**
     * Invoked every time a state of current hazelcast member is modified
     *
     * This method is used to sync the objects into context
     *
     * @param event
     */
    @SuppressWarnings("unchecked")
    @Override
    public void stateChanged(LifecycleEvent event) {
        log.info("Hazelcast state changed to {}", event.getState().name());
        HazelcastClusterSyncAware beanObject;
        if (event.getState() == LifecycleEvent.LifecycleState.MERGING) {
            for (Class<HazelcastClusterSyncAware> resyncOnClass : CommonConstants.mergeResyncClass) {
                log.info("Triggering resync on the class {} as node {} is merging to the cluster",
                        resyncOnClass.getSimpleName(), System.getProperty(MetricProviderConstants.NODE_ADDRESS));
                beanObject = (HazelcastClusterSyncAware) ServiceCommonUtils.getBean(resyncOnClass);
                beanObject.resync("");
            }
        }
    }
}
