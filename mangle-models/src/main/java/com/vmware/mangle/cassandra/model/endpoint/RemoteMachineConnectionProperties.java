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

import com.datastax.driver.core.DataType.Name;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import com.vmware.mangle.model.enums.OSType;

/**
 * Remote Machine Connection Properties model class
 *
 * @author kumargautam
 */
@UserDefinedType("remoteMachineConnectionProperties")
@ApiModel(description = "RemoteMachine connection properties should be specified if endpoint type is MACHINE")
@Data
public class RemoteMachineConnectionProperties implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "Target machine's Hostname or IP", example = "10.123.45.68 or mangle.vmware.com")
    @JsonProperty(required = true)
    private String host;
    @ApiModelProperty(value = "SSH port of the target machine", dataType = "java.lang.Integer", example = "22")
    @JsonProperty(required = false, defaultValue = "22")
    private Integer sshPort = 22;
    @ApiModelProperty(value = "SSH connection timeout", dataType = "java.lang.Integer", example = "1000")
    @JsonProperty(required = false, defaultValue = "60000")
    private Integer timeout = 60000;
    @ApiModelProperty(value = "Please select OSType from existing enums", example = "LINUX")
    @JsonProperty(required = true)
    @CassandraType(type = Name.VARCHAR)
    private OSType osType;
}
