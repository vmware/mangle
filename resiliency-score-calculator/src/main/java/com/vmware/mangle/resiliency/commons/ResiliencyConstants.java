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

package com.vmware.mangle.resiliency.commons;

/**
 * @author chetanc
 *
 *
 */
public class ResiliencyConstants {
    private ResiliencyConstants() {

    }

    public static final String OPERATION = "operation";
    public static final String PROPERTY_FILE = "property";
    public static final String LOCAL_PROPERTY_FILE = "src/main/resources/wavefront-properties.yaml";
    public static final String RSCORE_PROPERTIES_FILE = "src/main/resources/rscore-properties.yaml";

    public static final String OPERATION_RESELIENCY_CALCULATOR = "calculate";
    public static final String OPERATION_RESELIENCY_EMAIL = "email";

    public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM_SS = "dd-MM-yyyy HH:mm:ss";
    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    public static final String EVENT_QUERY_FILTER = "events(type=\"fault-injection\" and %s)";
    public static final String WAVEFRONT_QUERY_URL = "/api/v2/chart/api?g=d&s=%s&e=%s&q=%s&&listMode=true";
    public static final String WAVEFRONT_QUERY_URL_MINUTE_GRANULARITY =
            "/api/v2/chart/api?g=m&s=%s&e=%s&q=%s&&listMode=true";
    public static final String WAVEFRONT_METRIC_QUERY_URL =
            "/api/v2/chart/api?g=%s&s=%s&e=%s&q=%s&strict=true&summarization=LAST";
    public static final String METRIC_QUERY_TOTAL_FILTER_GROUP_BY_URL = "sum(mcount(1m, ts(%s, Service=%s)), URL)";
    public static final String METRIC_QUERY_TOTAL_FILTER_GROUP_BY_URL_SOURCE =
            "sum(mcount(1m, ts(%s, source=%s, Service=%s)), URL)";
    public static final String METRIC_QUERY_SUCCESS_FILTER_GROUP_BY_URL =
            "sum(mcount(1m, ts(%s, Service=%s and statusCode=\"2*\")), URL)";

    public static final String METRIC_QUERY_SUCCESS_FILTER_GROUP_BY_URL_SOURCE =
            "sum(mcount(1m, ts(%s, source=%s, Service=%s and statusCode=\"2*\")), URL)";
    public static final String SERVICE = "service";
    public static final String SERVICE_FAMILY = "serviceFamily";

    public static final String SERVICE_FAMILY_INJECTION_EVENT =
            "/api/v2/chart/api?g=m&s=%s&q=events(type=\"fault-injection\" and eventTag=\"\\\"ServiceFamily:%s\\\"\")";
    public static final String SERVICE_FAMILY_RELISIENCY_SCORE =
            "/api/v2/chart/api?g=m&p=1&s=%s&q=avg(ts(\"%s\" and serviceFamily=%s))";
    public static final String SERVICE_RELISIENCY_SCORE =
            "/api/v2/chart/api?g=m&p=1&s=%s&q=avg(ts(\"%s\" and Service=%s))";
    public static final String SERVICE_TOTAL_API_COUNT =
            "/api/v2/chart/api?g=m&p=1&s=%s&q=sum(mcount(1m,ts(\"%s\" and Service=\"%s\")), URL)";
    public static final String SERVICE_PASS_API_COUNT =
            "/api/v2/chart/api?g=m&p=1&s=%s&q=sum(mcount(1m,ts(\"%s\" and statusCode=\"2*\" and Service=\"%s\")),URL)";

    public static final int THREAD_POOL_SIZE = 5;
    public static final String METRIC_SOURCE = "mangle";
    public static final String COMMON_SERVICE_FAMILY_NAME = "common";

}
