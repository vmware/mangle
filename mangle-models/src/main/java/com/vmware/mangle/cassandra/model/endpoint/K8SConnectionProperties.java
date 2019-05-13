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

import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

/**
 * K8S Connection Properties
 *
 * @author kumargautam
 */
@UserDefinedType("k8sConnectionProperties")
@ApiModel(description = "K8S connection properties should be specified if endpoint type is K8S_CLUSTER")
@Data
public class K8SConnectionProperties implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "Namespace of the cluster, if not specified default to 'default' namespace", example = "default")
    @Pattern(regexp = "[a-z0-9]([-a-z0-9]*[a-z0-9])?", message = "must consist of lower case alphanumeric characters or '-', and must start and end with an alphanumeric character (e.g. 'my-name',  or '123-abc')")
    @JsonProperty(defaultValue = "default")
    private String namespace;
}
