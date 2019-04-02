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

import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

import com.vmware.mangle.services.enums.AgentFaultName;

/**
 * @author bkaranam
 *
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class CpuFaultSpec extends JVMAgentFaultSpec {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "String value between 1 to 100 representing memory usage %", example = "80")
    @NotEmpty
    @Range(min = 1, max = 100)
    private String cpuLoad;

    public CpuFaultSpec() {
        setFaultName(AgentFaultName.INJECT_CPU_FAULT.getValue());
        setSpecType(this.getClass().getName());
    }

    @NotEmpty
    @Override
    public String getTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }
}
