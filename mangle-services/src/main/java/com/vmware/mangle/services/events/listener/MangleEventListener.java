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

package com.vmware.mangle.services.events.listener;


import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.vmware.mangle.services.cassandra.model.events.basic.EntityCreatedEvent;
import com.vmware.mangle.services.cassandra.model.events.basic.EntityDeletedEvent;
import com.vmware.mangle.services.cassandra.model.events.basic.EntityUpdatedEvent;
import com.vmware.mangle.services.cassandra.model.events.basic.Event;

/**
 * @author hkilari
 */
@Log4j2
public class MangleEventListener {

    @Component
    static class EntityCreatedEventListener {

        @TransactionalEventListener
        public void handle(EntityCreatedEvent entityCreatedEvent) {
            log.info("handle entity created event '{}'", entityCreatedEvent);
        }

        @TransactionalEventListener
        public void handle(EntityUpdatedEvent entityUpdatedEvent) {
            log.info("handle entity Updated event '{}'", entityUpdatedEvent);
        }

        @TransactionalEventListener
        public void handle(EntityDeletedEvent entityDeletedEvent) {
            log.info("handle entity Deleted event '{}'", entityDeletedEvent);
        }
    }

    @Component
    static class AllEventsListener {
        @EventListener
        public void handle(Event event) {
            log.info(event.getMessage());
        }
    }
}
