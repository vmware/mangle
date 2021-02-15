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

package com.vmware.mangle.metric.common;

/**
 * @author dbhat
 *
 *
 */
public class Constants {

    private Constants() {
    }

    // Wavefront configuration file parameters
    public static final String CONFIG_PARAM_WAVEFRONT_PROXY = "wavefront-proxy";
    public static final String CONFIG_PARAM_WAVEFRONT_PROXY_PORT = "wavefront-proxy-port";
    public static final String CONFIG_PARAM_METRIC_SOURCE = "source";
    public static final String CONFIG_PARAM_STATIC_TAGS = "static-tags";

    //Default configuration parameters to use
    public static final String DEFAULT_WAVEFRONT_PROXY = null;
    public static final Integer DEFAULT_WAVEFRONT_PROXY_PORT = 2878;
    public static final String WAVEFRONT_CONFIG_FILE_NAME = "wavefront.config";
    public static final String DEFAULT_METRIC_SOURCE = "mangle";

    public static final String CALCULATE_FUNCTIONAL_SCORE_ARG_NAME = "calculateFunctionalScore";
    public static final String API_WEIGHTAGE__ARG_NAME = "apiHealthWeightage";
    public static final String URL_SUFFIX = "url";
    public static final String FAULT = "faultEventName";
}
