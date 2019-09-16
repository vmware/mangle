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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import com.vmware.mangle.model.constants.MetricProviderConstant;

/**
 * Model Class for Datadog Connection Properties
 *
 * @author ashrimali
 */

@UserDefinedType("datadogConnectionProperties")
@Data
@ApiModel(description = "datadogConnectionProperties should be specified if MetricProvider type is Datadog Metric Provider")
public class DatadogConnectionProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Datadog API Key")
    @NotEmpty
    private String apiKey;

    @ApiModelProperty(value = "Datadog Application Key")
    @NotEmpty
    private String applicationKey;

    @ApiModelProperty(value = "Datadog API Instance Uri")
    @JsonIgnore
    @NotEmpty
    private String uri = MetricProviderConstant.DATADOG_URI;

    @ApiModelProperty(value = "Static tags for the metrics")
    private Map<String, String> staticTags;
}
