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

package com.vmware.mangle.services.cassandra.converters;

import com.datastax.driver.core.Row;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.convert.ReadingConverter;

import com.vmware.mangle.model.enums.EventType;
import com.vmware.mangle.services.cassandra.model.events.basic.EntityCreatedEvent;
import com.vmware.mangle.services.cassandra.model.events.basic.EntityDeletedEvent;
import com.vmware.mangle.services.cassandra.model.events.basic.EntityUpdatedEvent;
import com.vmware.mangle.services.cassandra.model.events.basic.Event;

/**
 * Event reading converter.
 *
 * @author ranjans
 */
@ReadingConverter
@Log4j2
public class EventReadingConverter implements Converter<Row, Event> {

    private CassandraConverter cassandraConverter;

    public EventReadingConverter() {
        this.cassandraConverter = new MappingCassandraConverter();
    }

    public EventReadingConverter(MappingCassandraConverter mappingCassandraConverter) {
        this.cassandraConverter = mappingCassandraConverter;
    }

    @Override
    public Event convert(Row source) {
        log.debug("Start execution of event convert() method...");
        String eventType = source.getString("name");
        if (eventType.equals(EventType.ENTITY_CREATED_EVENT.getName())) {
            return (EntityCreatedEvent) cassandraConverter.read(EntityCreatedEvent.class, source);
        }
        if (eventType.equals(EventType.ENTITY_UPDATED_EVENT.getName())) {
            return (EntityUpdatedEvent) cassandraConverter.read(EntityUpdatedEvent.class, source);
        }
        if (eventType.equals(EventType.ENTITY_DELETED_EVENT.getName())) {
            return (EntityDeletedEvent) cassandraConverter.read(EntityDeletedEvent.class, source);
        } else {
            return (Event) cassandraConverter.read(Event.class, source);
        }
    }

}
