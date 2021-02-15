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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.vmware.mangle.cassandra.model.MangleDto;
import com.vmware.mangle.model.constants.ResiliencyScoreConstant;

/**
 * @author dbhat
 *
 */
@Table(value = "resiliencyscore_metric_config")
@Data
@JsonIgnoreProperties(value = { "primaryKey" })
public class ResiliencyScoreMetricConfig extends MangleDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @PrimaryKeyColumn(value = "name", type = PrimaryKeyType.PARTITIONED)
    @ApiModelProperty(value = "ResiliencyScore Metric Configuration Name. Default value: "
            + ResiliencyScoreConstant.RESILIENCY_SCORE_METRIC_CONFIG_NAME)
    @JsonProperty(required = false)
    private String name = ResiliencyScoreConstant.RESILIENCY_SCORE_METRIC_CONFIG_NAME;

    @ApiModelProperty(position = 1, value = "Metric Name for resiliency score. Default value: "
            + ResiliencyScoreConstant.RESILIENCY_SCORE_METRIC_NAME)
    @Column
    @JsonProperty(required = false)
    private String metricName = ResiliencyScoreConstant.RESILIENCY_SCORE_METRIC_NAME;

    @ApiModelProperty(position = 2, value = "Metric source to be associated for resiliency score metric. Default value: "
            + ResiliencyScoreConstant.RESILIENCY_SCORE_METRIC_SOURCE)
    @Column
    @JsonProperty(required = false)
    private String metricSource = ResiliencyScoreConstant.RESILIENCY_SCORE_METRIC_SOURCE;

    @ApiModelProperty(position = 3, value = "Test reference: pre injection and post injection time window length in mins. Default value: "
            + ResiliencyScoreConstant.TEST_REFERENCE_WINDOW_IN_MINUTE)
    @Column
    @JsonProperty(required = false)
    private Short testReferenceWindow = ResiliencyScoreConstant.TEST_REFERENCE_WINDOW_IN_MINUTE;

    @ApiModelProperty(position = 4, value = "Resiliency score calculation window length in hours. Default value: "
            + ResiliencyScoreConstant.RESILIENCY_SCORE_CALCULATION_WINDOW_IN_HOUR)
    @JsonProperty(required = false)
    @Column
    private Short resiliencyCalculationWindow = ResiliencyScoreConstant.RESILIENCY_SCORE_CALCULATION_WINDOW_IN_HOUR;

    @ApiModelProperty(position = 5, value = "Granularity with which the time series data to be retrieved from Metric provider. Default value: "
            + ResiliencyScoreConstant.METRIC_QUERY_GRANULARITY)
    @Column
    @JsonProperty(required = false)
    private String metricQueryGranularity = ResiliencyScoreConstant.METRIC_QUERY_GRANULARITY;

    @Override
    @JsonIgnore
    public String getPrimaryKey() {
        return name;
    }

}
