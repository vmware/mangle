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

package com.vmware.mangle.unittest.services.cassandra.converters;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datastax.driver.core.Row;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.model.enums.EventType;
import com.vmware.mangle.services.cassandra.converters.EventReadingConverter;
import com.vmware.mangle.services.cassandra.model.events.basic.EntityCreatedEvent;
import com.vmware.mangle.services.cassandra.model.events.basic.EntityDeletedEvent;
import com.vmware.mangle.services.cassandra.model.events.basic.EntityUpdatedEvent;
import com.vmware.mangle.services.cassandra.model.events.basic.Event;
import com.vmware.mangle.services.mockdata.EventMockData;

/**
 * Unit Test Case for EventReadingConverter
 *
 * @author ranjans
 */
public class EventReadingConverterTest extends PowerMockTestCase {

    private EventReadingConverter eventReadingConverter;
    @Mock
    private MappingCassandraConverter mappingCassandraConverter;
    private EventMockData eventMockData;

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        this.eventMockData = new EventMockData();
    }

    @BeforeMethod
    public void tearUp() {
        this.eventReadingConverter = spy(new EventReadingConverter(mappingCassandraConverter));
    }

    @Test
    public void testConvertEntityCreatedEvent() {
        Row source = mock(Row.class);
        when(source.getString(anyString())).thenReturn(EventType.ENTITY_CREATED_EVENT.getName());
        when(mappingCassandraConverter.read(any(), any(Row.class))).thenReturn(eventMockData.getEntityCreatedEvent());
        Event actualResult = eventReadingConverter.convert(source);
        Assert.assertTrue(actualResult instanceof EntityCreatedEvent);
        verify(source, times(1)).getString(anyString());
        verify(mappingCassandraConverter, times(1)).read(any(), any(Row.class));
    }

    @Test
    public void testConvertEntityUpdatedEvent() {
        Row source = mock(Row.class);
        when(source.getString(anyString())).thenReturn(EventType.ENTITY_UPDATED_EVENT.getName());
        when(mappingCassandraConverter.read(any(), any(Row.class))).thenReturn(eventMockData.getEntityUpdatedEvent());
        Event actualResult = eventReadingConverter.convert(source);
        Assert.assertTrue(actualResult instanceof EntityUpdatedEvent);
        verify(source, times(1)).getString(anyString());
        verify(mappingCassandraConverter, times(1)).read(any(), any(Row.class));
    }

    @Test
    public void testConvertEntityDeletedEvent() {
        Row source = mock(Row.class);
        when(source.getString(anyString())).thenReturn(EventType.ENTITY_DELETED_EVENT.getName());
        when(mappingCassandraConverter.read(any(), any(Row.class))).thenReturn(eventMockData.getEntityDeletedEvent());
        Event actualResult = eventReadingConverter.convert(source);
        Assert.assertTrue(actualResult instanceof EntityDeletedEvent);
        verify(source, times(1)).getString(anyString());
        verify(mappingCassandraConverter, times(1)).read(any(), any(Row.class));
    }

}
