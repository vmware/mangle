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

package com.vmware.mangle.cassandra.model.faults.specs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.vmware.mangle.services.enums.AgentFaultName;

/**
 *
 * @author kumargautam
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({ "timeoutinMilliseconds" })
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class DiskSpaceSpec extends CommandExecutionFaultSpec {

    private static final long serialVersionUID = 1L;
    private String directoryPath;

    public DiskSpaceSpec() {
        setFaultName(AgentFaultName.INJECT_DISK_SPACE_FAULT.getValue());
        setSpecType(this.getClass().getName());
    }

    @JsonIgnore
    @Override
    public void setTimeoutInMilliseconds(Integer timeoutinMilliseconds) {
        super.setTimeoutInMilliseconds(timeoutinMilliseconds);
    }
}
