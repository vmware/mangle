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

package com.vmware.mangle.services.events.hazelcast;

import lombok.Getter;

import com.vmware.mangle.services.cassandra.model.events.basic.Event;

/**
 * @author chetanc
 *
 *
 */
@Getter
public class HazelcastSyncEvent extends Event {
    private static final long serialVersionUID = 1L;
    private Class syncClass;
    private String syncOnObjectIndentifier;

    public HazelcastSyncEvent(Class syncClass, String syncOnObjectIndentifier) {
        super("HazelcastSyncEvent",
                "Sync on the the class: " + syncClass + " on the object identifier " + syncOnObjectIndentifier);
        this.syncClass = syncClass;
        this.syncOnObjectIndentifier = syncOnObjectIndentifier;
    }

}
