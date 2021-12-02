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

package com.vmware.mangle.cassandra.model.metricprovider;


import java.io.Serializable;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

/**
 * Model class for representing the connection properties respective to Dynatrace Monitoring system instance.
 *
 * @author dbhat
 */


@UserDefinedType("dynatraceConnectionProperties")
@Data
@ApiModel(description = "DynatraceConnectionProperties should be specified if MetricProvider type is Dynatrace Metric Provider")
public class DynatraceConnectionProperties implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Device ID is a unique ID of the custom device publishing metric")
    @NotEmpty
    private String deviceId;

    @ApiModelProperty(value = "Static tags for the metrics")
    private Map<String, String> staticTags;

    @ApiModelProperty(value = "uri of Dynatrace instance. Example: https://XXXXXXX.live.dynatrace.com")
    @NotEmpty
    private String uri;

    @ApiModelProperty(value = "API token for Dynatrace instance")
    @NotEmpty
    private String apiToken;

}
