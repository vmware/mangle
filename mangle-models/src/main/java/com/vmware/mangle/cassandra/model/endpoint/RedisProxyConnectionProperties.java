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

package com.vmware.mangle.cassandra.model.endpoint;

import java.io.Serializable;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

/**
 * Redis Proxy Connection Properties model class.
 *
 * @author kumargautam
 */
@UserDefinedType("redisProxyConnectionProperties")
@ApiModel(description = "RedisProxy connection properties should be specified if endpoint type is REDIS")
@Data
public class RedisProxyConnectionProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "RedFI Proxy's Hostname or IP", example = "10.134.211.35 or mangle.vmware.com")
    @JsonProperty(required = true)
    @NotEmpty
    private String host;

    @ApiModelProperty(value = "RedFI Controller port", dataType = "java.lang.Integer", example = "6380")
    @JsonProperty(required = false, defaultValue = "6380")
    @NotNull
    @Min(0)
    @Max(65535)
    private Integer port = 6380;
}
