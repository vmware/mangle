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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.vmware.mangle.services.enums.AgentFaultName;

/**
 * @author bkaranam
 *
 *
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class CpuFaultSpec extends JVMAgentFaultSpec {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "Integer value between 1 to 100 representing cpu usage %", example = "80")
    @NotNull
    @Min(1)
    @Max(100)
    private Integer cpuLoad;

    public CpuFaultSpec() {
        setFaultName(AgentFaultName.INJECT_CPU_FAULT.getValue());
        setSpecType(this.getClass().getName());
    }

    @NotNull
    @Override
    public Integer getTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }
}
