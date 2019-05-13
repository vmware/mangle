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

package com.vmware.mangle.unittest.metric.constants;

/**
 * MetricReporterTess Constants.
 *
 * @author kumargautam
 * @author dbhat
 */
public class MetricReporterTestConstants {

    private MetricReporterTestConstants() {
    }

    @SuppressWarnings("squid:S1313")
    public static final String WAVEFRONT_INSTANCE = "https://10.2.3.50";
    public static final String WAVEFRONT_API_TOKEN = "0348dfdf83834nddbd7rsbd";
    @SuppressWarnings("squid:S1313")
    public static final String WAVEFRONT_PROXY = "10.23.22.4";
    public static final Integer WAVEFRONT_PROXY_PORT = 2878;
    public static final String WAVEFRONT_SOURCE = "mangle";
    public static final String WAVEFRONT_STATIC_TAG_KEY = "cluster";
    public static final String WAVEFRONT_STATIC_TAG_VALUE = "dev";
    public static final String TAGS = "product=mangle,app=lemans";
    public static final String CPU_USAGE = "cpu.usage";
}

