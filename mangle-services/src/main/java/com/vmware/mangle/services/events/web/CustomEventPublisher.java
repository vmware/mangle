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

package com.vmware.mangle.services.events.web;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.vmware.mangle.services.EventService;
import com.vmware.mangle.services.cassandra.model.events.basic.Event;

/**
 * @author hkilari
 *
 */
@Component
@Log4j2
public class CustomEventPublisher {
    private ApplicationEventPublisher applicationEventPublisher;
    private EventService eventService;

    @Autowired
    public CustomEventPublisher(ApplicationEventPublisher applicationEventPublisher, EventService eventService) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.eventService = eventService;
    }

    public void publishAnEvent(Event event) {
        log.trace("Publishing custom event. ");
        applicationEventPublisher.publishEvent(eventService.save(event));
    }
}
