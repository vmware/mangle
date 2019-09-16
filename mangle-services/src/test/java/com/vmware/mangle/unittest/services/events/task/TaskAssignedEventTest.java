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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.services.cassandra.model.events.basic.Event;
import com.vmware.mangle.services.events.task.TaskAssignedEvent;
import com.vmware.mangle.services.tasks.helper.MockTaskHelper;


/**
 * Unit tests for the {@link Event} entity, basically just verifying that Lombok is working.
 *
 * @author hkilari
 * @since 1.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TaskAssignedEventTest {
    private Task<CommandExecutionFaultSpec> task;
    private String eventMesage;

    @BeforeMethod
    public void init() {
        MockTaskHelper<CommandExecutionFaultSpec> mockTask = new MockTaskHelper();
        task = mockTask.init(new CommandExecutionFaultSpec());
        task.setId("12345");
        eventMesage = "Assigned Task: " + task.getClass().getName() + " With Id: " + task.getId();
    }


    @Test
    public void lombokShouldSetCorrectly() {
        TaskAssignedEvent<?> event = new TaskAssignedEvent(task);

        Assert.assertEquals(getField(event, "name"), "TaskAssignedEvent");
        Assert.assertEquals(getField(event, "message"), eventMesage);
        Assert.assertEquals(getField(event, "task"), task);
    }

    @Test
    public void lombokShouldGetCorrectly() {
        TaskAssignedEvent<?> event = new TaskAssignedEvent(task);

        Assert.assertEquals(event.getName(), "TaskAssignedEvent");
        Assert.assertEquals(event.getMessage(), eventMesage);
        Assert.assertEquals(event.getTask(), task);
    }

    @Test
    public void eventsShouldBeEqualWithDifferentIdsButTheSameName() {
        TaskAssignedEvent<?> event1 = new TaskAssignedEvent(task);
        TaskAssignedEvent<?> event2 = new TaskAssignedEvent(task);

        event1.setId("1");
        event2.setId("2");

        Assert.assertEquals(event1, event1);
        Assert.assertEquals(event2, event2);
        Assert.assertEquals(event1, event2);
    }

    @Test
    public void eventsShouldNotBeEqualWithTheSameIdButDifferentNames() {
        TaskAssignedEvent<?> event3 = new TaskAssignedEvent(task);
        TaskAssignedEvent<?> event4 = new TaskAssignedEvent(task);
        event4.setName("TaskAssignedEventModified");

        Assert.assertEquals(event3, event3);
        Assert.assertEquals(event4, event4);
        Assert.assertNotEquals(event3, event4);
    }

    @Test
    public void eventsShouldHaveSameHashCodeWithDifferentIdsButTheSameName() {
        TaskAssignedEvent<?> event1 = new TaskAssignedEvent(task);
        TaskAssignedEvent<?> event2 = new TaskAssignedEvent(task);
        event1.setId("1");
        event2.setId("2");
        Assert.assertEquals(event1.hashCode(), event1.hashCode());
        Assert.assertEquals(event2.hashCode(), event2.hashCode());
    }

    @Test
    public void eventsShouldNotHaveSameHashCodeWithTheSameIdButDifferentNames() {
        TaskAssignedEvent<?> event3 = new TaskAssignedEvent(task);
        TaskAssignedEvent<?> event4 = new TaskAssignedEvent(task);
        event4.setName("TaskAssignedEventModified");

        Assert.assertEquals(event3.hashCode(), event3.hashCode());
        Assert.assertEquals(event4.hashCode(), event4.hashCode());
        Assert.assertNotEquals(event3.hashCode(), event4.hashCode());
    }

    @Test
    public void testTaskAssignedEventResolvableType() {
        TaskAssignedEvent<?> event = new TaskAssignedEvent(task);
        event.getResolvableType();
        Assert.assertEquals(event.getResolvableType().toString(),
                TaskAssignedEvent.class.getName() + "<" + Event.class.getName() + ">");
    }
}
