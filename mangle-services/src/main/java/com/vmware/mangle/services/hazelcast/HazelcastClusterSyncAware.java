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

import com.vmware.mangle.services.commons.ServiceCommonUtils;

/**
 * @author chetanc
 *
 *         The class which provides methods to update the application context with the latest
 *         changes related to any object should implement this interface.
 *
 *         After the data modification is committed to DB, triggerSyncEvent must be triggered with
 *         the identifier(generally the primary key) which will identify that object in the db table
 *
 *         resync method should do the operation that will trigger operations that will update the
 *         application context with the modification done on the other node.
 *
 */
public interface HazelcastClusterSyncAware {
    void resync(String objectIdentifier);

    default void triggerMultiNodeResync(String... objectIdentifier) {
        HazelcastSyncTopicManager topicManager =
                (HazelcastSyncTopicManager) ServiceCommonUtils.getBean(HazelcastSyncTopicManager.class);
        topicManager.triggerSyncEvent(this.getClass(), objectIdentifier);
    }

}
