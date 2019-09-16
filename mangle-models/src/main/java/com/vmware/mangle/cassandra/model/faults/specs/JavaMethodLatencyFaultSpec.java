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
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.vmware.mangle.services.enums.BytemanFaultType;

/**
 * @author bkaranam
 *
 *
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class JavaMethodLatencyFaultSpec extends JVMCodeLevelFaultSpec {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Integer value. You can provide integer value in terms of milli seconds", required = true, example = "100")
    private Integer latency;

    public JavaMethodLatencyFaultSpec() {
        setFaultType(BytemanFaultType.JAVA_METHOD_LATENCY.toString());
        setFaultName(BytemanFaultType.JAVA_METHOD_LATENCY.toString());
        setSpecType(this.getClass().getName());
    }

    @JsonIgnore
    @Override
    public void setTimeoutInMilliseconds(Integer timeoutinMilliseconds) {
        super.setTimeoutInMilliseconds(timeoutinMilliseconds);
    }
}
