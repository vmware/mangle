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


import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.cassandra.model.slack.Notifier;
import com.vmware.mangle.services.NotifierService;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * NotifierController class.
 *
 * @author kumargautam
 */
@RestController
@RequestMapping(value = "/rest/api/v1/notifier")
@Validated
@Log4j2
public class NotifierController {

    @Autowired
    private NotifierService notifierService;

    @ApiOperation(value = "API to get notifier", nickname = "getNotifier")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Notifier>> getNotifier(@RequestParam(name = "name", required = false) String name) {
        log.debug("Received request to retrieve details of notifiers...");
        List<Notifier> list = new ArrayList<>();
        if (StringUtils.hasLength(name)) {
            Notifier notifier = notifierService.getByName(name);
            if (notifier != null) {
                list.add(notifier);
            }
        } else {
            list = notifierService.getAllNotificationInfo();
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @ApiOperation(value = "API to add the notifier", nickname = "createNotifier")
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Notifier> createNotifier(@Validated @RequestBody Notifier notifier)
            throws MangleException {
        log.debug("Received request to create notifier ...");
        notifierService.testConnection(notifier);
        return new ResponseEntity<>(notifierService.create(notifier), HttpStatus.OK);
    }

    @ApiOperation(value = "API to update the notifier", nickname = "updateNotifier")
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Notifier> updateNotifier(@Validated @RequestBody Notifier notifier)
            throws MangleException {
        log.debug("Received request to update notifier...");
        notifierService.testConnection(notifier);
        return new ResponseEntity<>(notifierService.update(notifier), HttpStatus.OK);
    }

    @ApiOperation(value = "API to delete the notifier by names", nickname = "deleteNotifierByNames")
    @DeleteMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteNotifierByNames(@RequestParam @NotEmpty List<String> names)
            throws MangleException {
        log.debug("Received request to delete notifier by names...");
        notifierService.deleteByNames(names);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "API to test the notifier connection", nickname = "testConnection", hidden = true)
    @PostMapping(value = "/testConnection", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Notifier> testConnection(@Validated @RequestBody Notifier notification)
            throws MangleException {
        log.debug("Received request to test connection for notifier...");
        notifierService.testConnection(notification);
        return new ResponseEntity<>(notification, HttpStatus.OK);
    }

    @ApiOperation(value = "API to test the notifier connection", nickname = "testConnection")
    @PostMapping(value = "/testConnection/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Notifier> testConnection(@PathVariable("name") @NotBlank String name) throws MangleException {
        log.debug("Received request to test connection for notifier...");
        Notifier notification = notifierService.getByName(name);
        if (notification == null) {
            throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.SLACK_NAME, name);
        }
        notifierService.testConnection(notification);
        return new ResponseEntity<>(notification, HttpStatus.OK);
    }

    @ApiOperation(value = "API to enable or disable the notifier", nickname = "enableNotifier")
    @PutMapping(value = "/enable", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> enableNotifier(
            @RequestParam(value = "names", required = true) @NotEmpty List<String> names,
            @RequestParam(value = "enable", required = true) @NotNull Boolean enable) {
        log.debug("Received request to enable or disable the notifier...");
        return new ResponseEntity<>(notifierService.enableSlacks(names, enable), HttpStatus.OK);
    }
}