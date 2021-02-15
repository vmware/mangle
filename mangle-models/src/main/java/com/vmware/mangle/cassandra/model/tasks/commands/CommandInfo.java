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

package com.vmware.mangle.cassandra.model.tasks.commands;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.cassandra.core.mapping.UserDefinedType;


/**
 * @author bkaranam
 *
 *
 */
@UserDefinedType("CommandInfo")
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(onConstructor = @__({ @Deprecated }))
@NoArgsConstructor(onConstructor = @__({ @Deprecated }))
@Setter(onMethod = @__({ @Deprecated }))
@Builder(toBuilder = true)
@SuppressWarnings("squid:S1948")
public class CommandInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String command;
    private boolean ignoreExitValueCheck;
    private List<String> expectedCommandOutputList;
    private int noOfRetries;
    private int retryInterval;
    private int timeout;
    private List<CommandOutputProcessingInfo> commandOutputProcessingInfoList;
    private Map<String, String> knownFailureMap;

    public static CommandInfoBuilder builder(String command) {
        CommandInfoBuilder builder = new CommandInfoBuilder();
        return builder.command(command);
    }
}