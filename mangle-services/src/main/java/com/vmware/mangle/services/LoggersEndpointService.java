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

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.logging.LogLevel;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.vmware.mangle.services.hazelcast.HazelcastClusterSyncAware;

/**
 * Custom api for Loggers Endpoint.
 *
 * @author kumargautam
 */
@Service
@EndpointWebExtension(endpoint = LoggersEndpoint.class)
@Log4j2
public class LoggersEndpointService implements HazelcastClusterSyncAware {

    private LoggersEndpoint loggersEndpoint;

    /**
     * Create a new {@link LoggersEndpointService} instance.
     *
     * @param loggingSystem
     *            the logging system to expose
     */
    @Autowired
    public LoggersEndpointService(LoggersEndpoint loggersEndpoint) {
        Assert.notNull(loggersEndpoint, "LoggersEndpoint must not be null");
        this.loggersEndpoint = loggersEndpoint;
    }

    @WriteOperation
    public void configureLogLevel(@Selector String name, @NonNull LogLevel configuredLevel) {
        log.debug("Received request to update logger by name...");
        loggersEndpoint.configureLogLevel(name, configuredLevel);
        String objectIdentifier = name + "#" + configuredLevel.name();
        triggerMultiNodeResync(objectIdentifier);
    }

    @Override
    public void resync(String objectIdentifier) {
        log.debug("Received request to resync loggers...");
        String[] input = objectIdentifier.split("#");
        if (input.length == 2) {
            String name = input[0];
            LogLevel configuredLevel = LogLevel.valueOf(input[1]);
            loggersEndpoint.configureLogLevel(name, configuredLevel);
        }
    }
}