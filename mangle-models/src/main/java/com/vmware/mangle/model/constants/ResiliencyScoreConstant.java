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

package com.vmware.mangle.model.constants;

/**
 * @author dbhat
 *
 */
public class ResiliencyScoreConstant {

    private ResiliencyScoreConstant() {

    }

    @SuppressWarnings("javadoc")
    public static final String RESILIENCY_SCORE_METRIC_CONFIG_NAME = "resiliencyScoreConfig";
    public static final String RESILIENCY_SCORE_METRIC_NAME = "mangle.resiliency.score";
    public static final short TEST_REFERENCE_WINDOW_IN_MINUTE = 15;
    public static final short RESILIENCY_SCORE_CALCULATION_WINDOW_IN_HOUR = 1;
    public static final String METRIC_QUERY_GRANULARITY = "m";
    public static final String RESILIENCY_SCORE_METRIC_SOURCE = "mangle";
    public static final String RESILIENCY_SCORE_DEFAULT_SERVICE_FAMILY_NAME = "default";
}
