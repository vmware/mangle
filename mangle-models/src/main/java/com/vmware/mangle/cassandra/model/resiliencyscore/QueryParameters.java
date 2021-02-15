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

package com.vmware.mangle.cassandra.model.resiliencyscore;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;


/**
 * @author dbhat
 *
 */
@UserDefinedType("queryParameters")
@Data
@ApiModel(description = "queryParameters defines the metric provider query and associated weight to be applied while calculating the Resiliency score")
public class QueryParameters implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Weight to be applied while calculating the Resiliency score for the specified query")
    @NotEmpty
    private float weight;

    @ApiModelProperty(value = "Alert query / condition which returns time series values as 0 or 1")
    @NotEmpty
    private String query;
}
