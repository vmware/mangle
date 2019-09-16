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

import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

/**
 * AWS Connection Properties model class
 *
 * @author kumargautam
 */
@UserDefinedType("awsConnectionProperties")
@ApiModel(description = "AWS connection properties should be specified if endpoint type is AWS")
@Data
public class AWSConnectionProperties implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotEmpty
    private String region;
}
