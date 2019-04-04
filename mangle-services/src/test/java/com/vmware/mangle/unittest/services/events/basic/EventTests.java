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

import static org.springframework.test.util.ReflectionTestUtils.getField;

import static com.vmware.mangle.unittest.services.events.basic.EventTestUtils.createEventwithName;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.services.cassandra.model.events.basic.Event;


/**
 * Unit tests for the {@link Event} entity, basically just verifying that Lombok is working.
 *
 * @author hkilari
 * @since 1.0
 */
public class EventTests {

    @Test
    public void lombokShouldSetCorrectly() {
        Event event = new Event("TestEvent", "Test Event Message");
        event.setMessage("Test Event Message Modified");

        Assert.assertEquals(getField(event, "name"), "TestEvent");
        Assert.assertEquals(getField(event, "message"), "Test Event Message Modified");
    }

    @Test
    public void lombokShouldGetCorrectly() {
        Event event = new Event("TestEvent", "Test Event Message");
        Assert.assertEquals(event.getName(), "TestEvent");
        Assert.assertEquals(event.getMessage(), "Test Event Message");
    }

    @Test
    public void eventsShouldBeEqualWithDifferentIdsButTheSameName() {
        Event event1 = createEventwithName("TestEvent1");
        Event event2 = createEventwithName("TestEvent1");

        event1.setId("1");
        event2.setId("2");

        Assert.assertEquals(event1, event1);
        Assert.assertEquals(event2, event2);
        Assert.assertEquals(event1, event2);
    }

    @Test
    public void eventsShouldNotBeEqualWithTheSameIdButDifferentNames() {
        Event event3 = createEventwithName("event3");
        Event event4 = createEventwithName("event4");

        Assert.assertEquals(event3, event3);
        Assert.assertEquals(event4, event4);
        Assert.assertNotEquals(event3, event4);
    }

    @Test
    public void eventsShouldHaveSameHashCodeWithDifferentIdsButTheSameName() {
        Event event1 = createEventwithName("1L");
        Event event2 = createEventwithName("2L");

        Assert.assertEquals(event1.hashCode(), event1.hashCode());
        Assert.assertEquals(event2.hashCode(), event2.hashCode());
    }

    @Test
    public void eventsShouldNotHaveSameHashCodeWithTheSameIdButDifferentNames() {
        Event event3 = createEventwithName("event 3");
        Event event4 = createEventwithName("event 4");

        Assert.assertEquals(event3.hashCode(), event3.hashCode());
        Assert.assertEquals(event4.hashCode(), event4.hashCode());
        Assert.assertNotEquals(event3.hashCode(), event4.hashCode());
    }

    @Test
    public void testToStringOfEvent() {
        Event event3 = createEventwithName("event 3");
        Assert.assertEquals(event3.toString(), "Event(id=" + event3.getId() + ", eventDate=" + event3.getEventDate()
                + ", name=event 3, message=Test Event Message)");
    }
}
