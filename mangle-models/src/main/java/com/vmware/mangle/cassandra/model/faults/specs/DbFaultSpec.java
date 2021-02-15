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

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.vmware.mangle.services.enums.AgentFaultName;

/**
 * Specification for DbFault.
 *
 * @author kumargautam
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings({ "squid:MaximumInheritanceDepth" })
public class DbFaultSpec extends CommandExecutionFaultSpec {

    private static final long serialVersionUID = 1L;

    public DbFaultSpec() {
        setFaultName(AgentFaultName.INJECT_DB_CONNECTION_LEAK_FAULT.getValue());
        setSpecType(this.getClass().getName());
    }

    @NotNull
    @Override
    public Integer getTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }

    @Override
    public String toString() {
        return "DbFaultSpec ("
                + (timeoutInMilliseconds != null ? "timeoutInMilliseconds=" + getTimeoutInMilliseconds() + ", " : "")
                + (getInjectionHomeDir() != null ? "injectionHomeDir=" + getInjectionHomeDir() + ", " : "") + ")";
    }
}
