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

package com.vmware.mangle.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.mangle.services.cassandra.model.events.basic.Event;
import com.vmware.mangle.services.events.service.EventNotFoundException;
import com.vmware.mangle.services.repository.EventRepository;
import com.vmware.mangle.utils.constants.ErrorConstants;

/**
 * Service API for the {@link Event} entity.
 *
 * @author hkilari
 * @since 1.0
 */
@Component
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public Event findById(String id) {
        Optional<Event> retrievedEvent = eventRepository.findById(id);
        if (!retrievedEvent.isPresent()) {
            throw new EventNotFoundException(String.format(ErrorConstants.EVENT_NOT_FOUND, id));
        }
        return retrievedEvent.get();
    }

    public Event save(Event event) {
        return eventRepository.save(event);
    }

    public void deleteById(String id) {
        eventRepository.deleteById(id);
    }

}
