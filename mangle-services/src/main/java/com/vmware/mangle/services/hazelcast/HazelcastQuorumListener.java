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

import com.hazelcast.quorum.QuorumEvent;
import com.hazelcast.quorum.QuorumListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.mangle.services.config.MangleBootInitializer;
import com.vmware.mangle.services.enums.MangleQuorumStatus;
import com.vmware.mangle.services.scheduler.Scheduler;
import com.vmware.mangle.utils.HazelcastUtils;

/**
 * @author chetanc
 *
 *
 */
@Component
@Log4j2
public class HazelcastQuorumListener implements QuorumListener {

    private MangleBootInitializer mangleBootInitializer;
    private Scheduler scheduler;
    private HazelcastTaskCache taskCache;

    @Autowired
    public HazelcastQuorumListener(MangleBootInitializer mangleBootInitializer, Scheduler scheduler,
            HazelcastTaskCache taskCache) {
        this.mangleBootInitializer = mangleBootInitializer;
        this.scheduler = scheduler;
        this.taskCache = taskCache;
    }

    @Override
    public void onChange(QuorumEvent quorumEvent) {
        updateApplicationQuorumStatus(quorumEvent);
    }

    private void updateApplicationQuorumStatus(QuorumEvent quorumEvent) {
        if (quorumEvent.isPresent()) {
            log.info("Quorum has been created");
            HazelcastUtils.updateHazelcastQuorumStatus(MangleQuorumStatus.PRESENT);
            mangleBootInitializer.updateClusterConfigObject();
            mangleBootInitializer.initializeApplicationTasks();
        } else {
            log.info("Quorum is not present on the current node");
            HazelcastUtils.updateHazelcastQuorumStatus(MangleQuorumStatus.NOT_PRESENT);
            scheduler.removeAllSchedulesFromCurrentNode();
            mangleBootInitializer.removeCurrentClusterNodesFromClusterConfig();
            taskCache.cleanTaskMapForQuorumFailure();
        }
    }
}
