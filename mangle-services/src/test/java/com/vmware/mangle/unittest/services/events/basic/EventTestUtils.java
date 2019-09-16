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

package com.vmware.mangle.unittest.services.events.basic;

import com.vmware.mangle.services.cassandra.model.events.basic.Event;

/**
 * Collection of test utilities for working with the {@link Event} entity.
 *
 * @author hkilari
 * @since 1.0
 */
class EventTestUtils {
    private EventTestUtils() {

    }

    static Event createEventwithName(String name) {
        return createEvent(name, "Test Event Message");
    }

    static Event createEvent(String name, String messsage) {
        return new Event(name, messsage);
    }

}
