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

package com.vmware.mangle.services.mockdata;

/**
 * @author dbhat
 */
public class MockDataConstants {
    private MockDataConstants() {

    }

    public static final String RESILIENCY_SCORE_METRIC_CONFIG_METRIC_NAME = "mangle.resiliency.score";
    public static final String RESILIENCY_SCORE_METRIC_CONFIG_GRANULARITY = "m";
    public static final short RESILIENCY_SCORE_METRIC_CONFIG_TEST_REFERENCE_WINDOW = 1;
    public static final short RESILIENCY_SCORE_METRIC_CONFIG_CALCULATION_WINDOW = 15;
    public static final String SOURCE = "source";
    public static final String MANGLE = "mangle";
    public static final String SERVICE_FAMILY_NAME = "mangle";
    public static final String SERVICE_NAME = "cassandra";
    public static final String DUMMY_QUERY_1 = "ts(pod.cpu.usage > 10)";
    public static final float WEIGHT_1 = 0.8f;
    public static final String QUERY_NAME = "high-cpu-usage";
    public static final String FAULT_EVENT_NAME = "cpu-fault-injected-r12and0m-123";
    public static final double RESILIENCY_SCORE = 0.9;
    public static final String STATUS_MESSAGE = "Resiliency Score calculating was successful";
    public static final String TASK_NAME = "dummyTaskName";
    public static final String TASK_DESCRIPTION = "dummyTaskDescription";
    public static final String TASK_NAME1 = "dummy1TaskName";
    public static final String TASK_DESCRIPTION1 = "dummy1TaskDescription";

    public static final String DYNATRACE_API_KEY =
            "dt0c01.ST2EY72KQINMH574WMNVI7YN.G3DFPBEJYMODIDAEX454M7YWBUVEFOWKPRVMWFASS64NFH52PX6BNDVFFM572RZM";
    public static final String DYNATRACE_DEVICE_ID = "sample";
    public static final String DYNATRACE_URI = "https://mySampleEnv.live.dynatrace.com";
    public static final String DYNATRACE_INSTANCE_NAME = "dynatrace_trail_instance";
    public static final String DYNATRACE_ID = "abcd-efgh-23";

    public static final String INVALID_URI_MULTIPLE_FORWARD_SLASHES = "https://my-metric-provider.eng.vmware.com////";
    public static final String VALID_URI = "https://my-metric-provider.eng.vmware.com";
    public static final String INVALID_URI_SINGLE_FORWARD_SLASH = "https://my-metric-provider.eng.vmware.com/";
    public static final String DYNATRACE_METRIC_PROVIDER_NAME = "mangle-dynatrace-instance";
}
