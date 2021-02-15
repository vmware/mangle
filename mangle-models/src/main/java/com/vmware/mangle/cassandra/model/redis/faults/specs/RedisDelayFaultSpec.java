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

package com.vmware.mangle.cassandra.model.redis.faults.specs;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.vmware.mangle.services.enums.RedisFaultName;

/**
 * RedisDelayFaultSpec model.
 *
 * @author kumargautam
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class RedisDelayFaultSpec extends RedisFaultSpec {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "Adds a delay, the value unit is milliseconds.", example = "1000")
    @NotNull
    @Min(1)
    @Max(2147483647)
    private Integer delay;

    @SuppressWarnings("squid:S2637")
    public RedisDelayFaultSpec() {
        setFaultName(RedisFaultName.REDISDBDELAYFAULT.getValue());
        setSpecType(this.getClass().getName());
    }
}
