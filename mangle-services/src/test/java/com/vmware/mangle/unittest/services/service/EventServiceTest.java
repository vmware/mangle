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

package com.vmware.mangle.unittest.services.service;


import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.services.EventService;
import com.vmware.mangle.services.cassandra.model.events.basic.Event;
import com.vmware.mangle.services.repository.EventRepository;

/**
 * Integration tests for the {@link EventService}.
 *
 * @author hkilari
 * @since 1.0
 */
@Log4j2
public class EventServiceTest {

    @InjectMocks
    EventService eventService;

    @Mock
    EventRepository eventRepository;

    @BeforeClass
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for {@link EventService#save(Event)}.
     *
     * @throws Exception
     */
    @Test
    public void saveTest() throws Exception {
        log.info("Executing test: saveTest on EventService#save");
        when(eventRepository.save(any())).then(new ReturnsArgumentAt(0));
        Event persistedEvent = eventService.save(new Event("new event", "integration test"));
        verify(eventRepository, times(1)).save(any());

        when(eventRepository.findById(persistedEvent.getId())).thenReturn(Optional.of(persistedEvent));
        Event retrievedEvent = eventService.findById(persistedEvent.getId());
        verify(eventRepository, times(1)).findById(persistedEvent.getId());
        Assert.assertNotNull(retrievedEvent);

        when(eventRepository.findAll()).thenReturn(Arrays.asList(persistedEvent));
        Assert.assertNotNull(
                eventService.findAll().stream().filter(e -> e.getName().equals("new event")).findFirst().get());
        verify(eventRepository, times(1)).findAll();
        Assert.assertEquals(eventService.findAll().size(), 1);

        Mockito.doNothing().when(eventRepository).delete(any());
        eventService.deleteById(persistedEvent.getId());
        verify(eventRepository, times(1)).deleteById(any());
    }

}
