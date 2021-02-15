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

package com.vmware.mangle.unittest.services.events.task;

import static org.springframework.test.util.ReflectionTestUtils.getField;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.services.events.task.ResiliencyScoreTaskCreatedEvent;
import com.vmware.mangle.services.mockdata.ResiliencyScoreMockData;

/**
 * @author dbhat
 */

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ResiliencyScoreTaskCreatedEventTest {
    private ResiliencyScoreTask task;
    private String eventMesage;

    @BeforeClass
    public void init() {
        task = ResiliencyScoreMockData.getResiliencyScoreTask1();
        eventMesage = "Created Task: " + task.getClass().getName() + " With Id: " + task.getId();
    }

    @Test
    public void lombokShouldSetCorrectly() {
        ResiliencyScoreTaskCreatedEvent event = new ResiliencyScoreTaskCreatedEvent(task);

        Assert.assertEquals(getField(event, "name"), "ResiliencyScoreTaskCreatedEvent");
        Assert.assertEquals(getField(event, "message"), eventMesage);
        Assert.assertEquals(getField(event, "task"), task);
    }

    @Test
    public void lombokShouldGetCorrectly() {
        ResiliencyScoreTaskCreatedEvent event = new ResiliencyScoreTaskCreatedEvent(task);

        System.out.println("***** Event Name " + event.getName());
        Assert.assertEquals(event.getName(), "ResiliencyScoreTaskCreatedEvent");
        Assert.assertEquals(event.getMessage(), eventMesage);
        Assert.assertEquals(event.getTask(), task);
    }

    @Test
    public void eventsShouldNotBeEqualWithTheSameIdButDifferentNames() {
        ResiliencyScoreTaskCreatedEvent event3 = new ResiliencyScoreTaskCreatedEvent(task);
        ResiliencyScoreTaskCreatedEvent event4 = new ResiliencyScoreTaskCreatedEvent(task);
        event4.setName("ResiliencyScore Created Event has been modified");

        Assert.assertEquals(event3, event3);
        Assert.assertEquals(event4, event4);
        Assert.assertNotEquals(event3, event4);
    }

    @Test
    public void eventsShouldHaveSameHashCodeWithDifferentIdsButTheSameName() {
        ResiliencyScoreTaskCreatedEvent event1 = new ResiliencyScoreTaskCreatedEvent(task);
        ResiliencyScoreTaskCreatedEvent event2 = new ResiliencyScoreTaskCreatedEvent(task);
        event1.setId("1");
        event2.setId("2");
        Assert.assertEquals(event1.hashCode(), event1.hashCode());
        Assert.assertEquals(event2.hashCode(), event2.hashCode());
    }

    @Test
    public void eventsShouldNotHaveSameHashCodeWithTheSameIdButDifferentNames() {
        ResiliencyScoreTaskCreatedEvent event3 = new ResiliencyScoreTaskCreatedEvent(task);
        ResiliencyScoreTaskCreatedEvent event4 = new ResiliencyScoreTaskCreatedEvent(task);
        event4.setName("ResiliencyScore Created Event has been modified");

        Assert.assertEquals(event3.hashCode(), event3.hashCode());
        Assert.assertEquals(event4.hashCode(), event4.hashCode());
        Assert.assertNotEquals(event3.hashCode(), event4.hashCode());
    }
}
