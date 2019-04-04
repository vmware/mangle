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

package com.vmware.mangle.services.controller;

import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.model.MangleResponseResource;
import com.vmware.mangle.services.EventService;
import com.vmware.mangle.services.cassandra.model.events.basic.Event;

/**
 * @author hkilari
 *
 */
@RestController
@RequestMapping("rest/api/v1/events")
@Log4j2
public class EventsController {

    @Autowired
    EventService service;


    @GetMapping
    public ResponseEntity<MangleResponseResource<List<Event>>> retrieveAllEvents() {
        log.debug("Retrieving all events");
        return new ResponseEntity<>(new MangleResponseResource<List<Event>>(service.findAll()), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MangleResponseResource<Event>> retrieveEvent(@PathVariable String id) {
        log.debug("Finding event with ID: " + id);
        return new ResponseEntity<>(new MangleResponseResource<Event>(service.findById(id)), HttpStatus.OK);
    }
}
