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

package com.vmware.mangle.services.mockdata;

import java.util.Date;
import java.util.Properties;

import com.vmware.mangle.model.enums.EventType;
import com.vmware.mangle.services.cassandra.model.events.basic.EntityCreatedEvent;
import com.vmware.mangle.services.cassandra.model.events.basic.EntityDeletedEvent;
import com.vmware.mangle.services.cassandra.model.events.basic.EntityUpdatedEvent;
import com.vmware.mangle.services.cassandra.model.events.basic.Event;
import com.vmware.mangle.utils.ReadProperty;
import com.vmware.mangle.utils.constants.Constants;

/**
 * Mock data for Event
 *
 * @author ranjans
 */
public class EventMockData {

    private Properties properties;

    public EventMockData() {
        this.properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
    }

    public Event getEntityCreatedEvent() {
        EntityCreatedEvent entityCreatedEvent = new EntityCreatedEvent();
        entityCreatedEvent.setId(properties.getProperty("event.id"));
        entityCreatedEvent.setName(EventType.ENTITY_CREATED_EVENT.toString());
        entityCreatedEvent.setEntityId(properties.getProperty("event.entity.id"));
        entityCreatedEvent.setEntityClass(properties.getProperty("event.entity.class"));
        entityCreatedEvent.setEventDate(new Date());
        entityCreatedEvent.setMessage(properties.getProperty("event.message"));
        return entityCreatedEvent;
    }

    public Event getEntityUpdatedEvent() {
        EntityUpdatedEvent entityUpdatedEvent = new EntityUpdatedEvent();
        entityUpdatedEvent.setId(properties.getProperty("event.id"));
        entityUpdatedEvent.setName(EventType.ENTITY_UPDATED_EVENT.toString());
        entityUpdatedEvent.setEntityId(properties.getProperty("event.entity.id"));
        entityUpdatedEvent.setEntityClass(properties.getProperty("event.entity.class"));
        entityUpdatedEvent.setEventDate(new Date());
        entityUpdatedEvent.setMessage(properties.getProperty("event.message"));
        return entityUpdatedEvent;
    }

    public Event getEntityDeletedEvent() {
        EntityDeletedEvent entityDeletedEvent = new EntityDeletedEvent();
        entityDeletedEvent.setId(properties.getProperty("event.id"));
        entityDeletedEvent.setName(EventType.ENTITY_DELETED_EVENT.toString());
        entityDeletedEvent.setEntityId(properties.getProperty("event.entity.id"));
        entityDeletedEvent.setEntityClass(properties.getProperty("event.entity.class"));
        entityDeletedEvent.setEventDate(new Date());
        entityDeletedEvent.setMessage(properties.getProperty("event.message"));
        return entityDeletedEvent;
    }

}
