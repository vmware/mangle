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

package com.vmware.mangle.cassandra.model.plugin;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import com.vmware.mangle.model.enums.EndpointType;

/**
 * Model class used to store Extensions name.
 *
 * @author kumargautam
 */
@UserDefinedType("CustomFaultDescriptorResponse")
@Data
public class CustomFaultDescriptorResponse extends CustomFaultExecutionRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    @NotEmpty
    private List<EndpointType> supportedEndpoints;
}
