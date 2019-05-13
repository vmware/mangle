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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

/**
 * Docker Connection Properties
 *
 * @author kumargautam
 */
@UserDefinedType("dockerConnectionProperties")
@ApiModel(description = "Docker connection properties should be specified if endpoint type is DOCKER")
@Data
public class DockerConnectionProperties implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Docker HostName.")
    @NotEmpty
    private String dockerHostname;

    @NotNull
    @ApiModelProperty(value = "Docker Port.If not specified, default to '2375' port.")
    @Min(0)
    @Max(65535)
    private Integer dockerPort = 2375;

    @NotNull
    @ApiModelProperty(value = "Specify if tls enabled is true or false")
    @JsonProperty(defaultValue = "false")
    private Boolean tlsEnabled = false;

    @Column
    private String certificatesName;

    @JsonIgnore
    @Transient
    protected transient DockerCertificates certificatesSpec;
}
