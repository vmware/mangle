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
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

/**
 * Model Class for WaveFront Connection Properties
 *
 * @author kumargautam
 * @author ashrimali
 */
@UserDefinedType("waveFrontConnectionProperties")
@Data
@ApiModel(description = "WaveFrontMetricProviderProperties should be specified if MetricProvider type is WaveFront Metric Provider")
public class WaveFrontConnectionProperties implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Proxy instance for wavefront.")
    @NotEmpty
    private String waveFrontProxy;

    @ApiModelProperty(value = "WaveFrontProxy Port for the Metric Provider, if not specified default to '2878' port")
    @JsonProperty(defaultValue = "2878")
    @NotNull
    private Integer waveFrontProxyPort;

    @ApiModelProperty(value = "Source which is generating the metrics, if not specified default to 'Mangle'")
    @JsonProperty(defaultValue = "Mangle")
    @NotEmpty
    private String source;

    @ApiModelProperty(value = "Static tags for the metrics")
    private Map<String, String> staticTags;

    @ApiModelProperty(value = "https://<wavefront host>")
    @NotEmpty
    private String wavefrontInstance;

    @ApiModelProperty(value = "<bearer token>")
    @NotEmpty
    private String wavefrontAPIToken;

    @ApiModelProperty(value = "Wavefront Prefix to append with metrics for the metrics")
    @JsonProperty(defaultValue = "Mangle")
    @NotEmpty
    private String prefix;
}
