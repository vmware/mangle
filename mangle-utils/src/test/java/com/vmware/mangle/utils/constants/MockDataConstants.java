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

package com.vmware.mangle.utils.constants;

/**
 * @author dbhat
 */
public class MockDataConstants {
    private MockDataConstants() {

    }

    public static final int METRIC_PROVIDER_PROXY_PORT = 2878;
    public static final String METRIC_PROVIDER_PROXY_HOST = "wavefront.host.com";
    public static final String SOURCE = "source";
    public static final String MANGLE = "mangle";
    public static final String WAVEFRONT_INSTANCE = "https://dummy-wf-instance.mangle.com";
    public static final String DUMMY_API_TOKEN = "c234-sdf-2345-dsfs";

    public static final int FAULT_INJECTION_DURATION_IN_MINUTE = 3;
    public static final String FAULT_INJECTED_EVENT_NAME = "mangle-fault-injected";
    public static final String METRIC_PROVIDER_NAME = "try.wavefront.com";
    public static final String RESILIENCY_SCORE_METRIC_CONFIG_METRIC_NAME = "mangle.resiliency.score";
    public static final String RESILIENCY_SCORE_METRIC_CONFIG_GRANULARITY = "m";
    public static final short RESILIENCY_SCORE_METRIC_CONFIG_TEST_REFERENCE_WINDOW = 1;
    public static final short RESILIENCY_SCORE_METRIC_CONFIG_CALCULATION_WINDOW = 15;
    public static final String DUMMY_QUERY_1 = "ts(pod.cpu.usage > 10)";
    public static final float WEIGHT_1 = 0.8f;
    public static final String QUERY_NAME = "high-cpu-usage";
    public static final String SERVICE_NAME = "cassandra";
    public static final String SERVICE_FAMILY_NAME = "mangle";

    public static final String METRIC_NAME = "mangle.test.metric.name";
    public static final String APP = "app";
    public static final double METRIC_VALUE = 0.99;
}
